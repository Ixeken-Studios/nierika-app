package com.ixeken.nierika.core.mediastore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.CancellationSignal
import android.util.Size
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.Dimension
import coil3.toAndroidUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Fetcher acelerado por hardware exclusivo para miniaturas de MediaStore (Android 14+ / API 34+).
 * Únicamente intercepta solicitudes explícitas de tamaño miniatura (<= 640px de ancho y alto).
 * Si la solicitud es para resolución completa (Size.ORIGINAL, Dimension.Undefined o > 640px),
 * retorna null de inmediato para que Coil 3 decodifique la imagen original a resolución nativa
 * sin compresión ni pixelado en el visor multimedia.
 */
class MediaStoreThumbnailFetcher(
    private val androidUri: Uri,
    private val options: Options,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        if (androidUri.scheme != ContentResolver.SCHEME_CONTENT || androidUri.authority != "media") {
            return@withContext null
        }

        val widthDim = options.size.width
        val heightDim = options.size.height

        // Si el tamaño no está explícitamente acotado en píxeles (ej. Size.ORIGINAL), no es miniatura: delegar al decodificador completo
        if (widthDim !is Dimension.Pixels || heightDim !is Dimension.Pixels) {
            return@withContext null
        }

        val targetWidth = widthDim.px
        val targetHeight = heightDim.px

        // Si supera el límite de miniatura (640px), delegar a la carga en alta definición
        if (targetWidth > 640 || targetHeight > 640 || targetWidth <= 0 || targetHeight <= 0) {
            return@withContext null
        }

        val cancellationSignal = CancellationSignal()
        try {
            val bitmap = context.contentResolver.loadThumbnail(
                androidUri,
                Size(targetWidth, targetHeight),
                cancellationSignal
            )
            ImageFetchResult(
                image = bitmap.asImage(),
                isSampled = true,
                dataSource = DataSource.DISK
            )
        } catch (_: Exception) {
            // Fallback transparente al pipeline de decodificación estándar
            null
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<coil3.Uri> {
        override fun create(data: coil3.Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme == ContentResolver.SCHEME_CONTENT && data.authority == "media") {
                val widthDim = options.size.width
                val heightDim = options.size.height
                if (widthDim is Dimension.Pixels && heightDim is Dimension.Pixels &&
                    widthDim.px in 1..640 && heightDim.px in 1..640
                ) {
                    return MediaStoreThumbnailFetcher(data.toAndroidUri(), options, context)
                }
            }
            return null
        }
    }

    class AndroidUriFactory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme == ContentResolver.SCHEME_CONTENT && data.authority == "media") {
                val widthDim = options.size.width
                val heightDim = options.size.height
                if (widthDim is Dimension.Pixels && heightDim is Dimension.Pixels &&
                    widthDim.px in 1..640 && heightDim.px in 1..640
                ) {
                    return MediaStoreThumbnailFetcher(data, options, context)
                }
            }
            return null
        }
    }
}
