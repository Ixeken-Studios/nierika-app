package com.ixeken.nierika.core.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import java.io.File

/**
 * Modelo de dominio inmutable para un elemento resguardado dentro de la Bóveda Oculta.
 */
@Immutable
data class VaultMediaItem(
    val id: Long,
    val fileName: String,
    val internalPath: String,
    val mimeType: String,
    val dateAdded: Long,
    val size: Long,
    val width: Int = 0,
    val height: Int = 0,
    val durationMs: Long = 0L,
    val aspectRatio: Float = 1.0f
) {
    val isGif: Boolean get() = mimeType.equals("image/gif", ignoreCase = true) || fileName.endsWith(".gif", ignoreCase = true)
    val isVideo: Boolean get() = mimeType.startsWith("video/", ignoreCase = true) && !isGif
    val file: File get() = File(internalPath)
    val uri: Uri get() = Uri.fromFile(file)
    val formattedDuration: String
        get() {
            if (durationMs <= 0L) return ""
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
            }
        }
}
