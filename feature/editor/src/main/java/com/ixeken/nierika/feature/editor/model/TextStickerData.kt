package com.ixeken.nierika.feature.editor.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Estilo de fondo para el texto flotante estilo Stories.
 */
enum class TextBackgroundStyle {
    TRANSPARENT,
    SEMI_TRANSPARENT,
    SOLID
}

/**
 * Representa una capa de texto flotante interactiva sobre una fotografía o video.
 */
data class TextStickerData(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val color: Color = Color.White,
    val bgStyle: TextBackgroundStyle = TextBackgroundStyle.SEMI_TRANSPARENT,
    val positionNorm: Offset = Offset(0.5f, 0.5f),
    val scale: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val fontSize: Float = 26f
)
