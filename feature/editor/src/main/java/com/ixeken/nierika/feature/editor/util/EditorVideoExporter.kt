package com.ixeken.nierika.feature.editor.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.toArgb
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.Crop
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import com.ixeken.nierika.feature.editor.EditorUiState
import com.ixeken.nierika.feature.editor.model.DrawPathData
import com.ixeken.nierika.feature.editor.model.TextBackgroundStyle
import com.ixeken.nierika.feature.editor.model.TextStickerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Utilitario de exportación y recodificación de video asíncrona mediante Media3 Transformer.
 * Aplica recortes temporales, transformaciones geométricas (rotación y volteo horizontal/vertical),
 * estampado de capas de texto flotante y dibujo a mano alzada mediante Video Overlays, gestión de audio
 * y registro del archivo resultante en MediaStore API del sistema.
 */
@OptIn(UnstableApi::class)
object EditorVideoExporter {

    suspend fun exportVideo(
        context: Context,
        state: EditorUiState,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val sourceUri = Uri.parse(state.mediaUri)

            // 1. Obtener dimensiones y rotación nativa del video
            var videoWidth = 1280
            var videoHeight = 720
            var videoRotation = 0

            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, sourceUri)
                val rawW = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                val rawH = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                val rawRot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()

                if (rawW != null && rawW > 0) videoWidth = rawW
                if (rawH != null && rawH > 0) videoHeight = rawH
                if (rawRot != null) videoRotation = rawRot
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val isRotated90or270 = videoRotation == 90 || videoRotation == 270
            val effectiveW = if (isRotated90or270) videoHeight else videoWidth
            val effectiveH = if (isRotated90or270) videoWidth else videoHeight

            // 2. Configurar rango de tiempo recortado (Trim)
            val clippingConfig = MediaItem.ClippingConfiguration.Builder().apply {
                if (state.trimStartMs > 0L) {
                    setStartPositionMs(state.trimStartMs)
                }
                if (state.trimEndMs > 0L && (state.videoDurationMs <= 0L || state.trimEndMs < state.videoDurationMs)) {
                    setEndPositionMs(state.trimEndMs)
                }
                setStartsAtKeyFrame(false)
            }.build()

            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(clippingConfig)
                .build()

            // 3. Configurar efectos de video (Rotación, Volteo, Textos y Dibujo)
            val videoEffects = mutableListOf<Effect>()

            // 3a. Rotación y Volteo
            if (state.rotationDegrees != 0f || state.isFlippedHorizontally || state.isFlippedVertically) {
                val scaleX = if (state.isFlippedHorizontally) -1f else 1f
                val scaleY = if (state.isFlippedVertically) -1f else 1f
                val scaleAndRotate = ScaleAndRotateTransformation.Builder()
                    .setRotationDegrees(state.rotationDegrees)
                    .setScale(scaleX, scaleY)
                    .build()
                videoEffects.add(scaleAndRotate)
            }

            // 3b. Recorte de Video (Crop en NDC [-1f..1f])
            val crop = state.videoCropBounds
            val isCropped = crop != null && (crop.left > 0.001f || crop.top > 0.001f || crop.right < 0.999f || crop.bottom < 0.999f)
            if (crop != null && isCropped) {
                val ndcLeft = -1f + 2f * crop.left
                val ndcRight = -1f + 2f * crop.right
                val ndcBottom = 1f - 2f * crop.bottom
                val ndcTop = 1f - 2f * crop.top
                videoEffects.add(Crop(ndcLeft, ndcRight, ndcBottom, ndcTop))
            }

            // 3c. Superposición de Stickers de Texto y Trazos de Dibujo
            if (state.textStickers.isNotEmpty() || state.drawingPaths.isNotEmpty()) {
                val overlayW = if (crop != null && isCropped) (effectiveW * crop.width).toInt().coerceAtLeast(16) else effectiveW
                val overlayH = if (crop != null && isCropped) (effectiveH * crop.height).toInt().coerceAtLeast(16) else effectiveH
                val overlayBitmap = renderVideoOverlayBitmap(
                    width = overlayW,
                    height = overlayH,
                    textStickers = state.textStickers,
                    drawingPaths = state.drawingPaths
                )
                if (overlayBitmap != null) {
                    val bitmapOverlay = BitmapOverlay.createStaticBitmapOverlay(overlayBitmap)
                    videoEffects.add(OverlayEffect(listOf(bitmapOverlay)))
                }
            }

            val editedMediaItemBuilder = EditedMediaItem.Builder(mediaItem)
                .setRemoveAudio(state.isVideoMuted)

            if (videoEffects.isNotEmpty()) {
                editedMediaItemBuilder.setEffects(Effects(emptyList(), videoEffects))
            }

            val editedMediaItem = editedMediaItemBuilder.build()

            val cacheFile = File(context.cacheAreaDir(), "temp_export_${System.currentTimeMillis()}.mp4")
            if (cacheFile.exists()) cacheFile.delete()

            var isCompleted = false
            var exportError: Throwable? = null

