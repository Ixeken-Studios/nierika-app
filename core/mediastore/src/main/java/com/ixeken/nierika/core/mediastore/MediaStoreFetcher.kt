package com.ixeken.nierika.core.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.util.Log
import java.util.Locale

/**
 * Representación intermedia ligera de un elemento obtenido directamente de MediaStore.
 */
data class RawMediaItem(
    val id: Long,
    val uriString: String,
    val displayName: String,
    val mimeType: String,
    val dateTakenMillis: Long,
    val dateModifiedMillis: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val bucketName: String?,
    val relativePath: String? = null,
    val dateExpiresMillis: Long = 0L,
    val durationMillis: Long = 0L
)

/**
 * Lógica de bajo nivel para consultar la MediaStore API del sistema operativo Android.
 * Soporta consulta unificada de imágenes y videos con paginación moderna vía Bundle en API 26+.
 */
class MediaStoreFetcher(private val context: Context) {

    suspend fun fetchAllMedia(): List<RawMediaItem> = withContext(Dispatchers.IO) {
        fetchMedia(offset = 0, limit = Int.MAX_VALUE)
    }

    suspend fun fetchMedia(
        offset: Int,
        limit: Int,
        sortAscending: Boolean = false
    ): List<RawMediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<RawMediaItem>()

