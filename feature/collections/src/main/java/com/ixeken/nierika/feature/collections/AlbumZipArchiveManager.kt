package com.ixeken.nierika.feature.collections

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Elemento multimedia desacoplado para procesos de compresión en lote a archivo .ZIP.
 */
data class ExportableMedia(
    val id: Long,
    val displayName: String,
    val mimeType: String,
    val uri: Uri
)

/**
 * Modelo de estado para la previsualización del archivo .ZIP antes de importar.
 */
data class ZipPreviewInfo(
    val zipUri: Uri,
    val suggestedAlbumName: String,
    val mediaCount: Int,
    val validEntries: List<String>
)

/**
 * Modelo de estado para la importación y descompresión de un archivo .ZIP a álbum.
 */
data class AlbumImportState(
    val isImporting: Boolean = false,
    val albumName: String = "",
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val progress: Float = 0f,
    val importedAlbumId: Long? = null,
    val isCompleted: Boolean = false,
    val error: String? = null
)

/**
 * Modelo de estado para la compresión y exportación de un álbum a archivo .ZIP.
 */
data class AlbumExportState(
    val isExporting: Boolean = false,
    val albumName: String = "",
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val error: String? = null
)

/**
 * Motor independiente de procesamiento, inspección, compresión y descompresión de archivos .ZIP
 * para álbumes de usuario y colecciones multimedia.
 * Ejecuta todas las operaciones intensivas de I/O en Dispatchers.IO protegiendo la reactividad de la UI.
 */
object AlbumZipArchiveManager {

    private const val BUFFER_SIZE = 64 * 1024