            // 4. Construir e iniciar Media3 Transformer en Main Thread (Looper obligatorio)
            withContext(Dispatchers.Main) {
                val transformer = Transformer.Builder(context)
                    .experimentalSetTrimOptimizationEnabled(true)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            isCompleted = true
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            exportError = exportException
                            isCompleted = true
                        }
                    })
                    .build()

                transformer.start(editedMediaItem, cacheFile.absolutePath)

                // Monitor de progreso de exportación en Main Thread
                val progressHolder = ProgressHolder()
                while (!isCompleted) {
                    val stateResult = transformer.getProgress(progressHolder)
                    if (stateResult == Transformer.PROGRESS_STATE_AVAILABLE) {
                        onProgress(progressHolder.progress / 100f)
                    }
                    delay(100)
                }
            }

            if (exportError != null) {
                return Result.failure(exportError!!)
            }

            // 5. Guardar archivo procesado en MediaStore en Dispatchers.IO
            withContext(Dispatchers.IO) {
                val fileName = "Nierika_Video_${System.currentTimeMillis()}.mp4"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Video.Media.RELATIVE_PATH, "${android.os.Environment.DIRECTORY_PICTURES}/Nierika")
                        put(MediaStore.Video.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val outputUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry for video"))

                resolver.openOutputStream(outputUri)?.use { outStream ->
                    cacheFile.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(outputUri, contentValues, null, null)
                }

                cacheFile.delete()
                Result.success(outputUri.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Renderiza los stickers de texto flotante y trazos de dibujo sobre un Bitmap transparente
     * escalado a la resolución exacta del video para ser aplicado como Overlay en Media3 Transformer.
     */
    private fun renderVideoOverlayBitmap(
        width: Int,
        height: Int,
        textStickers: List<TextStickerData>,
        drawingPaths: List<DrawPathData>
    ): Bitmap? {
        if (width <= 0 || height <= 0) return null
        return try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val outW = width.toFloat()
            val outH = height.toFloat()

            // 1. Renderizar trazos de dibujo
            drawingPaths.forEach { drawPath ->
                val strokeWidthOnBitmap = (drawPath.strokeWidth * (outW / 400f)).coerceAtLeast(1f)
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = strokeWidthOnBitmap
                    if (drawPath.isEraser) {
                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    } else {
                        color = drawPath.color.toArgb()
                    }
                }

                if (drawPath.points.size > 1) {
                    val path = android.graphics.Path().apply {
                        val first = drawPath.points.first()
                        moveTo(first.x * outW, first.y * outH)
                        drawPath.points.drop(1).forEach { pt ->
                            lineTo(pt.x * outW, pt.y * outH)
                        }
                    }
                    canvas.drawPath(path, strokePaint)
                } else if (drawPath.points.isNotEmpty()) {
                    val first = drawPath.points.first()
                    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = drawPath.color.toArgb()
                    }
                    canvas.drawCircle(first.x * outW, first.y * outH, strokeWidthOnBitmap / 2f, circlePaint)
                }
            }

            // 2. Renderizar capas de texto flotante (Stickers)
            textStickers.forEach { sticker ->
                val posX = sticker.positionNorm.x * outW
                val posY = sticker.positionNorm.y * outH
                val scaledFontSize = sticker.fontSize * sticker.scale * (outW / 400f)

                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = scaledFontSize
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }

                val actualTextColor = when (sticker.bgStyle) {
                    TextBackgroundStyle.SOLID -> {
                        if (sticker.color == androidx.compose.ui.graphics.Color.White) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    }
                    else -> sticker.color.toArgb()
                }
                textPaint.color = actualTextColor

                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(sticker.text, 0, sticker.text.length, textBounds)
                val textWidth = textPaint.measureText(sticker.text)
                val textHeight = textBounds.height().toFloat()

                val padX = 14f * sticker.scale * (outW / 400f)
                val padY = 8f * sticker.scale * (outW / 400f)

                canvas.save()
                canvas.translate(posX, posY)
                canvas.rotate(sticker.rotationDegrees)

                // Fondo estilizado si aplica
                if (sticker.bgStyle != TextBackgroundStyle.TRANSPARENT) {
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = when (sticker.bgStyle) {
                            TextBackgroundStyle.TRANSPARENT -> android.graphics.Color.TRANSPARENT
                            TextBackgroundStyle.SEMI_TRANSPARENT -> android.graphics.Color.argb(165, 0, 0, 0)
                            TextBackgroundStyle.SOLID -> {
                                if (sticker.color == androidx.compose.ui.graphics.Color.White) android.graphics.Color.WHITE else sticker.color.toArgb()
                            }
                        }
                    }
                    val bgRect = android.graphics.RectF(
                        -textWidth / 2f - padX,
                        -textHeight / 2f - padY,
                        textWidth / 2f + padX,
                        textHeight / 2f + padY
                    )
                    val cornerRadius = 12f * sticker.scale * (outW / 400f)
                    canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)
                }

                val textBaselineY = textHeight / 2f - textBounds.bottom
                canvas.drawText(sticker.text, 0f, textBaselineY, textPaint)
                canvas.restore()
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun Context.cacheAreaDir(): File {
        return externalCacheDir ?: cacheDir
    }
}