        val projection = mutableListOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DURATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Files.FileColumns.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                add(MediaStore.Files.FileColumns.DATA)
            }
        }.toTypedArray()

        val sortDirection = if (sortAscending) "ASC" else "DESC"
        val sortSql = "CASE WHEN ${MediaStore.Files.FileColumns.DATE_TAKEN} > 0 THEN ${MediaStore.Files.FileColumns.DATE_TAKEN} ELSE ${MediaStore.Files.FileColumns.DATE_MODIFIED} * 1000 END $sortDirection"

        val baseSelection = buildList {
            add("(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})")
            add("${MediaStore.MediaColumns.SIZE} > 0")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add("${MediaStore.MediaColumns.IS_PENDING} = 0")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add("${MediaStore.MediaColumns.IS_TRASHED} = 0")
            }
        }.joinToString(" AND ")

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    sortSql
                )
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    baseSelection
                )
            }
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                queryArgs,
                null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                baseSelection,
                null,
                "$sortSql LIMIT $limit OFFSET $offset"
            )
        }

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateTakenColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateModifiedColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val widthColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val mediaTypeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val durationColumn = c.getColumnIndex(MediaStore.Files.FileColumns.DURATION)
            val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                c.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                c.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            }

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val mediaType = c.getInt(mediaTypeColumn)

                val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentUri = ContentUris.withAppendedId(baseUri, id).toString()

                val rawDateTaken = c.getLong(dateTakenColumn)
                val rawDateModified = c.getLong(dateModifiedColumn) * 1000L
                val finalDateTaken = if (rawDateTaken > 0) rawDateTaken else rawDateModified

                val rawMimeType = c.getString(mimeColumn) ?: ""
                val finalMimeType = when {
                    rawMimeType.isNotEmpty() -> rawMimeType
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> "video/mp4"
                    else -> "image/jpeg"
                }

                val relativePath = if (pathColumn != -1) c.getString(pathColumn) else null
                val durationMillis = if (durationColumn != -1) c.getLong(durationColumn) else 0L

                mediaList.add(
                    RawMediaItem(
                        id = id,
                        uriString = contentUri,
                        displayName = c.getString(nameColumn) ?: "",
                        mimeType = finalMimeType,
                        dateTakenMillis = finalDateTaken,
                        dateModifiedMillis = rawDateModified,
                        sizeBytes = c.getLong(sizeColumn),
                        width = c.getInt(widthColumn),
                        height = c.getInt(heightColumn),
                        bucketName = c.getString(bucketColumn),
                        relativePath = relativePath,
                        durationMillis = durationMillis
                    )
                )
            }
        }

        return@withContext mediaList
    }

    /**
     * Consulta los elementos multimedia marcados en la papelera nativa de MediaStore (IS_TRASHED = 1).
     */
    suspend fun fetchTrashMedia(
        offset: Int = 0,
        limit: Int = 5000
    ): List<RawMediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<RawMediaItem>()

        val projection = mutableListOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DURATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Files.FileColumns.RELATIVE_PATH)
                add(MediaStore.MediaColumns.DATE_EXPIRES)
            } else {
                @Suppress("DEPRECATION")
                add(MediaStore.Files.FileColumns.DATA)
            }
        }.toTypedArray()

        val sortSql = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "${MediaStore.MediaColumns.DATE_EXPIRES} DESC, ${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        } else {
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        }

        val baseSelection = buildList {
            add("(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE} OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})")
            add("${MediaStore.MediaColumns.SIZE} > 0")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add("${MediaStore.MediaColumns.IS_TRASHED} = 1")
            }
        }.joinToString(" AND ")

        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortSql)
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, baseSelection)
                putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY)
            }
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                queryArgs,
                null
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortSql)
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, baseSelection)
            }
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                queryArgs,
                null
            )
        } else {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                baseSelection,
                null,
                "$sortSql LIMIT $limit OFFSET $offset"
            )
        }

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateTakenColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateModifiedColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val widthColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val mediaTypeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val durationColumn = c.getColumnIndex(MediaStore.Files.FileColumns.DURATION)
            val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                c.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                c.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            }
            val expiresColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                c.getColumnIndex(MediaStore.MediaColumns.DATE_EXPIRES)
            } else -1

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val mediaType = c.getInt(mediaTypeColumn)

                val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentUri = ContentUris.withAppendedId(baseUri, id).toString()

                val rawDateTaken = c.getLong(dateTakenColumn)
                val rawDateModified = c.getLong(dateModifiedColumn) * 1000L
                val finalDateTaken = if (rawDateTaken > 0) rawDateTaken else rawDateModified

                val rawMimeType = c.getString(mimeColumn) ?: ""
                val finalMimeType = when {
                    rawMimeType.isNotEmpty() -> rawMimeType
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> "video/mp4"
                    else -> "image/jpeg"
                }

                val relativePath = if (pathColumn != -1) c.getString(pathColumn) else null
                val rawExpiresSeconds = if (expiresColumn != -1) c.getLong(expiresColumn) else 0L
                val dateExpiresMillis = if (rawExpiresSeconds > 0L) rawExpiresSeconds * 1000L else 0L
                val durationMillis = if (durationColumn != -1) c.getLong(durationColumn) else 0L

                mediaList.add(
                    RawMediaItem(
                        id = id,
                        uriString = contentUri,
                        displayName = c.getString(nameColumn) ?: "",
                        mimeType = finalMimeType,
                        dateTakenMillis = finalDateTaken,
                        dateModifiedMillis = rawDateModified,
                        sizeBytes = c.getLong(sizeColumn),
                        width = c.getInt(widthColumn),
                        height = c.getInt(heightColumn),
                        bucketName = c.getString(bucketColumn),
                        relativePath = relativePath,
                        dateExpiresMillis = dateExpiresMillis,
                        durationMillis = durationMillis
                    )
                )
            }
        }

        return@withContext mediaList
    }

    /**
     * Duplica físicamente un archivo multimedia en la galería de manera robusta y compatible con Scoped Storage.
     * Guarda la copia en la carpeta Pictures/Nierika o Movies/Nierika con timestamp único.
     */
    suspend fun duplicateMedia(
        uriString: String,
        displayName: String?,
        mimeType: String?
    ): String? = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(uriString)
            val contentResolver = context.contentResolver

            val originalName = displayName?.takeIf { it.isNotBlank() && it != "Trash Item" }
                ?: sourceUri.lastPathSegment
                ?: "photo"

            val dotIndex = originalName.lastIndexOf('.')
            val nameWithoutExt = if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
            val extension = if (dotIndex > 0 && dotIndex < originalName.length - 1) {
                originalName.substring(dotIndex + 1).lowercase(Locale.getDefault())
            } else {
                when {
                    mimeType?.contains("png", ignoreCase = true) == true -> "png"
                    mimeType?.contains("webp", ignoreCase = true) == true -> "webp"
                    mimeType?.contains("gif", ignoreCase = true) == true -> "gif"
                    mimeType?.contains("mp4", ignoreCase = true) == true -> "mp4"
                    else -> "jpg"
                }
            }

            val finalMimeType = mimeType?.takeIf { it.isNotBlank() && it != "image/*" }
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                ?: "image/jpeg"

            val isVideo = finalMimeType.startsWith("video", ignoreCase = true)
            val newDisplayName = "${nameWithoutExt}_copy_${System.currentTimeMillis()}.$extension"

            val collectionUri = if (isVideo) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName)
                put(MediaStore.MediaColumns.MIME_TYPE, finalMimeType)
                put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Nierika/")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val newUri = contentResolver.insert(collectionUri, contentValues)
            if (newUri == null) {
                Log.e("MediaStoreFetcher", "No se pudo insertar registro en MediaStore para $newDisplayName")
                return@withContext null
            }

            try {
                val inputStream = contentResolver.openInputStream(sourceUri)
                val outputStream = contentResolver.openOutputStream(newUri, "w")

                if (inputStream != null && outputStream != null) {
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                } else {
                    Log.e("MediaStoreFetcher", "No se pudieron abrir los streams para copiar $sourceUri a $newUri")
                    contentResolver.delete(newUri, null, null)
                    return@withContext null
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    contentResolver.update(newUri, updateValues, null, null)
                }

                Log.d("MediaStoreFetcher", "Duplicación exitosa: $newDisplayName en $newUri")
                newUri.toString()
            } catch (e: Exception) {
                Log.e("MediaStoreFetcher", "Error al escribir stream de duplicación: ${e.message}", e)
                try { contentResolver.delete(newUri, null, null) } catch (_: Exception) {}
                null
            }
        } catch (e: Exception) {
            Log.e("MediaStoreFetcher", "Error general al duplicar archivo: ${e.message}", e)
            null
        }
    }

    /**
     * Elimina permanentemente un elemento multimedia de MediaStore y del almacenamiento del dispositivo.
     */
    suspend fun deleteMediaPermanently(uriString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val file = java.io.File(uri.path ?: "")
                if (file.exists()) file.delete() else true
            } else {
                val rows = context.contentResolver.delete(uri, null, null)
                rows > 0
            }
        } catch (e: Exception) {
            Log.e("MediaStoreFetcher", "Error al eliminar de MediaStore: ${e.message}", e)
            false
        }
    }

    /**
     * Consulta una lista de elementos multimedia específicos a partir de sus IDs.
     */
    suspend fun fetchMediaByIds(ids: List<Long>): List<RawMediaItem> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext emptyList()
        val mediaList = mutableListOf<RawMediaItem>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_TAKEN,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.WIDTH,
            MediaStore.Files.FileColumns.HEIGHT,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DURATION
        )

        val inClause = ids.joinToString(",") { "?" }
        val selection = buildList {
            add("${MediaStore.Files.FileColumns._ID} IN ($inClause)")
            add("${MediaStore.MediaColumns.SIZE} > 0")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add("${MediaStore.MediaColumns.IS_PENDING} = 0")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add("${MediaStore.MediaColumns.IS_TRASHED} = 0")
            }
        }.joinToString(" AND ")
        val selectionArgs = ids.map { it.toString() }.toTypedArray()

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC, ${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateTakenColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN)
            val dateModifiedColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val widthColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH)
            val heightColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT)
            val mediaTypeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
            val durationColumn = c.getColumnIndex(MediaStore.Files.FileColumns.DURATION)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val mediaType = c.getInt(mediaTypeColumn)

                val baseUri = if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val contentUri = ContentUris.withAppendedId(baseUri, id).toString()

                val rawDateTaken = c.getLong(dateTakenColumn)
                val rawDateModified = c.getLong(dateModifiedColumn) * 1000L
                val finalDateTaken = if (rawDateTaken > 0) rawDateTaken else rawDateModified

                val rawMimeType = c.getString(mimeColumn) ?: ""
                val finalMimeType = when {
                    rawMimeType.isNotEmpty() -> rawMimeType
                    mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> "video/mp4"
                    else -> "image/jpeg"
                }
                val durationMillis = if (durationColumn != -1) c.getLong(durationColumn) else 0L

                mediaList.add(
                    RawMediaItem(
                        id = id,
                        uriString = contentUri,
                        displayName = c.getString(nameColumn) ?: "Media_$id",
                        mimeType = finalMimeType,
                        dateTakenMillis = finalDateTaken,
                        dateModifiedMillis = rawDateModified,
                        sizeBytes = c.getLong(sizeColumn),
                        width = c.getInt(widthColumn),
                        height = c.getInt(heightColumn),
                        bucketName = c.getString(bucketColumn),
                        durationMillis = durationMillis
                    )
                )
            }
        }
        mediaList
    }
}
