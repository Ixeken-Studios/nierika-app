package com.ixeken.nierika.feature.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import com.ixeken.nierika.core.designsystem.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ixeken.nierika.feature.editor.model.TextBackgroundStyle
import com.ixeken.nierika.feature.editor.model.TextStickerData
import kotlin.math.roundToInt

/**
 * Capa flotante interactiva multitáctil para manipular capas de texto (Stickers) sobre fotos o videos.
 * Utiliza deltas normalizados continuos para permitir arrastrar, rotar y escalar sin que el texto regrese al centro.
 */
@Composable
fun TextStickerOverlay(
    stickers: List<TextStickerData>,
    bounds: Rect,
    isTextMode: Boolean,
    onUpdateSticker: (TextStickerData) -> Unit,
    onEditSticker: (TextStickerData) -> Unit,
    onDeleteSticker: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedStickerId by remember { mutableStateOf<String?>(stickers.lastOrNull()?.id) }

    val safeBounds = if (bounds.width > 0f && bounds.height > 0f) bounds else Rect(0f, 0f, 1000f, 1000f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        selectedStickerId = null
                    }
                )
            }
    ) {
        stickers.forEach { sticker ->
            val isSelected = isTextMode && (selectedStickerId == sticker.id)

            val currentSticker by rememberUpdatedState(sticker)
            val currentOnUpdate by rememberUpdatedState(onUpdateSticker)
            val currentOnEdit by rememberUpdatedState(onEditSticker)
            val currentOnDelete by rememberUpdatedState(onDeleteSticker)

            // Posición en píxeles basada en las coordenadas normalizadas (0..1)
            val posX = safeBounds.left + currentSticker.positionNorm.x * safeBounds.width
            val posY = safeBounds.top + currentSticker.positionNorm.y * safeBounds.height

            val actualTextColor = when (currentSticker.bgStyle) {
                TextBackgroundStyle.SOLID -> if (currentSticker.color == Color.White) Color.Black else Color.White
                else -> currentSticker.color
            }

            val backgroundColor = when (currentSticker.bgStyle) {
                TextBackgroundStyle.TRANSPARENT -> Color.Transparent
                TextBackgroundStyle.SEMI_TRANSPARENT -> Color.Black.copy(alpha = 0.65f)
                TextBackgroundStyle.SOLID -> if (currentSticker.color == Color.White) Color.White else currentSticker.color.copy(alpha = 0.95f)
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(posX.roundToInt(), posY.roundToInt())
                    }
                    .graphicsLayer {
                        translationX = -size.width / 2f
                        translationY = -size.height / 2f
                        scaleX = currentSticker.scale
                        scaleY = currentSticker.scale
                        rotationZ = currentSticker.rotationDegrees
                    }
                    .zIndex(if (isSelected) 5f else 1f)
                    .pointerInput(currentSticker.id, safeBounds) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            selectedStickerId = currentSticker.id

                            val deltaNormX = pan.x / safeBounds.width.coerceAtLeast(1f)
                            val deltaNormY = pan.y / safeBounds.height.coerceAtLeast(1f)

                            val newNormX = (currentSticker.positionNorm.x + deltaNormX).coerceIn(0.02f, 0.98f)
                            val newNormY = (currentSticker.positionNorm.y + deltaNormY).coerceIn(0.02f, 0.98f)
                            val newScale = (currentSticker.scale * zoom).coerceIn(0.35f, 5.0f)
                            val newRotation = (currentSticker.rotationDegrees + rotation) % 360f

                            currentOnUpdate(
                                currentSticker.copy(
                                    positionNorm = Offset(newNormX, newNormY),
                                    scale = newScale,
                                    rotationDegrees = newRotation
                                )
                            )
                        }
                    }
                    .pointerInput(currentSticker.id) {
                        detectTapGestures(
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (selectedStickerId == currentSticker.id) {
                                    currentOnEdit(currentSticker)
                                } else {
                                    selectedStickerId = currentSticker.id
                                }
                            },
                            onDoubleTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnEdit(currentSticker)
                            }
                        )
                    }
            ) {
                // Contenedor visual del texto con fondo estilizado
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = currentSticker.text,
                        color = actualTextColor,
                        fontSize = currentSticker.fontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Controles de Selección rápida (Botón Borrar y Botón Editar)
                if (isSelected) {
                    // Botón Borrar (esquina superior izquierda)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-12).dp, y = (-12).dp)
                            .size(28.dp)
                            .zIndex(10f)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnDelete(currentSticker.id)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.text_delete_button),
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    // Botón Editar (esquina superior derecha)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-12).dp)
                            .size(28.dp)
                            .zIndex(10f)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnEdit(currentSticker)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.text_edit_button),
                            tint = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}
