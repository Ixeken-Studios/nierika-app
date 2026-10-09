package com.ixeken.nierika.feature.editor.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs

/**
 * Identifica los controladores táctiles interactivos del marco de recorte.
 */
enum class CropDragHandle {
    NONE,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    CENTER
}

/**
 * Detecta qué controlador del marco de recorte fue presionado por el usuario en base a su proximidad.
 */
fun detectCropDragHandle(
    downPos: Offset,
    cropRect: Rect,
    handleRadius: Float,
    edgeThreshold: Float = 0f
): CropDragHandle {
    val distTL = (downPos - cropRect.topLeft).getDistance()
    val distTR = (downPos - cropRect.topRight).getDistance()
    val distBL = (downPos - cropRect.bottomLeft).getDistance()
    val distBR = (downPos - cropRect.bottomRight).getDistance()

    return when {
        distTL <= handleRadius -> CropDragHandle.TOP_LEFT
        distTR <= handleRadius -> CropDragHandle.TOP_RIGHT
        distBL <= handleRadius -> CropDragHandle.BOTTOM_LEFT
        distBR <= handleRadius -> CropDragHandle.BOTTOM_RIGHT
        edgeThreshold > 0f && downPos.x in cropRect.left..cropRect.right && abs(downPos.y - cropRect.top) <= edgeThreshold -> CropDragHandle.TOP
        edgeThreshold > 0f && downPos.x in cropRect.left..cropRect.right && abs(downPos.y - cropRect.bottom) <= edgeThreshold -> CropDragHandle.BOTTOM
        edgeThreshold > 0f && downPos.y in cropRect.top..cropRect.bottom && abs(downPos.x - cropRect.left) <= edgeThreshold -> CropDragHandle.LEFT
        edgeThreshold > 0f && downPos.y in cropRect.top..cropRect.bottom && abs(downPos.x - cropRect.right) <= edgeThreshold -> CropDragHandle.RIGHT
        cropRect.contains(downPos) -> CropDragHandle.CENTER
        else -> CropDragHandle.NONE
    }
}

/**
 * Recalcula el rectángulo de recorte al manipular esquinas, bordes o desplazar el marco central,
 * respetando el aspecto de ratio fijado y los límites de la imagen o video.
 */
