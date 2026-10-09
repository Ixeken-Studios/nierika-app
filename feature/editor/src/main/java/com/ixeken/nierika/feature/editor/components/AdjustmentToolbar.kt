package com.ixeken.nierika.feature.editor.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.CenteredSlider
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Enumeración de los 6 parámetros técnicos de ajuste de imagen disponibles.
 */
enum class AdjustmentOption(
    val titleResId: Int,
    val icon: ImageVector,
    val defaultValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>
) {
    BRIGHTNESS(R.string.adjust_brightness, Icons.Filled.Brightness6, 0f, -1f..1f),
    CONTRAST(R.string.adjust_contrast, Icons.Filled.Contrast, 1f, 0.2f..2f),
    SATURATION(R.string.adjust_saturation, Icons.Filled.Palette, 1f, 0f..2f),
    HIGHLIGHTS(R.string.adjust_highlights, Icons.Filled.Flare, 0f, -1f..1f),
    SHADOWS(R.string.adjust_shadows, Icons.Filled.Tonality, 0f, -1f..1f),
    TEMPERATURE(R.string.adjust_temperature, Icons.Filled.DeviceThermostat, 0f, -1f..1f)
}

/**
 * Panel M3 Expressive de dos niveles para el control técnico de imagen:
 * 1. Nivel Superior: Fila de tarjetas con etiqueta superior y caja de icono representativo.
 * 2. Nivel Activo (al seleccionar una opción): Píldora de título, cápsula con slider y botón de reset, y botón "Done" inferior.
 */
@Composable
fun AdjustmentToolbar(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    highlights: Float,
    shadows: Float,
    temperature: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onHighlightsChange: (Float) -> Unit,
    onShadowsChange: (Float) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onResetAdjustments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.nierikaMotionScheme
    var selectedOption by remember { mutableStateOf<AdjustmentOption?>(null) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        AnimatedContent(
            targetState = selectedOption,
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.fastEffectsSpec()) togetherWith
                        fadeOut(animationSpec = motionScheme.fastEffectsSpec())
            },
            label = "adjustmentViewModeAnimation"
        ) { option ->
            when (option) {
                null -> {
                    // MODO A: Fila horizontal desplazable de opciones con etiqueta arriba y botón con icono abajo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AdjustmentOption.entries.forEach { item ->
                            val currentValue = when (item) {
                                AdjustmentOption.BRIGHTNESS -> brightness
                                AdjustmentOption.CONTRAST -> contrast
                                AdjustmentOption.SATURATION -> saturation
                                AdjustmentOption.HIGHLIGHTS -> highlights
                                AdjustmentOption.SHADOWS -> shadows
                                AdjustmentOption.TEMPERATURE -> temperature
                            }
                            val isModified = kotlin.math.abs(currentValue - item.defaultValue) > 0.01f

                            val containerColor = if (isModified) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }

                            val iconTint = if (isModified) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(item.titleResId),
                                    fontSize = 12.sp,
                                    fontWeight = if (isModified) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isModified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(width = 68.dp, height = 54.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(containerColor)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedOption = item
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = stringResource(item.titleResId),
                                        tint = iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // MODO B: Control activo con dial / slider integrado y botón de confirmación "Done"
                    val currentValue = when (option) {
                        AdjustmentOption.BRIGHTNESS -> brightness
                        AdjustmentOption.CONTRAST -> contrast
                        AdjustmentOption.SATURATION -> saturation
                        AdjustmentOption.HIGHLIGHTS -> highlights
                        AdjustmentOption.SHADOWS -> shadows
                        AdjustmentOption.TEMPERATURE -> temperature
                    }

                    val onValueChangeCallback: (Float) -> Unit = { value ->
                        when (option) {
                            AdjustmentOption.BRIGHTNESS -> onBrightnessChange(value)
                            AdjustmentOption.CONTRAST -> onContrastChange(value)
                            AdjustmentOption.SATURATION -> onSaturationChange(value)
                            AdjustmentOption.HIGHLIGHTS -> onHighlightsChange(value)
                            AdjustmentOption.SHADOWS -> onShadowsChange(value)
                            AdjustmentOption.TEMPERATURE -> onTemperatureChange(value)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Título de la opción activa en píldora compacta
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(option.titleResId),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cápsula unificada para Icono + Slider + Botón de Reset individual
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = stringResource(option.titleResId),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                CenteredSlider(
                                    value = currentValue,
                                    onValueChange = { newVal ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onValueChangeCallback(newVal)
                                    },
                                    valueRange = option.valueRange,
                                    originValue = option.defaultValue,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                NierikaPlainTooltipBox(
                                    tooltipText = stringResource(R.string.tooltip_reset_adjustment)
                                ) {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onValueChangeCallback(option.defaultValue)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = stringResource(R.string.adjust_reset_all),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botón redondeado de confirmación "Done"
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedOption = null
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .height(44.dp)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.action_done),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

