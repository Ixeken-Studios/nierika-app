package com.ixeken.nierika.feature.editor.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.BitmapShader
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import androidx.exifinterface.media.ExifInterface
import com.ixeken.nierika.core.mediastore.copyExifMetadata
import com.ixeken.nierika.feature.editor.EditorUiState
import com.ixeken.nierika.feature.editor.model.CanvasBackgroundMode
import com.ixeken.nierika.feature.editor.model.CropAspectRatio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Utilitario de exportación y renderizado de imágenes editadas en segundo plano (Dispatchers.IO).
 * Renderiza transformaciones, matriz de color, expansión de lienzo y anotaciones de dibujo,
 * conservando metadatos EXIF en el almacenamiento MediaStore del sistema.
 */
object EditorImageExporter {

    suspend fun saveEditedImage(
        context: Context,
        state: EditorUiState,
        baseCroppedBitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(state.mediaUri)
            val croppedBitmap = if (baseCroppedBitmap != null) {
                // Bitmap recortado directamente con la geometría táctil definida por el usuario
                baseCroppedBitmap
            } else {
                val exifOrientation = try {
                    context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                        val exif = ExifInterface(stream)
                        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    } ?: ExifInterface.ORIENTATION_NORMAL
                } catch (_: Exception) {
                    ExifInterface.ORIENTATION_NORMAL
                }

                val exifRotation = when (exifOrientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                val exifFlipHorizontal = (exifOrientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL || exifOrientation == ExifInterface.ORIENTATION_TRANSVERSE)
                val exifFlipVertical = (exifOrientation == ExifInterface.ORIENTATION_FLIP_VERTICAL)

                // Pre-lectura de dimensiones para calcular inSampleSize seguro (evita OOM en fotos de 50MP+)
                val boundsOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }

                val rawWidth = boundsOptions.outWidth
                val rawHeight = boundsOptions.outHeight
                val maxDimension = 4096
                var sampleSize = 1
                if (rawWidth > maxDimension || rawHeight > maxDimension) {
                    val halfWidth = rawWidth / 2
                    val halfHeight = rawHeight / 2
                    while ((halfWidth / sampleSize) >= maxDimension || (halfHeight / sampleSize) >= maxDimension) {
                        sampleSize *= 2
                    }
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val originalBitmap = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                } ?: return@withContext Result.failure(Exception("Failed to decode source bitmap"))

                // 1. Aplicar rotación y reflejo (combinando orientación EXIF nativa y modificaciones del usuario)
                val matrix = Matrix().apply {
                    val totalRotation = (exifRotation + state.rotationDegrees) % 360f
                    postRotate(totalRotation)
                    val scaleX = if (state.isFlippedHorizontally xor exifFlipHorizontal) -1f else 1f
                    val scaleY = if (state.isFlippedVertically xor exifFlipVertical) -1f else 1f
                    postScale(scaleX, scaleY, originalBitmap.width / 2f, originalBitmap.height / 2f)
                }

                val transformedBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    0,
                    0,
                    originalBitmap.width,
                    originalBitmap.height,
                    matrix,
                    true
                )

                // 2. Aplicar recorte según la proporción de aspecto seleccionada
                val targetCropRatio = when {
                    state.selectedAspectRatio == CropAspectRatio.OTHER && state.customAspectRatioPair != null -> {
                        state.customAspectRatioPair.first / state.customAspectRatioPair.second
                    }
                    state.selectedAspectRatio.ratio != null -> {
                        state.selectedAspectRatio.ratio
                    }
                    else -> null
                }