fun calculateNewCropRect(
    current: Rect,
    dragAmount: Offset,
    handle: CropDragHandle,
    aspectRatio: Float?,
    bounds: Rect,
    minSize: Float = 100f
): Rect {
    return when (handle) {
        CropDragHandle.CENTER -> {
            val newLeft = (current.left + dragAmount.x).coerceIn(bounds.left, bounds.right - current.width)
            val newTop = (current.top + dragAmount.y).coerceIn(bounds.top, bounds.bottom - current.height)
            Rect(newLeft, newTop, newLeft + current.width, newTop + current.height)
        }

        CropDragHandle.TOP_LEFT -> {
            var newLeft = (current.left + dragAmount.x).coerceIn(bounds.left, current.right - minSize)
            var newTop = (current.top + dragAmount.y).coerceIn(bounds.top, current.bottom - minSize)

            if (aspectRatio != null) {
                val newW = current.right - newLeft
                val newH = newW / aspectRatio
                newTop = (current.bottom - newH).coerceIn(bounds.top, current.bottom - minSize)
                newLeft = current.right - (current.bottom - newTop) * aspectRatio
            }
            Rect(newLeft, newTop, current.right, current.bottom)
        }

        CropDragHandle.TOP_RIGHT -> {
            var newRight = (current.right + dragAmount.x).coerceIn(current.left + minSize, bounds.right)
            var newTop = (current.top + dragAmount.y).coerceIn(bounds.top, current.bottom - minSize)

            if (aspectRatio != null) {
                val newW = newRight - current.left
                val newH = newW / aspectRatio
                newTop = (current.bottom - newH).coerceIn(bounds.top, current.bottom - minSize)
                newRight = current.left + (current.bottom - newTop) * aspectRatio
            }
            Rect(current.left, newTop, newRight, current.bottom)
        }

        CropDragHandle.BOTTOM_LEFT -> {
            var newLeft = (current.left + dragAmount.x).coerceIn(bounds.left, current.right - minSize)
            var newBottom = (current.bottom + dragAmount.y).coerceIn(current.top + minSize, bounds.bottom)

            if (aspectRatio != null) {
                val newW = current.right - newLeft
                val newH = newW / aspectRatio
                newBottom = (current.top + newH).coerceIn(current.top + minSize, bounds.bottom)
                newLeft = current.right - (newBottom - current.top) * aspectRatio
            }
            Rect(newLeft, current.top, current.right, newBottom)
        }

        CropDragHandle.BOTTOM_RIGHT -> {
            var newRight = (current.right + dragAmount.x).coerceIn(current.left + minSize, bounds.right)
            var newBottom = (current.bottom + dragAmount.y).coerceIn(current.top + minSize, bounds.bottom)

            if (aspectRatio != null) {
                val newW = newRight - current.left
                val newH = newW / aspectRatio
                newBottom = (current.top + newH).coerceIn(current.top + minSize, bounds.bottom)
                newRight = current.left + (newBottom - current.top) * aspectRatio
            }
            Rect(current.left, current.top, newRight, newBottom)
        }

        CropDragHandle.TOP -> {
            val newTop = (current.top + dragAmount.y).coerceIn(bounds.top, current.bottom - minSize)
            var newLeft = current.left
            var newRight = current.right

            if (aspectRatio != null) {
                val newH = current.bottom - newTop
                val newW = newH * aspectRatio
                val center = (current.left + current.right) / 2f
                newLeft = (center - newW / 2f).coerceAtLeast(bounds.left)
                newRight = (newLeft + newW).coerceAtMost(bounds.right)
                if (newRight == bounds.right) {
                    newLeft = bounds.right - newW
                }
            }
            Rect(newLeft, newTop, newRight, current.bottom)
        }

        CropDragHandle.BOTTOM -> {
            val newBottom = (current.bottom + dragAmount.y).coerceIn(current.top + minSize, bounds.bottom)
            var newLeft = current.left
            var newRight = current.right

            if (aspectRatio != null) {
                val newH = newBottom - current.top
                val newW = newH * aspectRatio
                val center = (current.left + current.right) / 2f
                newLeft = (center - newW / 2f).coerceAtLeast(bounds.left)
                newRight = (newLeft + newW).coerceAtMost(bounds.right)
                if (newRight == bounds.right) {
                    newLeft = bounds.right - newW
                }
            }
            Rect(newLeft, current.top, newRight, newBottom)
        }

        CropDragHandle.LEFT -> {
            val newLeft = (current.left + dragAmount.x).coerceIn(bounds.left, current.right - minSize)
            var newTop = current.top
            var newBottom = current.bottom

            if (aspectRatio != null) {
                val newW = current.right - newLeft
                val newH = newW / aspectRatio
                val center = (current.top + current.bottom) / 2f
                newTop = (center - newH / 2f).coerceAtLeast(bounds.top)
                newBottom = (newTop + newH).coerceAtMost(bounds.bottom)
                if (newBottom == bounds.bottom) {
                    newTop = bounds.bottom - newH
                }
            }
            Rect(newLeft, newTop, current.right, newBottom)
        }

        CropDragHandle.RIGHT -> {
            val newRight = (current.right + dragAmount.x).coerceIn(current.left + minSize, bounds.right)
            var newTop = current.top
            var newBottom = current.bottom

            if (aspectRatio != null) {
                val newW = newRight - current.left
                val newH = newW / aspectRatio
                val center = (current.top + current.bottom) / 2f
                newTop = (center - newH / 2f).coerceAtLeast(bounds.top)
                newBottom = (newTop + newH).coerceAtMost(bounds.bottom)
                if (newBottom == bounds.bottom) {
                    newTop = bounds.bottom - newH
                }
            }
            Rect(current.left, newTop, newRight, newBottom)
        }

        CropDragHandle.NONE -> current
    }
}
