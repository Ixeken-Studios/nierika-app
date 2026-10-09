package com.ixeken.nierika.feature.editor.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Dibuja el marco de recorte superpuesto sobre el lienzo de edición:
 * incluye el scrim oscurecido exterior, el borde del área activa, la cuadrícula 3x3 de regla de tercios
 * y los controladores esquineros en forma de L de alto contraste.
 */
fun DrawScope.drawCropOverlay(
    currentCropRect: Rect,
    primaryColor: Color
) {
    // Dibujo de capa oscura exterior (scrim)
    val outerPath = Path().apply {
        addRect(Rect(0f, 0f, size.width, size.height))
    }
    val innerPath = Path().apply {
        addRect(currentCropRect)
    }
    val scrimPath = Path.combine(
        operation = PathOperation.Difference,
        path1 = outerPath,
        path2 = innerPath
    )
    drawPath(
        path = scrimPath,
        color = Color.Black.copy(alpha = 0.55f)
    )

    // Marco del área de recorte activa (delgado y sutil)
    drawRect(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = currentCropRect.topLeft,
        size = currentCropRect.size,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Líneas de la Regla de Tercios (3x3 grid)
    val thirdW = currentCropRect.width / 3f
    val thirdH = currentCropRect.height / 3f

    // Líneas verticales
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = Offset(currentCropRect.left + thirdW, currentCropRect.top),
        end = Offset(currentCropRect.left + thirdW, currentCropRect.bottom),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = Offset(currentCropRect.left + thirdW * 2, currentCropRect.top),
        end = Offset(currentCropRect.left + thirdW * 2, currentCropRect.bottom),
        strokeWidth = 1.dp.toPx()
    )

    // Líneas horizontales
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = Offset(currentCropRect.left, currentCropRect.top + thirdH),
        end = Offset(currentCropRect.right, currentCropRect.top + thirdH),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = Color.White.copy(alpha = 0.45f),
        start = Offset(currentCropRect.left, currentCropRect.top + thirdH * 2),
        end = Offset(currentCropRect.right, currentCropRect.top + thirdH * 2),
        strokeWidth = 1.dp.toPx()
    )

    // Controladores esquineros gruesos en forma de L con el color del tema y contorno de alto contraste
    val handleLength = 28.dp.toPx()
    val handleStroke = 5.dp.toPx()
    val shadowStroke = handleStroke + 2.dp.toPx()
    val shadowColor = Color.Black.copy(alpha = 0.6f)

    // Esquina Superior Izquierda
    val topLeftPath = Path().apply {
        moveTo(currentCropRect.left, currentCropRect.top + handleLength)
        lineTo(currentCropRect.left, currentCropRect.top)
        lineTo(currentCropRect.left + handleLength, currentCropRect.top)
    }
    drawPath(topLeftPath, shadowColor, style = Stroke(shadowStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(topLeftPath, primaryColor, style = Stroke(handleStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Esquina Superior Derecha
    val topRightPath = Path().apply {
        moveTo(currentCropRect.right - handleLength, currentCropRect.top)
        lineTo(currentCropRect.right, currentCropRect.top)
        lineTo(currentCropRect.right, currentCropRect.top + handleLength)
    }
    drawPath(topRightPath, shadowColor, style = Stroke(shadowStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(topRightPath, primaryColor, style = Stroke(handleStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Esquina Inferior Izquierda
    val bottomLeftPath = Path().apply {
        moveTo(currentCropRect.left, currentCropRect.bottom - handleLength)
        lineTo(currentCropRect.left, currentCropRect.bottom)
        lineTo(currentCropRect.left + handleLength, currentCropRect.bottom)
    }
    drawPath(bottomLeftPath, shadowColor, style = Stroke(shadowStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(bottomLeftPath, primaryColor, style = Stroke(handleStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Esquina Inferior Derecha
    val bottomRightPath = Path().apply {
        moveTo(currentCropRect.right - handleLength, currentCropRect.bottom)
        lineTo(currentCropRect.right, currentCropRect.bottom)
        lineTo(currentCropRect.right, currentCropRect.bottom - handleLength)
    }
    drawPath(bottomRightPath, shadowColor, style = Stroke(shadowStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(bottomRightPath, primaryColor, style = Stroke(handleStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
