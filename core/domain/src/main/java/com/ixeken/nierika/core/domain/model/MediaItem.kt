package com.ixeken.nierika.core.domain.model

import androidx.compose.runtime.Immutable

/**
 * Modelo de dominio inmutable que representa un elemento multimedia en la galería.
 */
@Immutable
data class MediaItem(
    val id: Long,
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val dateTakenMillis: Long,
    val dateModifiedMillis: Long,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val isFavorite: Boolean = false,
    val isTrash: Boolean = false,
    val bucketName: String? = null,
    val relativePath: String? = null,
    val tags: List<String> = emptyList(),
    val dateExpiresMillis: Long = 0L,
    val addedToAlbumMillis: Long = 0L,
    val durationMillis: Long = 0L,
    val trashedAtMillis: Long = 0L
) {
    val formattedDuration: String
        get() {
            if (durationMillis <= 0L) return ""
            val totalSeconds = durationMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
            }
        }

    /**
     * Marca temporal en milisegundos de cuándo se mandó el elemento a la papelera.
     * Prioriza trashedAtMillis de Room o (dateExpiresMillis - 30 días) de MediaStore,
     * permitiendo ordenar la papelera por fecha de borrado en lugar de la fecha de captura.
     */
    val effectiveTrashedAtMillis: Long
        get() {
            val mediaStoreTrashed = if (dateExpiresMillis > 0L) {
                dateExpiresMillis - (30L * 24 * 60 * 60 * 1000L)
            } else {
                0L
            }
            val maxKnown = maxOf(trashedAtMillis, mediaStoreTrashed)
            return when {
                maxKnown > 0L -> maxKnown
                dateModifiedMillis > 0L -> dateModifiedMillis
                else -> dateTakenMillis
            }
        }

    val daysRemainingInTrash: Int
        get() {
            val now = System.currentTimeMillis()
            val expires = if (dateExpiresMillis > 0) {
                dateExpiresMillis
            } else {
                val base = if (trashedAtMillis > 0L) trashedAtMillis else dateModifiedMillis
                base + 30L * 24 * 60 * 60 * 1000L
            }
            val diffMs = expires - now
            val days = kotlin.math.ceil(diffMs.toDouble() / (24 * 60 * 60 * 1000)).toInt()
            return days.coerceIn(1, 30)
        }
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1.0f

    /**
     * Determina si el elemento es una imagen animada en formato GIF.
     * Permite tratarlos nativamente como imágenes en movimiento en lugar de archivos de video.
     */
    val isGif: Boolean
        get() = mimeType.equals("image/gif", ignoreCase = true) ||
                displayName.endsWith(".gif", ignoreCase = true) ||
                uri.endsWith(".gif", ignoreCase = true)

    val isVideo: Boolean
        get() = mimeType.startsWith("video") && !isGif

    val isScreenshot: Boolean
        get() {
            val name = displayName.lowercase()
            val bucket = bucketName?.lowercase() ?: ""
            val path = relativePath?.lowercase() ?: ""
            return bucket.contains("screenshot") ||
                   bucket.contains("screen_capture") ||
                   bucket.contains("captura") ||
                   path.contains("screenshot") ||
                   path.contains("capturas") ||
                   name.startsWith("screenshot") ||
                   name.startsWith("screen_")
        }

    val isCameraPhoto: Boolean
        get() {
            val bucket = bucketName?.lowercase() ?: ""
            val path = relativePath?.lowercase() ?: ""
            return (bucket == "camera" || bucket == "100andro" || bucket == "cámara" || path.contains("dcim/camera")) && !isScreenshot
        }
}
