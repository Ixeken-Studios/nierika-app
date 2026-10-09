package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlin.math.PI
import kotlin.math.sin

/**
 * Componente oficial de barra de progreso ondulada en Material 3 Expressive (Linear Wavy Progress Indicator).
 * Renderiza una onda senoidal fluida continua en la porción activa del progreso y una pista recta en la parte restante.
 *
 * @param progress Proporción de progreso entre 0.0f y 1.0f.
 * @param modifier Modificador de Compose para tamaño y disposición.
 * @param color Color del trazo ondulado activo.
 * @param trackColor Color de la pista de fondo inactiva.
 * @param strokeWidth Grosor del trazo de la onda y la pista.
 * @param waveAmplitude Altura / amplitud de la cresta de la onda senoidal.
 * @param wavelength Longitud de ciclo completo de la onda en píxeles.
 */
@Composable
fun NierikaLinearWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    strokeWidth: Dp = 6.dp,
    waveAmplitude: Dp = 4.5.dp,
    wavelength: Dp = 32.dp
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "wavyProgressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wavyPhaseTransition")
    val phaseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavyPhaseOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val strokeWidthPx = strokeWidth.toPx()
        val amplitudePx = waveAmplitude.toPx()
        val wavelengthPx = wavelength.toPx()
        val activeWidth = width * animatedProgress

        // 1. Pista inactiva de fondo (Línea recta redondeada)
        if (activeWidth < width) {
            drawLine(
                color = trackColor,
                start = Offset(x = activeWidth.coerceAtLeast(0f), y = centerY),
                end = Offset(x = width, y = centerY),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }

        // 2. Porción activa ondulada (Curva senoidal suave)
        if (activeWidth > 0f) {
            val wavePath = Path()
            val stepPx = 2f
            var x = 0f
            var isFirstPoint = true

            while (x <= activeWidth) {
                // Cálculo de la onda senoidal con desfase continuo
                val normalizedX = (x / wavelengthPx) * (2 * PI).toFloat() + phaseOffset
                val y = centerY + amplitudePx * sin(normalizedX)

                if (isFirstPoint) {
                    wavePath.moveTo(x, y)
                    isFirstPoint = false
                } else {
                    wavePath.lineTo(x, y)
                }
                x += stepPx
            }

            drawPath(
                path = wavePath,
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
