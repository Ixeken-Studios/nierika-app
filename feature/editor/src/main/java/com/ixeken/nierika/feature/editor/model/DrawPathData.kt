package com.ixeken.nierika.feature.editor.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Modalidad de pincel para anotaciones sobre la imagen.
 */
enum class DrawBrushMode {
    PEN,
    PIXELATE,
    ERASER
}

/**
 * Representa un trazo a mano alzada realizado sobre el lienzo de edición.
 */
data class DrawPathData(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Offset>,
    val color: Color = Color.Red,
    val strokeWidth: Float = 10f,
    val brushMode: DrawBrushMode = DrawBrushMode.PEN
) {
    val isEraser: Boolean get() = brushMode == DrawBrushMode.ERASER
    val isPixelate: Boolean get() = brushMode == DrawBrushMode.PIXELATE
}