    /**
     * Valida si el nombre de archivo corresponde a un recurso multimedia admisible para la galería.
     */
    fun isValidMediaFile(fileName: String): Boolean {
        val lower = fileName.lowercase()
        if (lower.startsWith("__macosx") || lower.contains("/__macosx") ||
            lower.endsWith(".ds_store") || lower.endsWith("thumbs.db") || lower.endsWith(".nomedia")
        ) {
            return false
        }
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".webp") || lower.endsWith(".heif") || lower.endsWith(".heic") ||
                lower.endsWith(".gif") || lower.endsWith(".avif") || lower.endsWith(".mp4") ||
                lower.endsWith(".mov") || lower.endsWith(".mkv") || lower.endsWith(".3gp") ||
                lower.endsWith(".webm")
    }

    /**
     * Deduce el tipo MIME según la extensión del archivo multimedia.
     */
    fun getMimeTypeForFileName(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".heic") || lower.endsWith(".heif") -> "image/heif"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".avif") -> "image/avif"
            lower.endsWith(".mp4") -> "video/mp4"
            lower.endsWith(".mov") -> "video/quicktime"
            lower.endsWith(".mkv") -> "video/x-matroska"
            lower.endsWith(".3gp") -> "video/3gpp"
            lower.endsWith(".webm") -> "video/webm"
            else -> "image/jpeg"
        }
    }

    /**
     * Inspecciona rápidamente el archivo .ZIP para inferir el nombre del álbum y contabilizar elementos válidos.
     */
    suspend fun inspectZipArchive(context: Context, zipUri: Uri): ZipPreviewInfo? = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(zipUri) ?: return@withContext null

            var defaultName = ""
            val cursor = context.contentResolver.query(zipUri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        defaultName = it.getString(nameIndex).orEmpty()
                    }
                }
            }
            if (defaultName.isBlank()) {
                defaultName = zipUri.lastPathSegment?.substringAfterLast('/') ?: "Imported Album"
            }
            defaultName = defaultName.removeSuffix(".zip").removeSuffix(".ZIP")

            val validEntries = mutableListOf<String>()
            var detectedRootFolder: String? = null

            ZipInputStream(BufferedInputStream(input)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && isValidMediaFile(name)) {
                        validEntries.add(name)
                        if (name.contains('/')) {
                            val firstSegment = name.substringBefore('/')
                            if (!firstSegment.startsWith("__MACOSX") && detectedRootFolder == null) {
                                detectedRootFolder = firstSegment
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val finalAlbumName = detectedRootFolder?.ifBlank { defaultName } ?: defaultName
            if (validEntries.isNotEmpty()) {
                ZipPreviewInfo(
                    zipUri = zipUri,
                    suggestedAlbumName = finalAlbumName,
                    mediaCount = validEntries.size,
                    validEntries = validEntries
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Comprime y escribe en streaming los elementos multimedia en el archivo .ZIP de destino.
     */
    suspend fun exportToZip(
        context: Context,
        destinationUri: Uri,
        folderName: String,
        items: List<ExportableMedia>,
        onProgress: suspend (currentItem: Int, total: Int, progress: Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val total = items.size
        val outputStream = context.contentResolver.openOutputStream(destinationUri)
            ?: throw IllegalStateException("Cannot open destination output stream")

        val usedFileNames = mutableSetOf<String>()
        val safeFolderName = folderName.trim().ifEmpty { "Album" }

        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            val buffer = ByteArray(BUFFER_SIZE)

            items.forEachIndexed { index, item ->
                ensureActive()

                var baseName = item.displayName.ifEmpty { "media_${item.id}" }
                if (!baseName.contains(".")) {
                    val ext = if (item.mimeType.startsWith("video/")) ".mp4" else ".jpg"
                    baseName += ext
                }

                var uniqueName = baseName
                var counter = 1
                while (usedFileNames.contains(uniqueName)) {
                    val dotIndex = baseName.lastIndexOf('.')
                    uniqueName = if (dotIndex != -1) {
                        "${baseName.substring(0, dotIndex)} ($counter)${baseName.substring(dotIndex)}"
                    } else {
                        "$baseName ($counter)"
                    }
                    counter++
                }
                usedFileNames.add(uniqueName)

                val entryPath = "$safeFolderName/$uniqueName"
                val zipEntry = ZipEntry(entryPath)
                zipOut.putNextEntry(zipEntry)

                try {
                    context.contentResolver.openInputStream(item.uri)?.use { input ->
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            ensureActive()
                            zipOut.write(buffer, 0, bytesRead)
                        }
                    }
                } catch (_: Exception) {
                    // Continuar con el siguiente archivo si ocurre un error de lectura individual
                } finally {
                    zipOut.closeEntry()
                }

                val current = index + 1
                val prog = current.toFloat() / total.toFloat()
                onProgress(current, total, prog)
            }
            zipOut.flush()
        }
    }

    /**
     * Extrae en streaming los archivos multimedia de un .ZIP hacia MediaStore (Pictures/Nierika/[albumName]).
     * Retorna la lista de IDs insertados y la URI del primer elemento como carátula.
     */
    suspend fun importZipToMediaStore(
        context: Context,
        zipUri: Uri,
        albumName: String,
        validCount: Int,
        onProgress: suspend (currentItem: Int, total: Int, progress: Float) -> Unit
    ): Pair<List<Long>, String?> = withContext(Dispatchers.IO) {
        val insertedMediaIds = mutableListOf<Long>()
        var firstCoverUri: String? = null

        val inputStream = context.contentResolver.openInputStream(zipUri)
            ?: throw IllegalStateException("Cannot open ZIP input stream")

        val buffer = ByteArray(BUFFER_SIZE)
        var processedCount = 0

        ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                ensureActive()

                val entryName = entry.name
                if (!entry.isDirectory && isValidMediaFile(entryName)) {
                    val displayName = entryName.substringAfterLast('/')
                    val mimeType = getMimeTypeForFileName(displayName)
                    val isVideo = mimeType.startsWith("video/")
                    val targetCollectionUri = if (isVideo) {
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    } else {
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    }

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Nierika/$albumName")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                        val currentTime = System.currentTimeMillis() / 1000
                        put(MediaStore.MediaColumns.DATE_ADDED, currentTime)
                        put(MediaStore.MediaColumns.DATE_MODIFIED, currentTime)
                    }

                    val insertedUri = context.contentResolver.insert(targetCollectionUri, contentValues)
                    if (insertedUri != null) {
                        try {
                            context.contentResolver.openOutputStream(insertedUri)?.use { out ->
                                var readBytes: Int
                                while (zipIn.read(buffer).also { readBytes = it } != -1) {
                                    ensureActive()
                                    out.write(buffer, 0, readBytes)
                                }
                                out.flush()
                            }

                            // Quitar bandera IS_PENDING para hacer el archivo visible
                            val publishValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.IS_PENDING, 0)
                            }
                            context.contentResolver.update(insertedUri, publishValues, null, null)

                            val mediaId = ContentUris.parseId(insertedUri)
                            insertedMediaIds.add(mediaId)
                            if (firstCoverUri == null) {
                                firstCoverUri = insertedUri.toString()
                            }
                        } catch (_: Exception) {
                            try {
                                context.contentResolver.delete(insertedUri, null, null)
                            } catch (_: Exception) {}
                        }
                    }

                    processedCount++
                    val prog = if (validCount > 0) processedCount.toFloat() / validCount.toFloat() else 1f
                    onProgress(processedCount, validCount, prog)
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        Pair(insertedMediaIds, firstCoverUri)
    }
}
