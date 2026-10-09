package com.ixeken.nierika.feature.editor.util

import androidx.compose.ui.graphics.ColorMatrix
import kotlin.math.abs

/**
 * Utilidad encargada de calcular matrices de color (ColorMatrix) nativas para Compose.
 * Procesa en tiempo real: brillo, contraste, saturación, altas luces, sombras y temperatura de color.
 */
object ColorMatrixUtils {

    /**
     * Genera una matriz de color combinada aplicando transformaciones de color aceleradas por hardware.
     * @param brightness Rango [-1f, 1f], valor neutro 0f.
     * @param contrast Rango [0.2f, 2f], valor neutro 1f.
     * @param saturation Rango [0f, 2f], valor neutro 1f.
     * @param highlights Rango [-1f, 1f], valor neutro 0f.
     * @param shadows Rango [-1f, 1f], valor neutro 0f.
     * @param temperature Rango [-1f, 1f], valor neutro 0f.
     */
    fun createColorMatrix(
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
        highlights: Float = 0f,
        shadows: Float = 0f,
        temperature: Float = 0f
    ): ColorMatrix {
        // 1. Matriz de Saturación
        val satMatrix = ColorMatrix().apply {
            setToSaturation(saturation)
        }

        // 2. Matriz de Contraste, Brillo, Altas Luces y Sombras
        val c = contrast * (1f + highlights * 0.15f)
        val shadowOffset = shadows * 35f
        val translate = (0.5f * (1f - c) * 255f) + (brightness * 255f) + shadowOffset

        // 3. Modificación de Temperatura de Color (Warmth)
        val rScale = if (temperature > 0f) 1f + (temperature * 0.15f) else 1f - (abs(temperature) * 0.1f)
        val gScale = 1f
        val bScale = if (temperature < 0f) 1f + (abs(temperature) * 0.15f) else 1f - (temperature * 0.1f)

        val transformMatrix = ColorMatrix(
            floatArrayOf(
                c * rScale, 0f, 0f, 0f, translate,
                0f, c * gScale, 0f, 0f, translate,
                0f, 0f, c * bScale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        return multiplyMatrices(transformMatrix, satMatrix)
    }

    /**
     * Multiplica dos matrices de 4x5 en el espacio de transformación RGBA.
     */
    private fun multiplyMatrices(m1: ColorMatrix, m2: ColorMatrix): ColorMatrix {
        val a = m1.values
        val b = m2.values
        val result = FloatArray(20)

        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (i in 0..3) {
                    sum += a[row * 5 + i] * b[i * 5 + col]
                }
                result[row * 5 + col] = sum
            }
            var transSum = a[row * 5 + 4]
            for (i in 0..3) {
                transSum += a[row * 5 + i] * b[i * 5 + 4]
            }
            result[row * 5 + 4] = transSum
        }
        return ColorMatrix(result)
    }
}
