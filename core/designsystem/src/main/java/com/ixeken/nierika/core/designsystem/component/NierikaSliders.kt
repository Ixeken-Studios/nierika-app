package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Componente SliderWithTrackIcons según la especificación Material 3 Expressive (M3 Compose).
 * Proporciona un deslizador con iconos en los extremos del carril (startIcon y endIcon).
 */
@Composable
fun SliderWithTrackIcons(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    startIconContentDescription: String? = null,
    endIconContentDescription: String? = null,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (startIcon != null) {
            Icon(
                imageVector = startIcon,
                contentDescription = startIconContentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = colors,
            modifier = Modifier.weight(1f)
        )

        if (endIcon != null) {
            Icon(
                imageVector = endIcon,
                contentDescription = endIconContentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Componente CenteredSlider según la especificación Material 3 Expressive (M3 Compose).
 * Ideal para ajustes bipolares (ej. -100 a +100 con neutro en 0) o valores relativos a un origen.
 * La pista activa se expande dinámicamente desde el centro / origen hacia el valor seleccionado,
 * con una muesca central de referencia y animación elástica del thumb.
 */
@Composable
fun CenteredSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = -100f..100f,
    originValue: Float = 0f,
    onValueChangeFinished: (() -> Unit)? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    startIconContentDescription: String? = null,
    endIconContentDescription: String? = null,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    centerIndicatorColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (startIcon != null) {
            Icon(
                imageVector = startIcon,
                contentDescription = startIconContentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val density = LocalDensity.current

            val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
            val originFraction = ((originValue - valueRange.start) / rangeSpan).coerceIn(0f, 1f)
            val currentFraction = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

            var isDragging by remember { mutableStateOf(false) }

            // Pista del deslizador dibujada en Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .pointerInput(enabled, valueRange) {
                        if (!enabled) return@pointerInput
                        detectTapGestures(
                            onPress = { offset ->
                                isDragging = true
                                val newFraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val newValue = valueRange.start + newFraction * rangeSpan
                                currentOnValueChange(newValue)
                                tryAwaitRelease()
                                isDragging = false
                                currentOnValueChangeFinished?.invoke()
                            }
                        )
                    }
                    .draggable(
                        state = rememberDraggableState { delta ->
                            if (!enabled) return@rememberDraggableState
                            val deltaFraction = delta / totalWidthPx
                            val newValue = (value + deltaFraction * rangeSpan).coerceIn(valueRange.start, valueRange.endInclusive)
                            currentOnValueChange(newValue)
                        },
                        orientation = Orientation.Horizontal,
                        enabled = enabled,
                        onDragStarted = { isDragging = true },
                        onDragStopped = {
                            isDragging = false
                            currentOnValueChangeFinished?.invoke()
                        }
                    )
            ) {
                val trackHeight = 8.dp.toPx()
                val cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
                val topOffset = (size.height - trackHeight) / 2

                // 1. Pista Inactiva de Fondo Completo
                drawRoundRect(
                    color = inactiveTrackColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, trackHeight),
                    cornerRadius = cornerRadius
                )

                // 2. Pista Activa desde el origen hasta la posición actual
                val originX = originFraction * size.width
                val thumbX = currentFraction * size.width
                val leftX = minOf(originX, thumbX)
                val rightX = maxOf(originX, thumbX)
                val activeWidth = (rightX - leftX).coerceAtLeast(0f)

                if (activeWidth > 0f) {
                    drawRoundRect(
                        color = activeTrackColor,
                        topLeft = Offset(leftX, topOffset),
                        size = Size(activeWidth, trackHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Muesca / Indicador de Centro (Neutro)
                val centerTickWidth = 3.dp.toPx()
                val centerTickHeight = 12.dp.toPx()
                val centerTickTop = (size.height - centerTickHeight) / 2
                drawRoundRect(
                    color = centerIndicatorColor,
                    topLeft = Offset(originX - centerTickWidth / 2, centerTickTop),
                    size = Size(centerTickWidth, centerTickHeight),
                    cornerRadius = CornerRadius(centerTickWidth / 2, centerTickWidth / 2)
                )
            }

            // 4. Thumb flotante interactivo con Morphing elástico
            val thumbDiameter by animateDpAsState(
                targetValue = if (isDragging) 22.dp else 18.dp,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                ),
                label = "thumbDiameter"
            )

            val thumbOffsetPx = currentFraction * totalWidthPx
            val thumbOffsetDp = with(density) { (thumbOffsetPx - (thumbDiameter.toPx() / 2)).toDp() }

            Surface(
                shape = CircleShape,
                color = thumbColor,
                shadowElevation = if (isDragging) 6.dp else 2.dp,
                modifier = Modifier
                    .padding(start = thumbOffsetDp.coerceAtLeast(0.dp))
                    .size(thumbDiameter)
            ) {}
        }

        if (endIcon != null) {
            Icon(
                imageVector = endIcon,
                contentDescription = endIconContentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
