package com.ixeken.nierika.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Convierte coordenadas HSL (Hue, Saturation, Lightness) a un Color de Compose.
 * Implementación matemática pura en Kotlin que no depende de APIs del framework Android,
 * garantizando determinismo absoluto y compatibilidad en previews y pruebas unitarias.
 */
private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color {
    val h = (hue % 360f + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val l = lightness.coerceIn(0f, 1f)
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = 1f
    )
}

/**
 * Construye un ColorScheme oficial de Material Design 3 Expressive a partir de un tono (hue) y saturación,
 * respetando la progresión matemática de luminancia para cada nivel de superficie y contraste accesible.
 *
 * En modo oscuro, las superficies utilizan una saturación neutra muy sutil (8%) en el mismo tono primario,
 * asegurando que surfaceContainerHighest sea armónico con surfaceContainerHigh y eliminando cualquier
 * caída al color morado por defecto de Compose.
 */
fun buildM3ColorScheme(
    hue: Float,
    saturation: Float,
    darkTheme: Boolean
): ColorScheme {
    val primSat = saturation.coerceIn(0f, 0.85f)
    val secSat = (saturation * 0.35f).coerceIn(0f, 0.35f)
    val tertSat = (saturation * 0.50f).coerceIn(0f, 0.50f)
    val tertHue = (hue + 60f) % 360f

    val neutralSat = 0.08f
    val neutralVarSat = 0.14f

    return if (darkTheme) {
        darkColorScheme(
            primary = hslColor(hue, primSat, 0.80f),
            onPrimary = hslColor(hue, primSat, 0.20f),
            primaryContainer = hslColor(hue, primSat, 0.30f),
            onPrimaryContainer = hslColor(hue, primSat, 0.90f),
            inversePrimary = hslColor(hue, primSat, 0.40f),

            secondary = hslColor(hue, secSat, 0.80f),
            onSecondary = hslColor(hue, secSat, 0.20f),
            secondaryContainer = hslColor(hue, secSat, 0.30f),
            onSecondaryContainer = hslColor(hue, secSat, 0.90f),

            tertiary = hslColor(tertHue, tertSat, 0.80f),
            onTertiary = hslColor(tertHue, tertSat, 0.20f),
            tertiaryContainer = hslColor(tertHue, tertSat, 0.30f),
            onTertiaryContainer = hslColor(tertHue, tertSat, 0.90f),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),

            background = hslColor(hue, neutralSat, 0.06f),
            onBackground = hslColor(hue, neutralSat, 0.90f),
            surface = hslColor(hue, neutralSat, 0.06f),
            onSurface = hslColor(hue, neutralSat, 0.90f),
            surfaceVariant = hslColor(hue, neutralVarSat, 0.30f),
            onSurfaceVariant = hslColor(hue, neutralVarSat, 0.80f),
            surfaceTint = hslColor(hue, primSat, 0.80f),

            surfaceContainerLowest = hslColor(hue, neutralSat, 0.04f),
            surfaceContainerLow = hslColor(hue, neutralSat, 0.10f),
            surfaceContainer = hslColor(hue, neutralSat, 0.12f),
            surfaceContainerHigh = hslColor(hue, neutralSat, 0.17f),
            surfaceContainerHighest = hslColor(hue, neutralSat, 0.22f),

            surfaceDim = hslColor(hue, neutralSat, 0.06f),
            surfaceBright = hslColor(hue, neutralSat, 0.24f),

            outline = hslColor(hue, neutralVarSat, 0.60f),
            outlineVariant = hslColor(hue, neutralVarSat, 0.30f),
            scrim = Color.Black,
            inverseSurface = hslColor(hue, neutralSat, 0.90f),
            inverseOnSurface = hslColor(hue, neutralSat, 0.20f)
        )
    } else {
        lightColorScheme(
            primary = hslColor(hue, primSat, 0.40f),
            onPrimary = Color.White,
            primaryContainer = hslColor(hue, primSat, 0.90f),
            onPrimaryContainer = hslColor(hue, primSat, 0.10f),
            inversePrimary = hslColor(hue, primSat, 0.80f),

            secondary = hslColor(hue, secSat, 0.40f),
            onSecondary = Color.White,
            secondaryContainer = hslColor(hue, secSat, 0.90f),
            onSecondaryContainer = hslColor(hue, secSat, 0.10f),

            tertiary = hslColor(tertHue, tertSat, 0.40f),
            onTertiary = Color.White,
            tertiaryContainer = hslColor(tertHue, tertSat, 0.90f),
            onTertiaryContainer = hslColor(tertHue, tertSat, 0.10f),

            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),

            background = hslColor(hue, neutralSat, 0.98f),
            onBackground = hslColor(hue, neutralSat, 0.10f),
            surface = hslColor(hue, neutralSat, 0.98f),
            onSurface = hslColor(hue, neutralSat, 0.10f),
            surfaceVariant = hslColor(hue, neutralVarSat, 0.90f),
            onSurfaceVariant = hslColor(hue, neutralVarSat, 0.30f),
            surfaceTint = hslColor(hue, primSat, 0.40f),

            surfaceContainerLowest = Color.White,
            surfaceContainerLow = hslColor(hue, neutralSat, 0.96f),
            surfaceContainer = hslColor(hue, neutralSat, 0.94f),
            surfaceContainerHigh = hslColor(hue, neutralSat, 0.92f),
            surfaceContainerHighest = hslColor(hue, neutralSat, 0.90f),

            surfaceDim = hslColor(hue, neutralSat, 0.87f),
            surfaceBright = hslColor(hue, neutralSat, 0.98f),

            outline = hslColor(hue, neutralVarSat, 0.50f),
            outlineVariant = hslColor(hue, neutralVarSat, 0.80f),
            scrim = Color.Black,
            inverseSurface = hslColor(hue, neutralSat, 0.20f),
            inverseOnSurface = hslColor(hue, neutralSat, 0.95f)
        )
    }
}

