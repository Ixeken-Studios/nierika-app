package com.ixeken.nierika.feature.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ixeken.nierika.core.designsystem.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class TrimmerTouchTarget {
    NONE, START_HANDLE, END_HANDLE, SEGMENT_BODY, TIMELINE_SEEK
}

/**
 * Componente interactivo de línea de tiempo y recorte de video estilo Google Photos y Material 3 Expressive.
 * Soporta edición independiente de ambos extremos (inicio y fin), deslizamiento horizontal del segmento completo seleccionado,
 * y respuesta táctil háptica de alta fidelidad.
 */
@Composable
fun VideoTimelineTrimmer(
    videoUri: String,
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    currentPositionMsProvider: () -> Long,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onTrimRangeChange: (startMs: Long, endMs: Long) -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val maxDuration = if (durationMs > 0) durationMs.toFloat() else 1f
    val safeTrimEnd = if (trimEndMs > 0 && trimEndMs <= durationMs) trimEndMs else durationMs

    // Estados actualizados para gestos táctiles concurrentes
    val currentTrimStartMsState by rememberUpdatedState(trimStartMs)
    val currentTrimEndMsState by rememberUpdatedState(safeTrimEnd)
    val currentDurationMsState by rememberUpdatedState(durationMs)
    val currentOnTrimRangeChange by rememberUpdatedState(onTrimRangeChange)
    val currentOnSeekTo by rememberUpdatedState(onSeekTo)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila de controles flotantes sobre la línea de tiempo (Play/Pausa y Cápsula de Tiempo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTogglePlayPause()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(R.string.video_cd_pause) else stringResource(R.string.video_cd_play),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val segmentLengthMs = (safeTrimEnd - trimStartMs).coerceAtLeast(0L)
            PlayheadTimeCapsule(
                currentPositionMsProvider = currentPositionMsProvider,
                durationMs = durationMs,
                segmentLengthMs = segmentLengthMs
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Contenedor principal de la tira de video y manijas táctiles
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val handleWidthDp = 20.dp
            val handleWidthPx = with(density) { handleWidthDp.toPx() }
            val usableWidth = (totalWidthPx - 2f * handleWidthPx).coerceAtLeast(1f)
            val touchThresholdPx = with(density) { 32.dp.toPx() }

            val startRatio = (trimStartMs.toFloat() / maxDuration).coerceIn(0f, 1f)
            val endRatio = (safeTrimEnd.toFloat() / maxDuration).coerceIn(startRatio, 1f)

            // Posiciones internas donde inicia y termina el segmento seleccionado de video
            val startInnerPx = handleWidthPx + startRatio * usableWidth
            val endInnerPx = handleWidthPx + endRatio * usableWidth

            val primaryColor = MaterialTheme.colorScheme.primary
            val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
            // Tira de miniaturas de fondo con padding horizontal delegada a VideoThumbnailFilmstrip
            VideoThumbnailFilmstrip(
                videoUri = videoUri,
                durationMs = durationMs,
                handleWidthDp = handleWidthDp
            )

            // Capa de atenuación exterior izquierda (fuera del segmento seleccionado)
            if (startInnerPx > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(density) { startInnerPx.toDp() })
                        .background(Color.Black.copy(alpha = 0.65f))
                        .align(Alignment.CenterStart)
                )
            }

            // Capa de atenuación exterior derecha (fuera del segmento seleccionado)
            if (endInnerPx < totalWidthPx) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset { IntOffset(endInnerPx.roundToInt(), 0) }
                        .width(with(density) { (totalWidthPx - endInnerPx).toDp() })
                        .background(Color.Black.copy(alpha = 0.65f))
                        .align(Alignment.CenterStart)
                )
            }

            // Marco delimitador superior e inferior conectando la parte interna de las manijas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 3.dp.toPx()
                val left = startInnerPx.coerceIn(0f, totalWidthPx)
                val right = endInnerPx.coerceIn(left, totalWidthPx)

                drawLine(
                    color = primaryColor,
                    start = androidx.compose.ui.geometry.Offset(left, 0f),
                    end = androidx.compose.ui.geometry.Offset(right, 0f),
                    strokeWidth = strokeW
                )

                drawLine(
                    color = primaryColor,
                    start = androidx.compose.ui.geometry.Offset(left, size.height),
                    end = androidx.compose.ui.geometry.Offset(right, size.height),
                    strokeWidth = strokeW
                )
            }

            // Manija Izquierda de Recorte (|
            // Su borde interno se alinea exactamente con startInnerPx
            Box(
                modifier = Modifier
                    .offset {
                        val leftHandleX = (startInnerPx - handleWidthPx)
                            .coerceIn(0f, endInnerPx - handleWidthPx)
                        IntOffset(leftHandleX.roundToInt(), 0)
                    }
                    .width(handleWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(onPrimaryColor)
                )
            }

            // Manija Derecha de Recorte |)
            // Su borde interno se alinea exactamente con endInnerPx
            Box(
                modifier = Modifier
                    .offset {
                        val rightHandleX = endInnerPx
                            .coerceIn(startInnerPx, totalWidthPx - handleWidthPx)
                        IntOffset(rightHandleX.roundToInt(), 0)
                    }
                    .width(handleWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(onPrimaryColor)
                )
            }

            // Indicador de reproducción activo iniciando y moviéndose estrictamente dentro de las manijas
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .offset {
                        val curPos = currentPositionMsProvider()
                        val ratio = (curPos.toFloat() / maxDuration).coerceIn(0f, 1f)
                        val px = handleWidthPx + ratio * usableWidth
                        if (px in (startInnerPx - 1f)..(endInnerPx + 1f)) {
                            IntOffset(px.roundToInt() - 2, 0)
                        } else {
                            IntOffset(-10000, 0)
                        }
                    }
                    .width(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryColor)
                    .shadow(4.dp)
            )

            // Capa unificada superior de detección de gestos táctiles de alta precisión
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(totalWidthPx) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downTouchX = down.position.x

                            val totalDuration = currentDurationMsState
                            if (totalDuration <= 0L || totalWidthPx <= 0f) return@awaitEachGesture

                            val startMsAtDown = currentTrimStartMsState
                            val endMsAtDown = currentTrimEndMsState
                            val segmentDurationMs = (endMsAtDown - startMsAtDown).coerceAtLeast(500L)

                            val startRatioAtDown = (startMsAtDown.toFloat() / totalDuration).coerceIn(0f, 1f)
                            val endRatioAtDown = (endMsAtDown.toFloat() / totalDuration).coerceIn(startRatioAtDown, 1f)

                            val startInnerPxAtDown = handleWidthPx + startRatioAtDown * usableWidth
                            val endInnerPxAtDown = handleWidthPx + endRatioAtDown * usableWidth

                            // Centros de las zonas táctiles de las manijas
                            val startHandleCenter = startInnerPxAtDown - handleWidthPx / 2f
                            val endHandleCenter = endInnerPxAtDown + handleWidthPx / 2f

                            val distStart = abs(downTouchX - startHandleCenter)
                            val distEnd = abs(downTouchX - endHandleCenter)

                            val activeTarget = when {
                                distStart <= touchThresholdPx && distStart <= distEnd -> TrimmerTouchTarget.START_HANDLE
                                distEnd <= touchThresholdPx -> TrimmerTouchTarget.END_HANDLE
                                downTouchX > startInnerPxAtDown && downTouchX < endInnerPxAtDown -> TrimmerTouchTarget.SEGMENT_BODY
                                else -> TrimmerTouchTarget.TIMELINE_SEEK
                            }

                            if (activeTarget == TrimmerTouchTarget.TIMELINE_SEEK) {
                                val seekRatio = ((downTouchX - handleWidthPx) / usableWidth).coerceIn(0f, 1f)
                                val seekMs = (seekRatio * totalDuration).toLong().coerceIn(0L, totalDuration)
                                currentOnSeekTo(seekMs)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }

                            // Compensación respecto al borde interno de la manija para un arrastre 1:1 sin saltos
                            val offsetFromInner = when (activeTarget) {
                                TrimmerTouchTarget.START_HANDLE -> downTouchX - startInnerPxAtDown
                                TrimmerTouchTarget.END_HANDLE -> downTouchX - endInnerPxAtDown
                                else -> 0f
                            }

                            var currentStartMs = startMsAtDown
                            var currentEndMs = endMsAtDown
                            val minDistanceMs = 500L

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break

                                val posChange = change.positionChange()
                                if (posChange.x != 0f) {
                                    change.consume()
                                    val currentTouchX = change.position.x

                                    when (activeTarget) {
                                        TrimmerTouchTarget.START_HANDLE -> {
                                            val targetInnerPx = currentTouchX - offsetFromInner
                                            val targetRatio = ((targetInnerPx - handleWidthPx) / usableWidth).coerceIn(0f, 1f)
                                            val targetMs = (targetRatio * totalDuration).toLong()
                                            val maxStartMs = (endMsAtDown - minDistanceMs).coerceAtLeast(0L)
                                            currentStartMs = targetMs.coerceIn(0L, maxStartMs)
                                            currentEndMs = endMsAtDown
                                            currentOnTrimRangeChange(currentStartMs, currentEndMs)
                                            currentOnSeekTo(currentStartMs)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }

                                        TrimmerTouchTarget.END_HANDLE -> {
                                            val targetInnerPx = currentTouchX - offsetFromInner
                                            val targetRatio = ((targetInnerPx - handleWidthPx) / usableWidth).coerceIn(0f, 1f)
                                            val targetMs = (targetRatio * totalDuration).toLong()
                                            val minEndMs = (startMsAtDown + minDistanceMs).coerceAtMost(totalDuration)
                                            currentStartMs = startMsAtDown
                                            currentEndMs = targetMs.coerceIn(minEndMs, totalDuration)
                                            currentOnTrimRangeChange(currentStartMs, currentEndMs)
                                            currentOnSeekTo(currentEndMs)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }

                                        TrimmerTouchTarget.SEGMENT_BODY -> {
                                            val deltaX = currentTouchX - downTouchX
                                            val deltaRatio = deltaX / usableWidth
                                            val deltaMs = (deltaRatio * totalDuration).toLong()

                                            var newStart = startMsAtDown + deltaMs
                                            var newEnd = endMsAtDown + deltaMs

                                            if (newStart < 0L) {
                                                newStart = 0L
                                                newEnd = segmentDurationMs.coerceAtMost(totalDuration)
                                            } else if (newEnd > totalDuration) {
                                                newEnd = totalDuration
                                                newStart = (totalDuration - segmentDurationMs).coerceAtLeast(0L)
                                            }

                                            currentStartMs = newStart
                                            currentEndMs = newEnd
                                            currentOnTrimRangeChange(currentStartMs, currentEndMs)
                                            currentOnSeekTo(currentStartMs)
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        }

                                        TrimmerTouchTarget.TIMELINE_SEEK -> {
                                            val currentRatio = ((currentTouchX - handleWidthPx) / usableWidth).coerceIn(0f, 1f)
                                            val seekMs = (currentRatio * totalDuration).toLong().coerceIn(0L, totalDuration)
                                            currentOnSeekTo(seekMs)
                                        }

                                        TrimmerTouchTarget.NONE -> {}
                                    }
                                }
                            }
                        }
                    }
            )
        }
    }
}

/**
 * Cápsula de tiempo flotante aislada como composable hoja para restringir
 * las recomposiciones causadas por el avance de milisegundos únicamente a este elemento.
 */
@Composable
private fun PlayheadTimeCapsule(
    currentPositionMsProvider: () -> Long,
    durationMs: Long,
    segmentLengthMs: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.height(34.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "${formatDurationMs(currentPositionMsProvider())} / ${formatDurationMs(durationMs)} (${formatDurationMs(segmentLengthMs)})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Convierte una duración en milisegundos al formato mm:ss.
 */
private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
