package com.ixeken.nierika.core.mediastore

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitario de alto rendimiento para extraer y guardar fotogramas de video como imágenes fotográficas.
 * Utiliza el decodificador nativo por hardware de Android (MediaMetadataRetriever) en Dispatchers.IO
 * e inserta la imagen directamente en el MediaStore del sistema (Pictures/Nierika).
 */
object VideoFrameExtractor {

    suspend fun saveVideoFrame(
        context: Context,
        videoUri: Uri,
        positionMs: Long
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            // OPTION_CLOSEST busca el fotograma más preciso al timestamp indicado (en microsegundos)
            val frameBitmap = retriever.getFrameAtTime(
                positionMs * 1000L,
                MediaMetadataRetriever.OPTION_CLOSEST
            ) ?: return@withContext Result.failure(Exception("Could not extract video frame"))

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "FRAME_${timestamp}_${positionMs}ms.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Nierika")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to insert MediaStore record"))

            resolver.openOutputStream(outputUri)?.use { outputStream ->
                frameBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                outputStream.flush()
            } ?: return@withContext Result.failure(Exception("Failed to write bitmap stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(outputUri, contentValues, null, null)
            }

            Result.success(outputUri)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
