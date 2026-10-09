package com.ixeken.nierika.feature.editor.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.editor.model.CropAspectRatio

import androidx.compose.ui.platform.LocalDensity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.StrokeJoin
import com.ixeken.nierika.feature.editor.model.CanvasBackgroundMode
import com.ixeken.nierika.feature.editor.model.DrawPathData


/**
 * Lienzo táctil interactivo en 100% Jetpack Compose para recortar, rotar, reflejar, expandir lienzo y dibujar sobre fotografías.
 * Soporta arrastre directo de esquinas, movimiento del marco, zoom con dos dedos, rotación, adaptación de lienzo y anotaciones a mano alzada.
 */
@Composable
fun NativeCropCanvas(
    mediaUri: String,
    croppedBitmap: Bitmap? = null,
    rotationDegrees: Float,
    isFlippedHorizontally: Boolean,
    isFlippedVertically: Boolean,
    aspectRatio: CropAspectRatio,
    colorMatrix: ColorMatrix? = null,
    isCropMode: Boolean = true,
    isCanvasExpansionMode: Boolean = false,
    isCanvasEnabled: Boolean = false,
    canvasPaddingX: Float = 0f,
    canvasPaddingY: Float = 0f,
    canvasBgMode: CanvasBackgroundMode = CanvasBackgroundMode.BLUR,
    canvasBlurRadius: Float = 25f,
    canvasSolidColor: Color = Color.Black,
    isDrawingMode: Boolean = false,
    drawingPaths: List<DrawPathData> = emptyList(),
    brushColor: Color = Color.Red,
    strokeWidth: Float = 10f,
    brushMode: com.ixeken.nierika.feature.editor.model.DrawBrushMode = com.ixeken.nierika.feature.editor.model.DrawBrushMode.PEN,
    isEraserActive: Boolean = (brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER),
    onAddDrawingPath: (DrawPathData) -> Unit = {},
    isTextMode: Boolean = false,
    textStickers: List<com.ixeken.nierika.feature.editor.model.TextStickerData> = emptyList(),
    onUpdateTextSticker: (com.ixeken.nierika.feature.editor.model.TextStickerData) -> Unit = {},
    onEditTextSticker: (com.ixeken.nierika.feature.editor.model.TextStickerData) -> Unit = {},
    onDeleteTextSticker: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val primaryColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current

    // Estado del marco de recorte arrastrable
    var cropRectState by remember { mutableStateOf<Rect?>(null) }
    var imageIntrinsicSize by remember(mediaUri) { mutableStateOf<androidx.compose.ui.geometry.Size?>(null) }

    // Mapa de bits pixelado para la brocha de pixelación
    val context = LocalContext.current
    var mosaicBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(mediaUri, croppedBitmap) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val bmp = croppedBitmap ?: run {
                    val uri = android.net.Uri.parse(mediaUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        android.graphics.BitmapFactory.decodeStream(stream)
                    }
                }
                if (bmp != null) {
                    val block = (bmp.width / 80).coerceIn(6, 18)
                    val w = bmp.width
                    val h = bmp.height
                    val small = Bitmap.createScaledBitmap(bmp, (w / block).coerceAtLeast(1), (h / block).coerceAtLeast(1), false)
                    val pixelated = Bitmap.createScaledBitmap(small, w, h, false)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        mosaicBitmap = pixelated
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Estado de Zoom y Desplazamiento de la imagen
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "rotationAnimation"
    )

    val animatedScaleX by animateFloatAsState(
        targetValue = if (isFlippedHorizontally) -1f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "scaleXAnimation"
    )

    val animatedScaleY by animateFloatAsState(
        targetValue = if (isFlippedVertically) -1f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "scaleYAnimation"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black)
            .pointerInput(Unit) {
                // Zoom multitáctil y desplazamiento de la imagen de fondo
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.8f, 5f)
                    panOffset += pan
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Factor de escala adaptativo para que la foto encaje al 100% en la pantalla al ser rotada a 90° / 270°
        val rotationFitScale = remember(imageIntrinsicSize, rotationDegrees, containerWidth, containerHeight) {
            val rawSize = imageIntrinsicSize
            if (rawSize == null || rawSize.width <= 0f || rawSize.height <= 0f) {
                1f
            } else {
                val isRotated90 = (kotlin.math.abs(rotationDegrees.toInt() / 90) % 2) == 1
                if (isRotated90) {
                    val imgW = rawSize.width
                    val imgH = rawSize.height
                    val s0 = minOf(containerWidth / imgW, containerHeight / imgH)
                    val dw = imgW * s0
                    val dh = imgH * s0
                    minOf(containerWidth / dh, containerHeight / dw)
                } else {
                    1f
                }
            }
        }

        val animatedRotationFitScale by animateFloatAsState(
            targetValue = rotationFitScale,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "rotationFitScaleAnimation"
        )

        // Límites geométricos exactos de la fotografía renderizada en pantalla (sin franjas negras)
        val imageBounds = remember(imageIntrinsicSize, rotationDegrees, rotationFitScale, containerWidth, containerHeight) {
            val rawSize = imageIntrinsicSize
            if (rawSize == null || rawSize.width <= 0f || rawSize.height <= 0f) {
                Rect(0f, 0f, containerWidth, containerHeight)
            } else {
                val isRotated90 = (kotlin.math.abs(rotationDegrees.toInt() / 90) % 2) == 1
                val imgW = rawSize.width
                val imgH = rawSize.height
                val s0 = minOf(containerWidth / imgW, containerHeight / imgH)
                val dw = imgW * s0
                val dh = imgH * s0

                val (dispW, dispH) = if (isRotated90) {
                    Pair(dh * rotationFitScale, dw * rotationFitScale)
                } else {
                    Pair(dw, dh)
                }

                val left = (containerWidth - dispW) / 2f
                val top = (containerHeight - dispH) / 2f
                Rect(left, top, left + dispW, top + dispH)
            }
        }

        // Límites geométricos exactos en tiempo real de la foto incluyendo transformaciones (zoom, pan, rotación)
        val effectiveImageBounds = remember(imageBounds, zoomScale, panOffset, animatedRotationFitScale) {
            val photoCenter = imageBounds.center + panOffset
            val photoWidth = imageBounds.width * zoomScale * animatedRotationFitScale
            val photoHeight = imageBounds.height * zoomScale * animatedRotationFitScale
            Rect(
                photoCenter.x - photoWidth / 2f,
                photoCenter.y - photoHeight / 2f,
                photoCenter.x + photoWidth / 2f,
                photoCenter.y + photoHeight / 2f
            )
        }

        // Inicializar o recalcular el marco de recorte limitado estrictamente a la foto
        LaunchedEffect(aspectRatio, imageBounds) {
            val availableW = imageBounds.width
            val availableH = imageBounds.height
            val targetRatio = aspectRatio.ratio

            val (rectW, rectH) = if (targetRatio != null) {
                if (availableW / availableH > targetRatio) {
                    Pair(availableH * targetRatio, availableH)
                } else {
                    Pair(availableW, availableW / targetRatio)
                }
            } else {
                Pair(availableW, availableH)
            }

            val left = imageBounds.left + (availableW - rectW) / 2f
            val top = imageBounds.top + (availableH - rectH) / 2f
            cropRectState = Rect(left, top, left + rectW, top + rectH)
        }

        // Garantizar que el marco de recorte nunca se salga de la foto visible al hacer zoom o pan
        LaunchedEffect(effectiveImageBounds) {
            cropRectState?.let { current ->
                val minSize = 100f
                val clampedLeft = current.left.coerceIn(effectiveImageBounds.left, (effectiveImageBounds.right - minSize).coerceAtLeast(effectiveImageBounds.left))
                val clampedTop = current.top.coerceIn(effectiveImageBounds.top, (effectiveImageBounds.bottom - minSize).coerceAtLeast(effectiveImageBounds.top))
                val clampedRight = current.right.coerceIn((clampedLeft + minSize).coerceAtMost(effectiveImageBounds.right), effectiveImageBounds.right)
                val clampedBottom = current.bottom.coerceIn((clampedTop + minSize).coerceAtMost(effectiveImageBounds.bottom), effectiveImageBounds.bottom)
                if (clampedLeft != current.left || clampedTop != current.top || clampedRight != current.right || clampedBottom != current.bottom) {
                    cropRectState = Rect(clampedLeft, clampedTop, clampedRight, clampedBottom)
                }
            }
        }

        val currentCropRect = cropRectState ?: imageBounds

        val context = LocalContext.current
        val imageRequest = remember(mediaUri, croppedBitmap) {
            ImageRequest.Builder(context)
                .data(croppedBitmap ?: mediaUri)
                .crossfade(true)
                .build()
        }

        val isCanvasActive = (isCanvasExpansionMode || isCanvasEnabled) && (canvasPaddingX > 0f || canvasPaddingY > 0f)

        if (isCanvasActive) {
            // Renderizado del lienzo extendido por ejes X e Y
            val rawW = imageBounds.width.coerceAtLeast(1f)
            val rawH = imageBounds.height.coerceAtLeast(1f)
            val totalW = rawW * (1f + canvasPaddingX)
            val totalH = rawH * (1f + canvasPaddingY)
            val scaleToFit = minOf(containerWidth / totalW, containerHeight / totalH).coerceAtLeast(0.01f)
            val canvasW = totalW * scaleToFit
            val canvasH = totalH * scaleToFit

            Box(
                modifier = Modifier
                    .size(with(density) { canvasW.toDp() }, with(density) { canvasH.toDp() })
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                // Fondo extendido (Desenfoque Gaussiano o Color Sólido)
                when (canvasBgMode) {
                    CanvasBackgroundMode.BLUR -> {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(canvasBlurRadius.dp)
                        )
                    }

                    CanvasBackgroundMode.SOLID -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(canvasSolidColor)
                        )
                    }
                }

                // Fotografía principal posicionada en el centro preservando proporciones completas
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                    onSuccess = { state ->
                        imageIntrinsicSize = androidx.compose.ui.geometry.Size(
                            state.result.image.width.toFloat(),
                            state.result.image.height.toFloat()
                        )
                    },
                    modifier = Modifier
                        .size(with(density) { (rawW * scaleToFit).toDp() }, with(density) { (rawH * scaleToFit).toDp() })
                        .graphicsLayer {
                            this.rotationZ = animatedRotation
                            this.scaleX = animatedScaleX * zoomScale * animatedRotationFitScale
                            this.scaleY = animatedScaleY * zoomScale * animatedRotationFitScale
                            this.translationX = panOffset.x
                            this.translationY = panOffset.y
                        }
                )
            }
        } else {
            // Imagen principal con transformaciones visuales avanzadas y filtro de matriz de color
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = colorMatrix?.let { ColorFilter.colorMatrix(it) },
                onSuccess = { state ->
                    imageIntrinsicSize = androidx.compose.ui.geometry.Size(
                        state.result.image.width.toFloat(),
                        state.result.image.height.toFloat()
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.rotationZ = animatedRotation
                        this.scaleX = animatedScaleX * zoomScale * animatedRotationFitScale
                        this.scaleY = animatedScaleY * zoomScale * animatedRotationFitScale
                        this.translationX = panOffset.x
                        this.translationY = panOffset.y
                    }
            )

            // Superposición táctil de recorte con rejilla de 3x3 y detectores de arrastre (solo en modo Crop)
            if (isCropMode) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(containerWidth, containerHeight, aspectRatio, imageBounds) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val downPos = down.position
                                val rect = cropRectState ?: return@awaitEachGesture
                                val handleRadius = with(density) { 44.dp.toPx() }

                                // Determinar el controlador activo según el punto inicial de toque
                                val activeHandle = detectCropDragHandle(
                                    downPos = downPos,
                                    cropRect = rect,
                                    handleRadius = handleRadius
                                )

                                var previousCentroid = Offset.Zero
                                var previousDistance = 0f
                                var isMultiTouchTransforming = false

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressedPointers = event.changes.filter { it.pressed }
                                    if (pressedPointers.isEmpty()) break

                                    when {
                                        // Gesto con 2 o más dedos: Zoom (Pinch) y desplazamiento (Pan)
                                        pressedPointers.size >= 2 -> {
                                            val p1 = pressedPointers[0]
                                            val p2 = pressedPointers[1]
                                            val currentCentroid = (p1.position + p2.position) / 2f
                                            val currentDistance = (p1.position - p2.position).getDistance()

                                            if (!isMultiTouchTransforming) {
                                                isMultiTouchTransforming = true
                                                previousCentroid = currentCentroid
                                                previousDistance = currentDistance
                                            } else {
                                                if (previousDistance > 0f && currentDistance > 0f) {
                                                    val zoomRatio = currentDistance / previousDistance
                                                    zoomScale = (zoomScale * zoomRatio).coerceIn(1.0f, 6.0f)
                                                }
                                                val panDelta = currentCentroid - previousCentroid
                                                panOffset += panDelta

                                                previousCentroid = currentCentroid
                                                previousDistance = currentDistance
                                            }
                                            pressedPointers.forEach { it.consume() }
                                        }

                                        // Gesto con 1 dedo: manipular esquinas o desplazar el marco de recorte
                                        pressedPointers.size == 1 && !isMultiTouchTransforming -> {
                                            val pointer = pressedPointers.first()
                                            if (activeHandle != CropDragHandle.NONE) {
                                                val dragAmount = pointer.positionChange()
                                                if (dragAmount != Offset.Zero) {
                                                    pointer.consume()
                                                    cropRectState = calculateNewCropRect(
                                                        current = cropRectState!!,
                                                        dragAmount = dragAmount,
                                                        handle = activeHandle,
                                                        aspectRatio = aspectRatio.ratio,
                                                        bounds = effectiveImageBounds
                                                    )
                                                }
                                            } else if (zoomScale > 1.05f) {
                                                // Si la imagen está ampliada y se toca fuera de los manejadores, permitir arrastrar la foto
                                                val dragAmount = pointer.positionChange()
                                                if (dragAmount != Offset.Zero) {
                                                    pointer.consume()
                                                    val maxPanX = maxOf(0f, (effectiveImageBounds.width - imageBounds.width) / 2f)
                                                    val maxPanY = maxOf(0f, (effectiveImageBounds.height - imageBounds.height) / 2f)
                                                    panOffset = Offset(
                                                        (panOffset.x + dragAmount.x).coerceIn(-maxPanX, maxPanX),
                                                        (panOffset.y + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    drawCropOverlay(
                        currentCropRect = currentCropRect,
                        primaryColor = primaryColor
                    )
                }
            }
        }

        // Capa de dibujo a mano alzada y anotaciones (Fase F5)
        val activePoints = remember { mutableStateListOf<Offset>() }
        val targetBounds = if (isCanvasActive) {
            val rawW = imageBounds.width.coerceAtLeast(1f)
            val rawH = imageBounds.height.coerceAtLeast(1f)
            val totalW = rawW * (1f + canvasPaddingX)
            val totalH = rawH * (1f + canvasPaddingY)
            val scaleToFit = minOf(containerWidth / totalW, containerHeight / totalH).coerceAtLeast(0.01f)
            val canvasW = totalW * scaleToFit
            val canvasH = totalH * scaleToFit
            val cLeft = (containerWidth - canvasW) / 2f
            val cTop = (containerHeight - canvasH) / 2f
            Rect(cLeft, cTop, cLeft + canvasW, cTop + canvasH)
        } else {
            imageBounds
        }

        val pixelShaderBrush = remember(mosaicBitmap, targetBounds) {
            mosaicBitmap?.let { bmp ->
                val shader = android.graphics.BitmapShader(bmp, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
                val matrix = android.graphics.Matrix()
                matrix.postScale(targetBounds.width / bmp.width.toFloat(), targetBounds.height / bmp.height.toFloat())
                matrix.postTranslate(targetBounds.left, targetBounds.top)
                shader.setLocalMatrix(matrix)
                androidx.compose.ui.graphics.ShaderBrush(shader)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.rotationZ = animatedRotation
                    this.scaleX = animatedScaleX * zoomScale * animatedRotationFitScale
                    this.scaleY = animatedScaleY * zoomScale * animatedRotationFitScale
                    this.translationX = panOffset.x
                    this.translationY = panOffset.y
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .then(
                        if (isDrawingMode) {
                            Modifier.pointerInput(brushMode, isEraserActive, brushColor, strokeWidth, targetBounds) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (targetBounds.contains(offset)) {
                                            activePoints.clear()
                                            activePoints.add(offset)
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        if (activePoints.isNotEmpty()) {
                                            change.consume()
                                            val clamped = Offset(
                                                change.position.x.coerceIn(targetBounds.left, targetBounds.right),
                                                change.position.y.coerceIn(targetBounds.top, targetBounds.bottom)
                                            )
                                            activePoints.add(clamped)
                                        }
                                    },
                                    onDragEnd = {
                                        if (activePoints.isNotEmpty()) {
                                            val normPoints = activePoints.map { pt ->
                                                Offset(
                                                    x = ((pt.x - targetBounds.left) / targetBounds.width).coerceIn(0f, 1f),
                                                    y = ((pt.y - targetBounds.top) / targetBounds.height).coerceIn(0f, 1f)
                                                )
                                            }
                                            onAddDrawingPath(
                                                DrawPathData(
                                                    points = normPoints,
                                                    color = if (brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER) Color.Transparent else brushColor,
                                                    strokeWidth = strokeWidth,
                                                    brushMode = brushMode
                                                )
                                            )
                                            activePoints.clear()
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
                clipRect(
                    left = targetBounds.left,
                    top = targetBounds.top,
                    right = targetBounds.right,
                    bottom = targetBounds.bottom
                ) {
                    // Renderizar todos los trazos guardados en la pila normalizados
                    drawingPaths.forEach { drawPath ->
                        val strokePx = (drawPath.strokeWidth * (targetBounds.width / 400f)).coerceAtLeast(1f)
                        when {
                            drawPath.points.size > 1 -> {
                                val p = Path().apply {
                                    val first = drawPath.points.first()
                                    moveTo(targetBounds.left + first.x * targetBounds.width, targetBounds.top + first.y * targetBounds.height)
                                    drawPath.points.drop(1).forEach { pt ->
                                        lineTo(targetBounds.left + pt.x * targetBounds.width, targetBounds.top + pt.y * targetBounds.height)
                                    }
                                }

                                when {
                                    drawPath.isEraser -> {
                                        drawPath(
                                            path = p,
                                            color = Color.Black,
                                            style = Stroke(
                                                width = strokePx * 1.5f,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            ),
                                            blendMode = BlendMode.Clear
                                        )
                                    }
                                    drawPath.isPixelate && pixelShaderBrush != null -> {
                                        drawPath(
                                            path = p,
                                            brush = pixelShaderBrush,
                                            style = Stroke(
                                                width = strokePx,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                    else -> {
                                        drawPath(
                                            path = p,
                                            color = if (drawPath.isPixelate) Color.Gray.copy(alpha = 0.8f) else drawPath.color,
                                            style = Stroke(
                                                width = strokePx,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                }
                            }
                            drawPath.points.isNotEmpty() -> {
                                val first = drawPath.points.first()
                                val center = Offset(targetBounds.left + first.x * targetBounds.width, targetBounds.top + first.y * targetBounds.height)
                                when {
                                    drawPath.isEraser -> {
                                        drawCircle(
                                            color = Color.Black,
                                            radius = strokePx / 2f,
                                            center = center,
                                            blendMode = BlendMode.Clear
                                        )
                                    }
                                    drawPath.isPixelate && pixelShaderBrush != null -> {
                                        drawCircle(
                                            brush = pixelShaderBrush,
                                            radius = strokePx / 2f,
                                            center = center
                                        )
                                    }
                                    else -> {
                                        drawCircle(
                                            color = if (drawPath.isPixelate) Color.Gray.copy(alpha = 0.8f) else drawPath.color,
                                            radius = strokePx / 2f,
                                            center = center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Renderizar el trazo activo en tiempo real mientras el usuario arrastra el dedo
                    val liveStrokePx = (strokeWidth * (targetBounds.width / 400f)).coerceAtLeast(1f)
                    when {
                        activePoints.size > 1 -> {
                            val livePath = Path().apply {
                                moveTo(activePoints.first().x, activePoints.first().y)
                                activePoints.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            when {
                                brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.PIXELATE && pixelShaderBrush != null -> {
                                    drawPath(
                                        path = livePath,
                                        brush = pixelShaderBrush,
                                        style = Stroke(
                                            width = liveStrokePx,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                                brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER -> {
                                    drawPath(
                                        path = livePath,
                                        color = Color.Black,
                                        style = Stroke(
                                            width = liveStrokePx * 1.5f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        ),
                                        blendMode = BlendMode.Clear
                                    )
                                }
                                else -> {
                                    drawPath(
                                        path = livePath,
                                        color = brushColor,
                                        style = Stroke(
                                            width = liveStrokePx,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                        activePoints.isNotEmpty() -> {
                            when {
                                brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.PIXELATE && pixelShaderBrush != null -> {
                                    drawCircle(
                                        brush = pixelShaderBrush,
                                        radius = liveStrokePx / 2f,
                                        center = activePoints.first()
                                    )
                                }
                                brushMode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER -> {
                                    drawCircle(
                                        color = Color.Black,
                                        radius = (liveStrokePx * 1.5f) / 2f,
                                        center = activePoints.first(),
                                        blendMode = BlendMode.Clear
                                    )
                                }
                                else -> {
                                    drawCircle(
                                        color = brushColor,
                                        radius = liveStrokePx / 2f,
                                        center = activePoints.first()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Capa de textos flotantes interactivos (Estilo Stories)
            if (textStickers.isNotEmpty() || isTextMode) {
                TextStickerOverlay(
                    stickers = textStickers,
                    bounds = targetBounds,
                    isTextMode = isTextMode,
                    onUpdateSticker = onUpdateTextSticker,
                    onEditSticker = onEditTextSticker,
                    onDeleteSticker = onDeleteTextSticker
                )
            }
        }
    }
}

