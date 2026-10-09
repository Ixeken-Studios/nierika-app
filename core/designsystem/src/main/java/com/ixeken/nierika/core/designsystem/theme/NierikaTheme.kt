package com.ixeken.nierika.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

/**
 * Genera la tipografía de Nierika con la fuente seleccionada.
 */
fun createNierikaTypography(
    fontFamily: FontFamily = FontFamily.Default
): Typography {
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
    )
}

/**
 * Anima cada token del ColorScheme con un spring rápido para que los cambios
 * de tema se perciban fluidos en lugar de instantáneos.
 */
@Composable
private fun animateColor(target: Color): Color =
    animateColorAsState(
        targetValue = target,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "themeColor"
    ).value

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme = target.copy(
    primary = animateColor(target.primary),
    onPrimary = animateColor(target.onPrimary),
    primaryContainer = animateColor(target.primaryContainer),
    onPrimaryContainer = animateColor(target.onPrimaryContainer),
    inversePrimary = animateColor(target.inversePrimary),
    secondary = animateColor(target.secondary),
    onSecondary = animateColor(target.onSecondary),
    secondaryContainer = animateColor(target.secondaryContainer),
    onSecondaryContainer = animateColor(target.onSecondaryContainer),
    tertiary = animateColor(target.tertiary),
    onTertiary = animateColor(target.onTertiary),
    tertiaryContainer = animateColor(target.tertiaryContainer),
    onTertiaryContainer = animateColor(target.onTertiaryContainer),
    background = animateColor(target.background),
    onBackground = animateColor(target.onBackground),
    surface = animateColor(target.surface),
    onSurface = animateColor(target.onSurface),
    surfaceVariant = animateColor(target.surfaceVariant),
    onSurfaceVariant = animateColor(target.onSurfaceVariant),
    surfaceTint = animateColor(target.surfaceTint),
    inverseSurface = animateColor(target.inverseSurface),
    inverseOnSurface = animateColor(target.inverseOnSurface),
    error = animateColor(target.error),
    onError = animateColor(target.onError),
    errorContainer = animateColor(target.errorContainer),
    onErrorContainer = animateColor(target.onErrorContainer),
    outline = animateColor(target.outline),
    outlineVariant = animateColor(target.outlineVariant),
    scrim = animateColor(target.scrim),
    surfaceBright = animateColor(target.surfaceBright),
    surfaceDim = animateColor(target.surfaceDim),
    surfaceContainer = animateColor(target.surfaceContainer),
    surfaceContainerHigh = animateColor(target.surfaceContainerHigh),
    surfaceContainerHighest = animateColor(target.surfaceContainerHighest),
    surfaceContainerLow = animateColor(target.surfaceContainerLow),
    surfaceContainerLowest = animateColor(target.surfaceContainerLowest),
    primaryFixed = animateColor(target.primaryFixed),
    primaryFixedDim = animateColor(target.primaryFixedDim),
    onPrimaryFixed = animateColor(target.onPrimaryFixed),
    onPrimaryFixedVariant = animateColor(target.onPrimaryFixedVariant),
    secondaryFixed = animateColor(target.secondaryFixed),
    secondaryFixedDim = animateColor(target.secondaryFixedDim),
    onSecondaryFixed = animateColor(target.onSecondaryFixed),
    onSecondaryFixedVariant = animateColor(target.onSecondaryFixedVariant),
    tertiaryFixed = animateColor(target.tertiaryFixed),
    tertiaryFixedDim = animateColor(target.tertiaryFixedDim),
    onTertiaryFixed = animateColor(target.onTertiaryFixed),
    onTertiaryFixedVariant = animateColor(target.onTertiaryFixedVariant)
)

/**
 * Proveedor central de tema para la aplicación Nierika.
 * Implementa el soporte completo de Material Design 3 Expressive Dynamic Color (Android 12+),
 * esquemas estáticos con contraste accesible, tipografía dinámica y escalado universal.
 * Todos los tokens de color se animan con spring para transiciones suaves entre temas.
 */
@Composable
fun NierikaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    themeStyle: ColorSchemeStyle = ColorSchemeStyle.DYNAMIC,
    fontFamily: FontFamily = FontFamily.Default,
    fontScaleMultiplier: Float = 1.0f,
    motionScheme: NierikaMotionScheme<*> = NierikaMotionScheme.expressive<Any>(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val targetColorScheme = when {
        themeStyle == ColorSchemeStyle.DYNAMIC && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getM3ColorScheme(themeStyle, darkTheme)
    }

    // Animar todos los tokens para transiciones de tema fluidas
    val colorScheme = animateColorScheme(targetColorScheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    val dynamicTypography = remember(fontFamily) {
        createNierikaTypography(fontFamily)
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity, fontScaleMultiplier) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * fontScaleMultiplier
        )
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography
        ) {
            CompositionLocalProvider(
                LocalNierikaMotionScheme provides motionScheme,
                LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(fontFamily = fontFamily),
                content = content
            )
        }
    }
}
