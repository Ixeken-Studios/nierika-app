package com.ixeken.nierika.feature.editor.components

import android.net.Uri
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.editor.model.CropAspectRatio
import com.ixeken.nierika.feature.editor.model.DrawBrushMode
import com.ixeken.nierika.feature.editor.model.DrawPathData
import com.ixeken.nierika.feature.editor.model.TextStickerData

/**
 * Superficie inmersiva interactiva del reproductor de video para el editor multimedia M3 Expressive.
 * Soporta transformaciones espaciales en tiempo real (rotación 90°, reflejo horizontal/vertical),
 * renderEffect por hardware para ajustes de color, y marco interactivo de recorte (Crop) táctil con esquinas arrastrables y rejilla 3x3.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoEditorCanvas(
    videoUri: String,
    isPlaying: Boolean,
    rotationDegrees: Float = 0f,
    isFlippedHorizontally: Boolean = false,
    isFlippedVertically: Boolean = false,
    aspectRatio: CropAspectRatio = CropAspectRatio.FREE,
    colorMatrix: ColorMatrix? = null,
    isCropMode: Boolean = false,
    trimStartMs: Long = 0L,
    trimEndMs: Long = 0L,
    isDrawingMode: Boolean = false,
    drawingPaths: List<DrawPathData> = emptyList(),
    brushColor: Color = Color.Red,
    strokeWidth: Float = 12f,
    brushMode: DrawBrushMode = DrawBrushMode.PEN,
    isEraserActive: Boolean = false,
    onAddDrawingPath: (DrawPathData) -> Unit = {},
    isTextMode: Boolean = false,
    textStickers: List<TextStickerData> = emptyList(),
    onUpdateTextSticker: (TextStickerData) -> Unit = {},
    onEditTextSticker: (TextStickerData) -> Unit = {},
    onDeleteTextSticker: (String) -> Unit = {},
    volume: Float = 1.0f,
    isMuted: Boolean = false,
    speed: Float = 1.0f,
    seekPositionMs: Long? = null,
    onDurationDiscovered: (Long) -> Unit = {},
    onPositionDiscovered: (Long) -> Unit = {},
    onPlayerReady: (ExoPlayer) -> Unit = {},
    videoCropBounds: Rect? = null,
    onCropBoundsChanged: (Rect?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val primaryColor = MaterialTheme.colorScheme.primary

    // Estado del marco de recorte arrastrable
    var cropRectState by remember { mutableStateOf<Rect?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var videoIntrinsicSize by remember(videoUri) { mutableStateOf<Size?>(null) }

    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUri)))
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        onDurationDiscovered(duration.coerceAtLeast(0L))
                        val curSize = videoSize
                        if (videoIntrinsicSize == null && curSize.width > 0 && curSize.height > 0) {
                            val rotation = curSize.unappliedRotationDegrees
                            val isRotated90 = rotation == 90 || rotation == 270
                            val w = if (isRotated90) curSize.height else curSize.width
                            val h = if (isRotated90) curSize.width else curSize.height
                            val pixelRatio = if (curSize.pixelWidthHeightRatio > 0f) curSize.pixelWidthHeightRatio else 1f
                            videoIntrinsicSize = Size(w * pixelRatio, h.toFloat())
                        }
                    }
                }

                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                    if (duration > 0L) {
                        onDurationDiscovered(duration)
                    }
                }

                override fun onVideoSizeChanged(newVideoSize: androidx.media3.common.VideoSize) {
                    if (newVideoSize.width > 0 && newVideoSize.height > 0) {
                        val rotation = newVideoSize.unappliedRotationDegrees
                        val isRotated90 = rotation == 90 || rotation == 270
                        val w = if (isRotated90) newVideoSize.height else newVideoSize.width
                        val h = if (isRotated90) newVideoSize.width else newVideoSize.height
                        val pixelRatio = if (newVideoSize.pixelWidthHeightRatio > 0f) newVideoSize.pixelWidthHeightRatio else 1f
                        videoIntrinsicSize = Size(w * pixelRatio, h.toFloat())
                    }
                }
            })
            prepare()
            if (duration > 0L) {
                onDurationDiscovered(duration)
            }
            onPlayerReady(this)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(volume, isMuted) {
        player.volume = if (isMuted) 0f else volume
    }

    LaunchedEffect(speed) {
        player.setPlaybackSpeed(speed)
    }

    LaunchedEffect(seekPositionMs) {
        seekPositionMs?.let { pos ->
            player.seekTo(pos)
            onPositionDiscovered(pos)
        }
    }

    LaunchedEffect(player, isPlaying, trimStartMs, trimEndMs) {
        while (true) {
            if (player.isPlaying) {
                val currentPos = player.currentPosition
                onPositionDiscovered(currentPos)
                if (trimEndMs > 0 && currentPos >= trimEndMs) {
                    player.seekTo(trimStartMs)
                }
            }
            kotlinx.coroutines.delay(40)
        }
    }

    DisposableEffect(isPlaying) {
        if (isPlaying) {
            player.play()
        } else {
            player.pause()
        }
        onDispose { }
    }

    val composeRenderEffect = remember(colorMatrix) {
        val cm: ColorMatrix? = colorMatrix
        if (cm != null) {
            android.graphics.RenderEffect.createColorFilterEffect(
                android.graphics.ColorMatrixColorFilter(cm.values)
            ).asComposeRenderEffect()
        } else null
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "videoRotation"
    )

    val animatedScaleX by animateFloatAsState(
        targetValue = if (isFlippedHorizontally) -1f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "videoScaleX"
    )

    val animatedScaleY by animateFloatAsState(
        targetValue = if (isFlippedVertically) -1f else 1f,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "videoScaleY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Factor de escala para encajar video rotado 90° o 270°
        val rotationFitScale = remember(videoIntrinsicSize, rotationDegrees, containerWidth, containerHeight) {
            val rawSize = videoIntrinsicSize
            if (rawSize == null || rawSize.width <= 0f || rawSize.height <= 0f) {
                1f
            } else {
                val isRotated90 = (kotlin.math.abs(rotationDegrees.toInt() / 90) % 2) == 1
                if (isRotated90) {
                    val vidW = rawSize.width
                    val vidH = rawSize.height
                    val s0 = minOf(containerWidth / vidW, containerHeight / vidH)
                    val dw = vidW * s0
                    val dh = vidH * s0
                    minOf(containerWidth / dh, containerHeight / dw)
                } else {
                    1f
                }
            }
        }

        val animatedRotationFitScale by animateFloatAsState(
            targetValue = rotationFitScale,
            animationSpec = motionScheme.defaultSpatialSpec(),
            label = "videoRotationFitScale"
        )

        // Límites geométricos exactos del video en pantalla
        val videoBounds = remember(videoIntrinsicSize, rotationDegrees, rotationFitScale, containerWidth, containerHeight) {
            val rawSize = videoIntrinsicSize
            if (rawSize == null || rawSize.width <= 0f || rawSize.height <= 0f) {
                Rect(0f, 0f, containerWidth, containerHeight)
            } else {
                val isRotated90 = (kotlin.math.abs(rotationDegrees.toInt() / 90) % 2) == 1
                val vidW = rawSize.width
                val vidH = rawSize.height
                val s0 = minOf(containerWidth / vidW, containerHeight / vidH)
                val dw = vidW * s0
                val dh = vidH * s0
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

        val baseVidW = videoIntrinsicSize?.let {
            val s0 = minOf(containerWidth / it.width, containerHeight / it.height)
            it.width * s0
        } ?: containerWidth

        val baseVidH = videoIntrinsicSize?.let {
            val s0 = minOf(containerWidth / it.width, containerHeight / it.height)
            it.height * s0
        } ?: containerHeight

        // Reiniciar zoom y desplazamiento de manipulación táctil al salir del modo recorte
        LaunchedEffect(isCropMode) {
            if (!isCropMode) {
                zoomScale = 1f
                panOffset = Offset.Zero
            }
        }

        // Inicializar o recalcular el marco de recorte según la proporción elegida y los límites del video
        LaunchedEffect(isCropMode, aspectRatio, videoBounds) {
            if (!isCropMode && cropRectState != null) return@LaunchedEffect

            val targetRatio = aspectRatio.ratio
            val (rectW, rectH) = if (targetRatio != null) {
                if (videoBounds.width / videoBounds.height > targetRatio) {
                    Pair(videoBounds.height * targetRatio, videoBounds.height)
                } else {
                    Pair(videoBounds.width, videoBounds.width / targetRatio)
                }
            } else {
                if (videoCropBounds != null) {
                    Pair(videoCropBounds.width * videoBounds.width, videoCropBounds.height * videoBounds.height)
                } else {
                    Pair(videoBounds.width, videoBounds.height)
                }
            }

            val left = if (videoCropBounds != null && targetRatio == null) {
                videoBounds.left + videoCropBounds.left * videoBounds.width
            } else {
                videoBounds.left + (videoBounds.width - rectW) / 2f
            }

            val top = if (videoCropBounds != null && targetRatio == null) {
                videoBounds.top + videoCropBounds.top * videoBounds.height
            } else {
                videoBounds.top + (videoBounds.height - rectH) / 2f
            }

            val newCrop = Rect(left, top, left + rectW, top + rectH)
            cropRectState = newCrop
            if (isCropMode && videoBounds.width > 0f && videoBounds.height > 0f) {
                onCropBoundsChanged(
                    Rect(
                        ((newCrop.left - videoBounds.left) / videoBounds.width).coerceIn(0f, 1f),
                        ((newCrop.top - videoBounds.top) / videoBounds.height).coerceIn(0f, 1f),
                        ((newCrop.right - videoBounds.left) / videoBounds.width).coerceIn(0f, 1f),
                        ((newCrop.bottom - videoBounds.top) / videoBounds.height).coerceIn(0f, 1f)
                    )
                )
            }
        }

        val currentCropRect = cropRectState ?: videoBounds

        // Cálculo geométrico de previsualización WYSIWYG para recorte confirmado
        val isRotated90 = (kotlin.math.abs(rotationDegrees.toInt() / 90) % 2) == 1
        val dispW = if (isRotated90) baseVidH * rotationFitScale else baseVidW
        val dispH = if (isRotated90) baseVidW * rotationFitScale else baseVidH

        val isCroppedPreview = !isCropMode && videoCropBounds != null &&
            (videoCropBounds.left > 0.005f || videoCropBounds.top > 0.005f || videoCropBounds.right < 0.995f || videoCropBounds.bottom < 0.995f)

        val cropPixelW = if (isCroppedPreview) (dispW * videoCropBounds.width).coerceAtLeast(10f) else dispW
        val cropPixelH = if (isCroppedPreview) (dispH * videoCropBounds.height).coerceAtLeast(10f) else dispH
        val cropFitScale = if (isCroppedPreview && cropPixelW > 0f && cropPixelH > 0f) {
            minOf(containerWidth / cropPixelW, containerHeight / cropPixelH)
        } else 1f

        val displayBoxW = cropPixelW * cropFitScale
        val displayBoxH = cropPixelH * cropFitScale

        val cropCenterX = if (isCroppedPreview) (videoCropBounds.left + videoCropBounds.right) / 2f else 0.5f
        val cropCenterY = if (isCroppedPreview) (videoCropBounds.top + videoCropBounds.bottom) / 2f else 0.5f
        val cropOffsetX = (0.5f - cropCenterX) * (dispW * cropFitScale)
        val cropOffsetY = (0.5f - cropCenterY) * (dispH * cropFitScale)

        // Superficie de renderizado del video con soporte WYSIWYG de recorte
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = with(LocalDensity.current) { displayBoxW.toDp() },
                        height = with(LocalDensity.current) { displayBoxH.toDp() }
                    )
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            player.setVideoTextureView(this)
                        }
                    },
                    modifier = Modifier
                        .requiredSize(
                            width = with(LocalDensity.current) { (baseVidW * cropFitScale).toDp() },
                            height = with(LocalDensity.current) { (baseVidH * cropFitScale).toDp() }
                        )
                        .graphicsLayer {
                            this.rotationZ = animatedRotation
                            this.scaleX = animatedScaleX * zoomScale * animatedRotationFitScale
                            this.scaleY = animatedScaleY * zoomScale * animatedRotationFitScale
                            this.translationX = if (isCropMode) panOffset.x else cropOffsetX
                            this.translationY = if (isCropMode) panOffset.y else cropOffsetY
                            this.renderEffect = composeRenderEffect
                        }
                )
            }
        }

        // Capa táctil interactiva para recortar y mover la cuadrícula del video
        if (isCropMode) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(aspectRatio, videoBounds) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downPos = down.position
                            val cropRect = cropRectState ?: videoBounds
                            val handleRadius = 48.dp.toPx()
                            val edgeThreshold = 32.dp.toPx()

                            val activeHandle = detectCropDragHandle(
                                downPos = downPos,
                                cropRect = cropRect,
                                handleRadius = handleRadius,
                                edgeThreshold = edgeThreshold
                            )

                            var isMultiTouch = false
                            var prevCentroid = Offset.Zero
                            var prevDistance = 0f

                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedPointers = event.changes.filter { it.pressed }
                                if (pressedPointers.isEmpty()) break

                                when {
                                    pressedPointers.size >= 2 -> {
                                        val p1 = pressedPointers[0]
                                        val p2 = pressedPointers[1]
                                        val centroid = (p1.position + p2.position) / 2f
                                        val distance = (p1.position - p2.position).getDistance()

                                        if (!isMultiTouch) {
                                            isMultiTouch = true
                                            prevCentroid = centroid
                                            prevDistance = distance
                                        } else {
                                            if (prevDistance > 0f && distance > 0f) {
                                                val zoomRatio = distance / prevDistance
                                                zoomScale = (zoomScale * zoomRatio).coerceIn(1.0f, 5.0f)
                                            }
                                            val panDelta = centroid - prevCentroid
                                            panOffset += panDelta
                                            prevCentroid = centroid
                                            prevDistance = distance
                                        }
                                        pressedPointers.forEach { it.consume() }
                                    }

                                    pressedPointers.size == 1 && !isMultiTouch -> {
                                        val pointer = pressedPointers.first()
                                        val dragDelta = pointer.positionChange()
                                        if (dragDelta != Offset.Zero) {
                                            pointer.consume()
                                            cropRectState?.let { current ->
                                                val newCrop = calculateNewCropRect(
                                                    current = current,
                                                    dragAmount = dragDelta,
                                                    handle = activeHandle,
                                                    aspectRatio = aspectRatio.ratio,
                                                    bounds = videoBounds
                                                )
                                                cropRectState = newCrop
                                                if (videoBounds.width > 0f && videoBounds.height > 0f) {
                                                    onCropBoundsChanged(
                                                        Rect(
                                                            ((newCrop.left - videoBounds.left) / videoBounds.width).coerceIn(0f, 1f),
                                                            ((newCrop.top - videoBounds.top) / videoBounds.height).coerceIn(0f, 1f),
                                                            ((newCrop.right - videoBounds.left) / videoBounds.width).coerceIn(0f, 1f),
                                                            ((newCrop.bottom - videoBounds.top) / videoBounds.height).coerceIn(0f, 1f)
                                                        )
                                                    )
                                                }
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

        // Capa de dibujo y textos flotantes interactivos sobre el video
        val activePoints = remember { mutableStateListOf<Offset>() }
        val previewBounds = remember(isCroppedPreview, displayBoxW, displayBoxH, containerWidth, containerHeight, videoBounds) {
            if (isCroppedPreview) {
                val left = (containerWidth - displayBoxW) / 2f
                val top = (containerHeight - displayBoxH) / 2f
                Rect(left, top, left + displayBoxW, top + displayBoxH)
            } else {
                videoBounds
            }
        }

        val targetBounds = if (isCropMode && cropRectState != null) {
            cropRectState!!
        } else {
            previewBounds
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.rotationZ = animatedRotation
                    this.scaleX = animatedScaleX * zoomScale
                    this.scaleY = animatedScaleY * zoomScale
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
                                                    color = if (brushMode == DrawBrushMode.ERASER) Color.Transparent else brushColor,
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
                    // Renderizar trazos guardados
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

                                if (drawPath.isEraser) {
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
                                } else {
                                    drawPath(
                                        path = p,
                                        color = drawPath.color,
                                        style = Stroke(
                                            width = strokePx,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                            drawPath.points.isNotEmpty() -> {
                                val first = drawPath.points.first()
                                val center = Offset(targetBounds.left + first.x * targetBounds.width, targetBounds.top + first.y * targetBounds.height)
                                if (drawPath.isEraser) {
                                    drawCircle(
                                        color = Color.Black,
                                        radius = strokePx / 2f,
                                        center = center,
                                        blendMode = BlendMode.Clear
                                    )
                                } else {
                                    drawCircle(
                                        color = drawPath.color,
                                        radius = strokePx / 2f,
                                        center = center
                                    )
                                }
                            }
                        }
                    }

                    // Renderizar trazo activo en tiempo real
                    val liveStrokePx = (strokeWidth * (targetBounds.width / 400f)).coerceAtLeast(1f)
                    when {
                        activePoints.size > 1 -> {
                            val livePath = Path().apply {
                                moveTo(activePoints.first().x, activePoints.first().y)
                                activePoints.drop(1).forEach { lineTo(it.x, it.y) }
                            }
                            if (brushMode == DrawBrushMode.ERASER) {
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
                            } else {
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
                        activePoints.isNotEmpty() -> {
                            if (brushMode == DrawBrushMode.ERASER) {
                                drawCircle(
                                    color = Color.Black,
                                    radius = (liveStrokePx * 1.5f) / 2f,
                                    center = activePoints.first(),
                                    blendMode = BlendMode.Clear
                                )
                            } else {
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

            // Capa de textos flotantes interactivos sobre el video
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