// 13. Purple (265° - Nierika Baseline)
val NierikaLightColorScheme = buildM3ColorScheme(hue = 265f, saturation = 0.75f, darkTheme = false)
val NierikaDarkColorScheme = buildM3ColorScheme(hue = 265f, saturation = 0.75f, darkTheme = true)

/**
 * Genera el esquema de color oficial de Material Design 3 según la semilla seleccionada.
 */
fun getM3ColorScheme(
    style: ColorSchemeStyle,
    darkTheme: Boolean
): ColorScheme {
    return when (style) {
        ColorSchemeStyle.DYNAMIC -> if (darkTheme) NierikaDarkColorScheme else NierikaLightColorScheme
        ColorSchemeStyle.MONOCHROME -> if (darkTheme) monochromeDarkColorScheme else monochromeLightColorScheme
        ColorSchemeStyle.PINK -> if (darkTheme) pinkDarkColorScheme else pinkLightColorScheme
        ColorSchemeStyle.ROSE -> if (darkTheme) roseDarkColorScheme else roseLightColorScheme
        ColorSchemeStyle.RED -> if (darkTheme) redDarkColorScheme else redLightColorScheme
        ColorSchemeStyle.ORANGE -> if (darkTheme) orangeDarkColorScheme else orangeLightColorScheme
        ColorSchemeStyle.YELLOW -> if (darkTheme) yellowDarkColorScheme else yellowLightColorScheme
        ColorSchemeStyle.CHARTREUSE -> if (darkTheme) chartreuseDarkColorScheme else chartreuseLightColorScheme
        ColorSchemeStyle.GREEN -> if (darkTheme) greenDarkColorScheme else greenLightColorScheme
        ColorSchemeStyle.TEAL -> if (darkTheme) tealDarkColorScheme else tealLightColorScheme
        ColorSchemeStyle.CYAN -> if (darkTheme) cyanDarkColorScheme else cyanLightColorScheme
        ColorSchemeStyle.BLUE -> if (darkTheme) blueDarkColorScheme else blueLightColorScheme
        ColorSchemeStyle.INDIGO -> if (darkTheme) indigoDarkColorScheme else indigoLightColorScheme
        ColorSchemeStyle.PURPLE -> if (darkTheme) NierikaDarkColorScheme else NierikaLightColorScheme
    }
}

// 1. Monochrome
// 1. Monochrome (Alto contraste en escala de grises pura según guía M3)
val monochromeLightColorScheme = lightColorScheme(
    primary = Color(0xFF141414),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2E2E30),
    onPrimaryContainer = Color(0xFFF2F2F5),
    secondary = Color(0xFF424245),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E2E5),
    onSecondaryContainer = Color(0xFF141414),
    tertiary = Color(0xFF5A5A5E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6D6DA),
    onTertiaryContainer = Color(0xFF1A1A1C),
    background = Color(0xFFF8F8FA),
    surface = Color(0xFFF8F8FA),
    onBackground = Color(0xFF121214),
    onSurface = Color(0xFF121214),
    surfaceVariant = Color(0xFFE2E2E5),
    onSurfaceVariant = Color(0xFF424245),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F2F5),
    surfaceContainer = Color(0xFFEBEBEF),
    surfaceContainerHigh = Color(0xFFE2E2E6),
    surfaceContainerHighest = Color(0xFFD8D8DC),
    outline = Color(0xFF75757A),
    outlineVariant = Color(0xFFC5C5C9),
    scrim = Color.Black,
    surfaceDim = Color(0xFFE0E0E4),
    surfaceBright = Color(0xFFFDFDFD),
    inverseSurface = Color(0xFF2E2E32),
    inverseOnSurface = Color(0xFFF2F2F5),
    inversePrimary = Color(0xFFE2E2E6)
)

val monochromeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF121214),
    primaryContainer = Color(0xFFE2E2E6),
    onPrimaryContainer = Color(0xFF141414),
    inversePrimary = Color(0xFF2E2E30),
    secondary = Color(0xFFD0D0D4),
    onSecondary = Color(0xFF1B1B1E),
    secondaryContainer = Color(0xFF3A3A3E),
    onSecondaryContainer = Color(0xFFEAEAEA),
    tertiary = Color(0xFFB8B8BC),
    onTertiary = Color(0xFF18181A),
    tertiaryContainer = Color(0xFF2E2E32),
    onTertiaryContainer = Color(0xFFE2E2E5),
    background = Color(0xFF121214),
    surface = Color(0xFF121214),
    onBackground = Color(0xFFF2F2F5),
    onSurface = Color(0xFFF2F2F5),
    surfaceVariant = Color(0xFF3A3A3E),
    onSurfaceVariant = Color(0xFFD0D0D4),
    surfaceTint = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFF0C0C0E),
    surfaceContainerLow = Color(0xFF1A1A1D),
    surfaceContainer = Color(0xFF222225),
    surfaceContainerHigh = Color(0xFF2B2B2F),
    surfaceContainerHighest = Color(0xFF36363B),
    surfaceDim = Color(0xFF121214),
    surfaceBright = Color(0xFF3A3A3E),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF4A4A4F),
    scrim = Color.Black,
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF1A1A1D)
)

// 2. Pink (340°)
val pinkLightColorScheme = buildM3ColorScheme(hue = 340f, saturation = 0.85f, darkTheme = false)
val pinkDarkColorScheme = buildM3ColorScheme(hue = 340f, saturation = 0.85f, darkTheme = true)

// 3. Rose (350°)
val roseLightColorScheme = buildM3ColorScheme(hue = 350f, saturation = 0.80f, darkTheme = false)
val roseDarkColorScheme = buildM3ColorScheme(hue = 350f, saturation = 0.80f, darkTheme = true)

// 4. Red (5°)
val redLightColorScheme = buildM3ColorScheme(hue = 5f, saturation = 0.85f, darkTheme = false)
val redDarkColorScheme = buildM3ColorScheme(hue = 5f, saturation = 0.85f, darkTheme = true)

// 5. Orange (32°)
val orangeLightColorScheme = buildM3ColorScheme(hue = 32f, saturation = 0.90f, darkTheme = false)
val orangeDarkColorScheme = buildM3ColorScheme(hue = 32f, saturation = 0.90f, darkTheme = true)

// 6. Yellow (48°)
val yellowLightColorScheme = buildM3ColorScheme(hue = 48f, saturation = 0.90f, darkTheme = false)
val yellowDarkColorScheme = buildM3ColorScheme(hue = 48f, saturation = 0.90f, darkTheme = true)

// 7. Chartreuse (82°)
val chartreuseLightColorScheme = buildM3ColorScheme(hue = 82f, saturation = 0.75f, darkTheme = false)
val chartreuseDarkColorScheme = buildM3ColorScheme(hue = 82f, saturation = 0.75f, darkTheme = true)

// 8. Green (135°)
val greenLightColorScheme = buildM3ColorScheme(hue = 135f, saturation = 0.70f, darkTheme = false)
val greenDarkColorScheme = buildM3ColorScheme(hue = 135f, saturation = 0.70f, darkTheme = true)

// 9. Teal (175°)
val tealLightColorScheme = buildM3ColorScheme(hue = 175f, saturation = 0.70f, darkTheme = false)
val tealDarkColorScheme = buildM3ColorScheme(hue = 175f, saturation = 0.70f, darkTheme = true)

// 10. Cyan (192°)
val cyanLightColorScheme = buildM3ColorScheme(hue = 192f, saturation = 0.80f, darkTheme = false)
val cyanDarkColorScheme = buildM3ColorScheme(hue = 192f, saturation = 0.80f, darkTheme = true)

// 11. Blue (215°)
val blueLightColorScheme = buildM3ColorScheme(hue = 215f, saturation = 0.85f, darkTheme = false)
val blueDarkColorScheme = buildM3ColorScheme(hue = 215f, saturation = 0.85f, darkTheme = true)

// 12. Indigo (235°)
val indigoLightColorScheme = buildM3ColorScheme(hue = 235f, saturation = 0.75f, darkTheme = false)
val indigoDarkColorScheme = buildM3ColorScheme(hue = 235f, saturation = 0.75f, darkTheme = true)
