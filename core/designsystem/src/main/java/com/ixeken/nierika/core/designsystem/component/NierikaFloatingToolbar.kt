package com.ixeken.nierika.core.designsystem.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defaults para barras de herramientas flotantes según Material 3 Expressive.
 */
object FloatingToolbarDefaults {
    val ContainerShape: Shape = RoundedCornerShape(32.dp)
    val TonalElevation: Dp = 6.dp
    val ShadowElevation: Dp = 8.dp
    val ContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    val ItemSpacing: Dp = 10.dp
}

/**
 * Componente ScrollableHorizontalFloatingToolbar según la especificación Material 3 Expressive (M3 Compose).
 * Proporciona una barra de herramientas flotante con desplazamiento horizontal fluido, elevación tonal,
 * y contenedor con esquinas redondeadas para alojar múltiples acciones contextuales.
 */
@Composable
fun ScrollableHorizontalFloatingToolbar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    tonalElevation: Dp = FloatingToolbarDefaults.TonalElevation,
    shadowElevation: Dp = FloatingToolbarDefaults.ShadowElevation,
    contentPadding: PaddingValues = FloatingToolbarDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(FloatingToolbarDefaults.ItemSpacing),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
            content = content
        )
    }
}

/**
 * Componente HorizontalFloatingToolbar (no desplazable) según la especificación Material 3 Expressive.
 */
@Composable
fun HorizontalFloatingToolbar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    tonalElevation: Dp = FloatingToolbarDefaults.TonalElevation,
    shadowElevation: Dp = FloatingToolbarDefaults.ShadowElevation,
    contentPadding: PaddingValues = FloatingToolbarDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(FloatingToolbarDefaults.ItemSpacing),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
            content = content
        )
    }
}
