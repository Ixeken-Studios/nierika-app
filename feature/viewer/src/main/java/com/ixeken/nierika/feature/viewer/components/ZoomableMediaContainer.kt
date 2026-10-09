package com.ixeken.nierika.feature.viewer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntSize
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Contenedor desacoplado de gestos de medios para fotos y videos en el visor de Nierika.
 *
 * Encapsula la gestión de transformaciones espaciales a 120Hz mediante lectura diferida
 * en graphicsLayer, eliminando recomposiciones innecesarias.
 *
 * Resuelve la colisión de eventos mediante touchSlop para que el doble toque de zoom
 * responda con 100% de fiabilidad, e implementa anclaje directo 1:1 con Animatable.snapTo.
 */
@Composable
fun ZoomableMediaContainer(
    modifier: Modifier = Modifier,
    isCurrentPage: Boolean = true,
    onToggleControls: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onHoldStart: (() -> Unit)? = null,
    onHoldEnd: (() -> Unit)? = null,
    content: @Composable BoxScope.(graphicsModifier: Modifier) -> Unit
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val coroutineScope = rememberCoroutineScope()
    val viewConfig = LocalViewConfiguration.current
    val haptic = LocalHapticFeedback.current

    val scaleAnim = remember { Animatable(1f) }
    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Restablecer transformaciones inmediatamente al cambiar de elemento en el visor
    LaunchedEffect(isCurrentPage) {
        if (!isCurrentPage && (scaleAnim.value != 1f || offsetAnim.value != Offset.Zero)) {
            scaleAnim.snapTo(1f)
            offsetAnim.snapTo(Offset.Zero)
            onZoomStateChanged(false)
        }
    }

    val graphicsModifier = Modifier.graphicsLayer {
        scaleX = scaleAnim.value
        scaleY = scaleAnim.value
        translationX = offsetAnim.value.x
        translationY = offsetAnim.value.y
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        coroutineScope.launch {
                            if (scaleAnim.value > 1.05f) {
                                onZoomStateChanged(false)
                                launch { scaleAnim.animateTo(1f, motionScheme.fastSpatialSpec()) }
                                launch { offsetAnim.animateTo(Offset.Zero, motionScheme.fastSpatialSpec()) }
                            } else {
                                onZoomStateChanged(true)
                                val targetScale = 2.5f
                                val center = Offset(containerSize.width / 2f, containerSize.height / 2f)
                                val maxX = ((targetScale - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                                val maxY = ((targetScale - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                                val targetOffset = Offset(
                                    x = ((center.x - tapOffset.x) * (targetScale - 1f)).coerceIn(-maxX, maxX),
                                    y = ((center.y - tapOffset.y) * (targetScale - 1f)).coerceIn(-maxY, maxY)
                                )
                                launch { scaleAnim.animateTo(targetScale, motionScheme.fastSpatialSpec()) }
                                launch { offsetAnim.animateTo(targetOffset, motionScheme.fastSpatialSpec()) }
                            }
                        }
                    },
                    onTap = {
                        onToggleControls()
                    }
                )
            }
            .pointerInput(onHoldStart, onHoldEnd) {
                val touchSlop = viewConfig.touchSlop
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var initialPinchScale: Float? = null
                    var hasTriggeredDismiss = false
                    var isPanStarted = false
                    var panAccumulator = Offset.Zero
                    var isHolding = false

                    val holdJob = if (onHoldStart != null) {
                        coroutineScope.launch {
                            delay(500L)
                            isHolding = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHoldStart()
                        }
                    } else null

                    do {
                        val event = awaitPointerEvent()
                        val activePointers = event.changes.filter { it.pressed }

                        if (activePointers.size >= 2) {
                            holdJob?.cancel()
                        }

                        when {
                            activePointers.size >= 2 -> {
                                isPanStarted = false
                                panAccumulator = Offset.Zero

                                if (initialPinchScale == null) {
                                    initialPinchScale = scaleAnim.value
                                }

                                val centroid = activePointers
                                    .map { it.position }
                                    .reduce { acc, pos -> acc + pos } / activePointers.size.toFloat()
                                val previousCentroid = activePointers
                                    .map { it.previousPosition }
                                    .reduce { acc, pos -> acc + pos } / activePointers.size.toFloat()

                                val pan = centroid - previousCentroid

                                val p0 = activePointers[0]
                                val p1 = activePointers[1]
                                val currentDist = (p0.position - p1.position).getDistance()
                                val previousDist = (p0.previousPosition - p1.previousPosition).getDistance()

                                val zoom = if (previousDist > 1f) {
                                    (currentDist / previousDist).coerceIn(0.5f, 2f)
                                } else 1f

                                val minScaleAllowed = when {
                                    (initialPinchScale ?: 1f) <= 1.05f -> 0.65f
                                    else -> 0.85f
                                }
                                val newScale = (scaleAnim.value * zoom).coerceIn(minScaleAllowed, 10.0f)
                                coroutineScope.launch { scaleAnim.snapTo(newScale) }

                                val zoomed = newScale > 1.02f
                                onZoomStateChanged(zoomed)

                                if (zoomed) {
                                    val maxX = ((newScale - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                                    val maxY = ((newScale - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                                    val newOffset = Offset(
                                        x = (offsetAnim.value.x + pan.x).coerceIn(-maxX * 1.3f, maxX * 1.3f),
                                        y = (offsetAnim.value.y + pan.y).coerceIn(-maxY * 1.3f, maxY * 1.3f)
                                    )
                                    coroutineScope.launch { offsetAnim.snapTo(newOffset) }
                                }

                                if ((initialPinchScale ?: 1f) <= 1.05f && newScale < 0.85f && !hasTriggeredDismiss) {
                                    hasTriggeredDismiss = true
                                    onDismiss()
                                }

                                event.changes.forEach { it.consume() }
                            }
                            activePointers.size == 1 && scaleAnim.value > 1.02f -> {
                                val change = activePointers.first()
                                val pan = change.position - change.previousPosition

                                if (!isPanStarted) {
                                    panAccumulator += pan
                                    if (panAccumulator.getDistance() > touchSlop) {
                                        isPanStarted = true
                                        holdJob?.cancel()
                                        val currentScale = scaleAnim.value
                                        val maxX = ((currentScale - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                                        val maxY = ((currentScale - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                                        val newOffset = Offset(
                                            x = (offsetAnim.value.x + panAccumulator.x).coerceIn(-maxX, maxX),
                                            y = (offsetAnim.value.y + panAccumulator.y).coerceIn(-maxY, maxY)
                                        )
                                        coroutineScope.launch { offsetAnim.snapTo(newOffset) }
                                        change.consume()
                                    }
                                } else {
                                    val currentScale = scaleAnim.value
                                    val maxX = ((currentScale - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                                    val maxY = ((currentScale - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                                    val newOffset = Offset(
                                        x = (offsetAnim.value.x + pan.x).coerceIn(-maxX, maxX),
                                        y = (offsetAnim.value.y + pan.y).coerceIn(-maxY, maxY)
                                    )
                                    coroutineScope.launch { offsetAnim.snapTo(newOffset) }
                                    change.consume()
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    holdJob?.cancel()
                    if (isHolding) {
                        isHolding = false
                        onHoldEnd?.invoke()
                    }

                    if (scaleAnim.value < 1f && !hasTriggeredDismiss) {
                        coroutineScope.launch {
                            scaleAnim.animateTo(1f, motionScheme.fastSpatialSpec())
                            offsetAnim.animateTo(Offset.Zero, motionScheme.fastSpatialSpec())
                            onZoomStateChanged(false)
                        }
                    } else if (scaleAnim.value > 1.02f) {
                        val currentScale = scaleAnim.value
                        val maxX = ((currentScale - 1f) * containerSize.width / 2f).coerceAtLeast(0f)
                        val maxY = ((currentScale - 1f) * containerSize.height / 2f).coerceAtLeast(0f)
                        val clampedOffset = Offset(
                            x = offsetAnim.value.x.coerceIn(-maxX, maxX),
                            y = offsetAnim.value.y.coerceIn(-maxY, maxY)
                        )
                        if (clampedOffset != offsetAnim.value) {
                            coroutineScope.launch {
                                offsetAnim.animateTo(clampedOffset, motionScheme.fastSpatialSpec())
                            }
                        }
                    }

                    if (initialPinchScale != null && (initialPinchScale ?: 1f) <= 1.05f && scaleAnim.value < 0.90f && !hasTriggeredDismiss) {
                        hasTriggeredDismiss = true
                        onDismiss()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content(graphicsModifier)
    }
}
