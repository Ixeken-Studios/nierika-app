package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Componente NierikaLoadingIndicator que implementa fielmente el diseño oficial de Material 3 Expressive.
 * Renderiza la forma de estrella de 12 lóbulos redondeados (scalloped starburst) con rotación y pulso elástico.
 */
@Composable
fun NierikaLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    lobes: Int = 12
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3eLoadingTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "m3eRotation"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "m3eScalePulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scalePulse)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val path = Path().apply {
                createScallopedStarPath(size = this@Canvas.size, lobes = lobes)
            }
            drawPath(
                path = path,
                color = color
            )
        }
    }
}

private fun Path.createScallopedStarPath(size: Size, lobes: Int) {
    reset()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerR = minOf(cx, cy)
    val innerR = outerR * 0.80f
    val totalPoints = lobes * 2

    for (i in 0 until totalPoints) {
        val angle = (i * Math.PI / lobes).toFloat()
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + r * cos(angle)
        val y = cy + r * sin(angle)
        if (i == 0) {
            moveTo(x, y)
        } else {
            val prevAngle = ((i - 1) * Math.PI / lobes).toFloat()
            val prevR = if ((i - 1) % 2 == 0) outerR else innerR
            val midAngle = (angle + prevAngle) / 2f
            val midR = (r + prevR) / 2f * 1.04f
            val ctrlX = cx + midR * cos(midAngle)
            val ctrlY = cy + midR * sin(midAngle)
            quadraticTo(ctrlX, ctrlY, x, y)
        }
    }
    close()
}
