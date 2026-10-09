package com.ixeken.nierika.feature.editor.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaRichTooltipBox
import com.ixeken.nierika.core.designsystem.component.SliderWithTrackIcons
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.editor.model.DrawBrushMode
import kotlin.math.roundToInt

/**
 * Paleta de colores predeterminados de alta visibilidad para el trazo a mano alzada.
 */
private val drawColorPalette = listOf(
    Color.Black,
    Color(0xFFE53935), // Red
    Color(0xFFFDD835), // Yellow
    Color(0xFF43A047), // Green
    Color(0xFF1E88E5), // Blue
    Color(0xFFE91E63), // Pink / Magenta
    Color.White
)

/**
 * Barra flotante de dibujo y anotaciones estilo WhatsApp / Markup M3 Expressive.
 * 
 * Contiene selector superior de herramientas (Pen / Pixelate / Eraser) junto a los controles de historial
 * (Deshacer, Rehacer, Limpiar), una cápsula flotante con la paleta de colores y dial de grosor.
 */
@Composable
fun DrawingToolbar(
    brushMode: DrawBrushMode = DrawBrushMode.PEN,
    isEraserActive: Boolean = (brushMode == DrawBrushMode.ERASER),
    strokeWidth: Float,
    selectedColor: Color,
    canUndo: Boolean,
    canRedo: Boolean,
    hasStrokes: Boolean,
    isVideo: Boolean = false,
    onSelectBrushMode: (DrawBrushMode) -> Unit = {},
    onToggleEraser: (Boolean) -> Unit = { onSelectBrushMode(if (it) DrawBrushMode.ERASER else DrawBrushMode.PEN) },
    onStrokeWidthChange: (Float) -> Unit,
    onSelectColor: (Color) -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.nierikaMotionScheme
    var isThicknessSliderVisible by remember { mutableStateOf(false) }

    val activeMode = if (isVideo && brushMode == DrawBrushMode.PIXELATE) DrawBrushMode.PEN else brushMode

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila 1: Pestañas superiores de herramienta (Pen / Pixelate / Eraser) y botones de historial (Undo / Redo / Clear)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Acciones de Deshacer y Rehacer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.tooltip_undo_stroke)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUndoClick()
                        },
                        enabled = canUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = stringResource(R.string.draw_action_undo),
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.tooltip_redo_stroke)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRedoClick()
                        },
                        enabled = canRedo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = stringResource(R.string.draw_action_redo),
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Pestañas centrales de selección (Brush / Pixelate / Eraser)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.draw_tool_brush),
                    fontSize = 14.sp,
                    fontWeight = if (activeMode == DrawBrushMode.PEN) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeMode == DrawBrushMode.PEN) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectBrushMode(DrawBrushMode.PEN)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )

                if (!isVideo) {
                    Text(
                        text = stringResource(R.string.draw_tool_pixelate),
                        fontSize = 14.sp,
                        fontWeight = if (activeMode == DrawBrushMode.PIXELATE) FontWeight.Bold else FontWeight.Medium,
                        color = if (activeMode == DrawBrushMode.PIXELATE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectBrushMode(DrawBrushMode.PIXELATE)
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.draw_tool_eraser),
                    fontSize = 14.sp,
                    fontWeight = if (activeMode == DrawBrushMode.ERASER) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeMode == DrawBrushMode.ERASER) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectBrushMode(DrawBrushMode.ERASER)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            // Acción de Limpiar todo
            NierikaRichTooltipBox(
                title = stringResource(R.string.tooltip_clear_canvas_title),
                text = stringResource(R.string.tooltip_clear_canvas_desc)
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClearClick()
                    },
                    enabled = hasStrokes,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = stringResource(R.string.draw_action_clear),
                        tint = if (hasStrokes) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Fila 2 (Opcional): Panel expansible para calibrar el grosor del trazo
        AnimatedVisibility(
            visible = isThicknessSliderVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.draw_stroke_width),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${strokeWidth.roundToInt()} px",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                SliderWithTrackIcons(
                    value = strokeWidth,
                    onValueChange = onStrokeWidthChange,
                    valueRange = 4f..40f,
                    startIcon = Icons.Filled.LineWeight,
                    endIcon = Icons.Filled.Brush,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            }
        }

        // Fila 3: Cápsula flotante principal de colores y herramientas
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono representativo del modo activo (Pincel / Pixelar / Borrador)
                Icon(
                    imageVector = when (activeMode) {
                        DrawBrushMode.PEN -> Icons.Filled.Brush
                        DrawBrushMode.PIXELATE -> Icons.Filled.BlurOn
                        DrawBrushMode.ERASER -> Icons.Filled.AutoFixHigh
                    },
                    contentDescription = null,
                    tint = when (activeMode) {
                        DrawBrushMode.PEN -> MaterialTheme.colorScheme.primary
                        DrawBrushMode.PIXELATE -> MaterialTheme.colorScheme.primary
                        DrawBrushMode.ERASER -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(22.dp)
                )

                if (activeMode == DrawBrushMode.PEN) {
                    // Círculos de la paleta de colores para modo Pluma
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        drawColorPalette.forEach { color ->
                            val isSelected = selectedColor == color
                            val animatedSize by animateDpAsState(
                                targetValue = if (isSelected) 32.dp else 24.dp,
                                animationSpec = motionScheme.fastSpatialSpec(),
                                label = "paletteColorSize"
                            )

                            Box(
                                modifier = Modifier
                                    .size(animatedSize)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        when {
                                            isSelected -> Modifier.border(2.5.dp, Color.White, CircleShape)
                                            color == Color.Black -> Modifier.border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                                            else -> Modifier
                                        }
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSelectBrushMode(DrawBrushMode.PEN)
                                        onSelectColor(color)
                                    }
                            )
                        }
                    }
                } else if (activeMode == DrawBrushMode.PIXELATE) {
                    Text(
                        text = stringResource(R.string.draw_tool_pixelate),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = stringResource(R.string.draw_tool_eraser),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botón de ajuste de grosor
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.tooltip_stroke_width)
                ) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isThicknessSliderVisible = !isThicknessSliderVisible
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(R.string.draw_stroke_width),
                            tint = if (isThicknessSliderVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
