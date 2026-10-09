package com.ixeken.nierika.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Proveedor del sistema de animación de movimiento basado en la especificación Material 3 Expressive Motion.
 * Permite cambiar entre el esquema Expressive (con rebote físico elástico) y el esquema Standard (sobrio).
 */
@Immutable
class NierikaMotionScheme<T>(
    val fastSpatialSpec: FiniteAnimationSpec<T>,
    val defaultSpatialSpec: FiniteAnimationSpec<T>,
    val slowSpatialSpec: FiniteAnimationSpec<T>,
    val fastEffectsSpec: FiniteAnimationSpec<T>,
    val defaultEffectsSpec: FiniteAnimationSpec<T>,
    val slowEffectsSpec: FiniteAnimationSpec<T>
) {
    @Suppress("UNCHECKED_CAST")
    fun <V> fastSpatialSpec(): FiniteAnimationSpec<V> = fastSpatialSpec as FiniteAnimationSpec<V>

    @Suppress("UNCHECKED_CAST")
    fun <V> defaultSpatialSpec(): FiniteAnimationSpec<V> = defaultSpatialSpec as FiniteAnimationSpec<V>

    @Suppress("UNCHECKED_CAST")
    fun <V> slowSpatialSpec(): FiniteAnimationSpec<V> = slowSpatialSpec as FiniteAnimationSpec<V>

    @Suppress("UNCHECKED_CAST")
    fun <V> fastEffectsSpec(): FiniteAnimationSpec<V> = fastEffectsSpec as FiniteAnimationSpec<V>

    @Suppress("UNCHECKED_CAST")
    fun <V> defaultEffectsSpec(): FiniteAnimationSpec<V> = defaultEffectsSpec as FiniteAnimationSpec<V>

    @Suppress("UNCHECKED_CAST")
    fun <V> slowEffectsSpec(): FiniteAnimationSpec<V> = slowEffectsSpec as FiniteAnimationSpec<V>

    companion object {
        fun <T> expressive(): NierikaMotionScheme<T> = NierikaMotionScheme(
            fastSpatialSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy),
            defaultSpatialSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
            slowSpatialSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioLowBouncy),
            fastEffectsSpec = spring(stiffness = Spring.StiffnessMediumLow),
            defaultEffectsSpec = spring(stiffness = Spring.StiffnessLow),
            slowEffectsSpec = spring(stiffness = Spring.StiffnessVeryLow)
        )

        fun <T> standard(): NierikaMotionScheme<T> = NierikaMotionScheme(
            fastSpatialSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
            defaultSpatialSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
            slowSpatialSpec = spring(stiffness = Spring.StiffnessVeryLow, dampingRatio = Spring.DampingRatioNoBouncy),
            fastEffectsSpec = spring(stiffness = Spring.StiffnessMediumLow),
            defaultEffectsSpec = spring(stiffness = Spring.StiffnessLow),
            slowEffectsSpec = spring(stiffness = Spring.StiffnessVeryLow)
        )
    }
}

val LocalNierikaMotionScheme = staticCompositionLocalOf<NierikaMotionScheme<*>> {
    NierikaMotionScheme.expressive<Any>()
}

/**
 * Extensión helper para obtener el NierikaMotionScheme actual desde MaterialTheme.
 */
val MaterialTheme.nierikaMotionScheme: NierikaMotionScheme<Any>
    @Composable
    @Suppress("UNCHECKED_CAST")
    get() = LocalNierikaMotionScheme.current as NierikaMotionScheme<Any>