                if (targetCropRatio != null) {
                    val currentRatio = transformedBitmap.width.toFloat() / transformedBitmap.height.toFloat()
                    val (cropW, cropH) = if (currentRatio > targetCropRatio) {
                        Pair((transformedBitmap.height * targetCropRatio).toInt(), transformedBitmap.height)
                    } else {
                        Pair(transformedBitmap.width, (transformedBitmap.width / targetCropRatio).toInt())
                    }
                    val cropX = ((transformedBitmap.width - cropW) / 2).coerceAtLeast(0)
                    val cropY = ((transformedBitmap.height - cropH) / 2).coerceAtLeast(0)
                    val validW = cropW.coerceIn(1, transformedBitmap.width - cropX)
                    val validH = cropH.coerceIn(1, transformedBitmap.height - cropY)
                    Bitmap.createBitmap(transformedBitmap, cropX, cropY, validW, validH)
                } else {
                    transformedBitmap
                }
            }

            // 3. Aplicar matriz de color (Brillo, Contraste, Saturación, Luces, Sombras, Warmth)
            val colorAdjustedBitmap = Bitmap.createBitmap(
                croppedBitmap.width,
                croppedBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val colorCanvas = Canvas(colorAdjustedBitmap)
            val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(state.colorMatrix.values)
            }
            colorCanvas.drawBitmap(croppedBitmap, 0f, 0f, colorPaint)

            // 4. Renderizado de expansión de lienzo solo si fue activado explícitamente por el usuario
            val baseCanvasBitmap = if (state.isCanvasEnabled && (state.canvasPaddingX > 0f || state.canvasPaddingY > 0f)) {
                val baseW = colorAdjustedBitmap.width
                val baseH = colorAdjustedBitmap.height
                val targetW = (baseW * (1f + state.canvasPaddingX)).toInt().coerceAtLeast(1)
                val targetH = (baseH * (1f + state.canvasPaddingY)).toInt().coerceAtLeast(1)

                val expandedBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                val expandedCanvas = Canvas(expandedBitmap)

                if (state.canvasBgMode == CanvasBackgroundMode.SOLID) {
                    expandedCanvas.drawColor(state.canvasSolidColor.toArgb())
                } else {
                    // Fondo con desenfoque real (Fast Gaussian Blur)
                    val blurredBg = createBlurredBackground(colorAdjustedBitmap, targetW, targetH, state.canvasBlurRadius)
                    expandedCanvas.drawBitmap(blurredBg, 0f, 0f, null)
                }

                // Centrar la imagen original sobre el lienzo
                val imgLeft = (targetW - colorAdjustedBitmap.width) / 2f
                val imgTop = (targetH - colorAdjustedBitmap.height) / 2f
                expandedCanvas.drawBitmap(colorAdjustedBitmap, imgLeft, imgTop, null)

                expandedBitmap
            } else {
                colorAdjustedBitmap
            }

            // 5. Superponer trazos de dibujo a mano alzada y stickers solo si existen (/ponytail: cero copias innecesarias)
            val hasDrawingsOrStickers = state.drawingPaths.isNotEmpty() || state.textStickers.isNotEmpty()
            val finalOutputBitmap = if (hasDrawingsOrStickers) {
                val bitmapToDrawOn = if (baseCanvasBitmap.isMutable) {
                    baseCanvasBitmap
                } else {
                    baseCanvasBitmap.copy(Bitmap.Config.ARGB_8888, true)
                }
                val drawCanvas = Canvas(bitmapToDrawOn)
                val outW = bitmapToDrawOn.width.toFloat()
                val outH = bitmapToDrawOn.height.toFloat()

                // Generar mapa pixelado si hay trazos de tipo PIXELATE
                val hasPixelateStrokes = state.drawingPaths.any { it.isPixelate }
                val pixelShader = if (hasPixelateStrokes) {
                    val block = (bitmapToDrawOn.width / 80).coerceIn(6, 18)
                    val smallW = (bitmapToDrawOn.width / block).coerceAtLeast(1)
                    val smallH = (bitmapToDrawOn.height / block).coerceAtLeast(1)
                    val small = Bitmap.createScaledBitmap(bitmapToDrawOn, smallW, smallH, false)
                    val pixelated = Bitmap.createScaledBitmap(small, bitmapToDrawOn.width, bitmapToDrawOn.height, false)
                    BitmapShader(pixelated, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                } else null

                state.drawingPaths.forEach { drawPath ->
                    val strokeWidthOnBitmap = (drawPath.strokeWidth * (outW / 400f)).coerceAtLeast(1f)

                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                        strokeWidth = strokeWidthOnBitmap

                        when {
                            drawPath.isEraser -> {
                                xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                            }
                            drawPath.isPixelate && pixelShader != null -> {
                                shader = pixelShader
                            }
                            else -> {
                                color = drawPath.color.toArgb()
                            }
                        }
                    }

                    when {
                        drawPath.points.size > 1 -> {
                            val path = Path().apply {
                                val first = drawPath.points.first()
                                moveTo(first.x * outW, first.y * outH)
                                drawPath.points.drop(1).forEach { pt ->
                                    lineTo(pt.x * outW, pt.y * outH)
                                }
                            }
                            drawCanvas.drawPath(path, strokePaint)
                        }
                        drawPath.points.isNotEmpty() -> {
                            val first = drawPath.points.first()
                            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                style = Paint.Style.FILL
                                when {
                                    drawPath.isEraser -> {
                                        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                                    }
                                    drawPath.isPixelate && pixelShader != null -> {
                                        shader = pixelShader
                                    }
                                    else -> {
                                        color = drawPath.color.toArgb()
                                    }
                                }
                            }
                            drawCanvas.drawCircle(first.x * outW, first.y * outH, strokeWidthOnBitmap / 2f, circlePaint)
                        }
                    }
                }

                // 6. Superponer capas de texto flotante (Stickers)
                state.textStickers.forEach { sticker ->
                    val posX = sticker.positionNorm.x * outW
                    val posY = sticker.positionNorm.y * outH
                    val scaledFontSize = sticker.fontSize * sticker.scale * (outW / 400f)

                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = scaledFontSize
                        isFakeBoldText = true
                        textAlign = Paint.Align.CENTER
                    }

                    val actualTextColor = when (sticker.bgStyle) {
                        com.ixeken.nierika.feature.editor.model.TextBackgroundStyle.SOLID -> {
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

                    drawCanvas.save()
                    drawCanvas.translate(posX, posY)
                    drawCanvas.rotate(sticker.rotationDegrees)

                    // Dibujar fondo estilizado si aplica
                    if (sticker.bgStyle != com.ixeken.nierika.feature.editor.model.TextBackgroundStyle.TRANSPARENT) {
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            color = when (sticker.bgStyle) {
                                com.ixeken.nierika.feature.editor.model.TextBackgroundStyle.TRANSPARENT -> android.graphics.Color.TRANSPARENT
                                com.ixeken.nierika.feature.editor.model.TextBackgroundStyle.SEMI_TRANSPARENT -> android.graphics.Color.argb(150, 0, 0, 0)
                                com.ixeken.nierika.feature.editor.model.TextBackgroundStyle.SOLID -> {
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
                        val cornerRadius = 10f * sticker.scale * (outW / 400f)
                        drawCanvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint)
                    }

                    val textY = -textBounds.exactCenterY()
                    drawCanvas.drawText(sticker.text, 0f, textY, textPaint)
                    drawCanvas.restore()
                }

                bitmapToDrawOn
            } else {
                baseCanvasBitmap
            }

            // 7. Guardar en MediaStore API como nuevo archivo de fotografía
            val fileName = "Nierika_Edit_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Nierika")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

            val outputStream: OutputStream = resolver.openOutputStream(outputUri)
                ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            finalOutputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(outputUri, contentValues, null, null)
            }

            // 6. Copiar metadatos EXIF de origen a la imagen editada
            context.copyExifMetadata(sourceUri, outputUri)

            Result.success(outputUri.toString())
        } catch (oom: OutOfMemoryError) {
            oom.printStackTrace()
            System.gc()
            Result.failure(Exception("Memoria insuficiente para exportar la imagen a máxima resolución"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun createBlurredBackground(source: Bitmap, targetW: Int, targetH: Int, radius: Float): Bitmap {
        // Mapeo proporcional de resolución para que la intensidad del desenfoque exportado coincida 1:1 con la previsualización en Compose (Modifier.blur)
        val sampleSize = 400
        val srcRatio = source.height.toFloat() / source.width.toFloat().coerceAtLeast(0.01f)
        val sampleW = sampleSize
        val sampleH = (sampleSize * srcRatio).toInt().coerceIn(50, 1000)
        val sampleBitmap = Bitmap.createScaledBitmap(source, sampleW, sampleH, true)

        // Escala normalizada del radio (1.0 a 50.0) proporcional a la cuadrícula de muestreo
        val blurRatio = (radius / 50f).coerceIn(0.02f, 1f)
        val calculatedRadius = (blurRatio * 20f).toInt().coerceIn(1, 24)

        val blurredSample = fastBlur(sampleBitmap, calculatedRadius)
        return Bitmap.createScaledBitmap(blurredSample, targetW, targetH, true)
    }

    private fun fastBlur(sentBitmap: Bitmap, radius: Int): Bitmap {
        val bitmap = sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true)
        if (radius < 1) return bitmap
        val w = bitmap.width
        val h = bitmap.height
        val pix = IntArray(w * h)
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) {
            dv[idx] = idx / divsum
        }

        yi = 0
        var yw = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        for (curY in 0 until h) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + Math.min(wm, Math.max(curI, 0))]
                sir = stack[curI + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)
                rbs = r1 - Math.abs(curI)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
            }
            stackpointer = radius

            for (curX in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curY == 0) {
                    vmin[curX] = Math.min(curX + radius + 1, wm)
                }
                p = pix[yw + vmin[curX]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = (p and 0x0000ff)

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
            }
            yw += w
        }

        for (curX in 0 until w) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            yp = -radius * w
            for (curI in -radius..radius) {
                yi = Math.max(0, yp) + curX
                sir = stack[curI + radius]
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                rbs = r1 - Math.abs(curI)
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                if (curI > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                if (curI < hm) {
                    yp += w
                }
            }
            yi = curX
            stackpointer = radius
            for (curY in 0 until h) {
                pix[yi] = (0xff000000.toInt() and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (curX == 0) {
                    vmin[curY] = Math.min(curY + r1, hm) * w
                }
                p = curX + vmin[curY]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h)
        return bitmap
    }
}
