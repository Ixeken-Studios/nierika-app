package com.ixeken.nierika.feature.editor.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tira de miniaturas de fotogramas de video para el componente de recorte VideoTimelineTrimmer.
 * Extrae asíncronamente en Dispatchers.IO una secuencia progresiva de 8 fotogramas sincronizados
 * mediante MediaMetadataRetriever, liberando el recurso de manera segura y renderizando las miniaturas
 * con compensación de padding horizontal para alinear el video exactamente con las manijas.
 */
@Composable
fun VideoThumbnailFilmstrip(
    videoUri: String,
    durationMs: Long,
    handleWidthDp: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnails = remember { mutableStateListOf<Bitmap>() }

    LaunchedEffect(videoUri, durationMs) {
        if (durationMs > 0) {
            thumbnails.clear()
            withContext(Dispatchers.IO) {
                var retriever: MediaMetadataRetriever? = null
                try {
                    retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, Uri.parse(videoUri))
                    val frameCount = 8
                    val stepUs = (durationMs * 1000L) / frameCount

                    for (i in 0 until frameCount) {
                        val timeUs = (i * stepUs).coerceAtMost(durationMs * 1000L)
                        val frame = retriever.getFrameAtTime(
                            timeUs,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        if (frame != null) {
                            val scaled = Bitmap.createScaledBitmap(frame, 120, 90, true)
                            withContext(Dispatchers.Main) {
                                thumbnails.add(scaled)
                            }
                        }
                        kotlinx.coroutines.yield()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        retriever?.release()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val filmstripBg = MaterialTheme.colorScheme.surfaceContainerHighest
    val placeholderBg = MaterialTheme.colorScheme.surfaceContainerLow

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = handleWidthDp)
            .background(filmstripBg)
    ) {
        if (thumbnails.isNotEmpty()) {
            thumbnails.forEach { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(placeholderBg)
            )
        }
    }
}
