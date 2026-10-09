package com.ixeken.nierika.core.designsystem.util

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Calcula la forma (Shape) de una celda en una cuadrícula según su posición.
 * Aplica radio en las esquinas exteriores del perímetro de la grilla y mantiene rectas las esquinas interiores.
 */
@Composable
fun getGridItemShape(
    index: Int,
    totalCount: Int,
    columnsCount: Int = 3,
    cornerRadius: Dp = 16.dp
): Shape {
    if (totalCount <= 0) return RoundedCornerShape(0.dp)

    val isFirstRow = index < columnsCount
    val lastRowIndex = (totalCount - 1) / columnsCount
    val currentRowIndex = index / columnsCount
    val isLastRow = currentRowIndex == lastRowIndex

    val isTopLeft = index == 0
    val isTopRight = if (totalCount < columnsCount) index == totalCount - 1 else index == columnsCount - 1

    val isBottomLeft = isLastRow && (index % columnsCount == 0)
    val isBottomRight = index == totalCount - 1

    return RoundedCornerShape(
        topStart = if (isTopLeft) cornerRadius else 0.dp,
        topEnd = if (isTopRight) cornerRadius else 0.dp,
        bottomStart = if (isBottomLeft) cornerRadius else 0.dp,
        bottomEnd = if (isBottomRight) cornerRadius else 0.dp
    )
}
