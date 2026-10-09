package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Modelo inmutable para elementos de la barra de navegación flotante.
 */
data class NavItemData<T>(
    val destination: T,
    val label: String,
    val icon: ImageVector
)

/**
 * Barra de navegación flotante inspirada en Material 3 Expressive (MD3E).
 * Muestra las pestañas en una cápsula central (con píldora de color, icono y texto en la seleccionada,
 * y únicamente texto en las inactivas) acompañada de un botón satélite circular de búsqueda.
 */
@Composable
fun <T> NierikaFloatingNavBar(
    items: List<NavItemData<T>>,
    selectedDestination: T,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cápsula Principal con las pestañas
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 6.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.destination == selectedDestination

                    NierikaExpressiveFloatingNavItem(
                        label = item.label,
                        icon = item.icon,
                        isSelected = isSelected,
                        onClick = { onItemSelect(item.destination) },
                        indicatorColor = indicatorColor,
                        selectedContentColor = selectedContentColor,
                        unselectedContentColor = unselectedContentColor
                    )
                }
            }
        }

        // Botón satélite circular de búsqueda con Animated Shape Morphing (Círculo a Squircle)
        if (showSearchButton) {
            val searchInteractionSource = remember { MutableInteractionSource() }
            val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
            val animatedCornerPercent by animateIntAsState(
                targetValue = if (isSearchPressed) 26 else 50,
                animationSpec = motionScheme.fastSpatialSpec(),
                label = "searchSatMorph"
            )
            val satShape = RoundedCornerShape(animatedCornerPercent)

            NierikaPlainTooltipBox(
                tooltipText = stringResource(R.string.tooltip_search)
            ) {
                Surface(
                    shape = satShape,
                    color = backgroundColor,
                    shadowElevation = 6.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(satShape)
                        .clickable(
                            interactionSource = searchInteractionSource,
                            indication = ripple(),
                            onClick = onSearchClick
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.action_search),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NierikaExpressiveFloatingNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    indicatorColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val smoothDpSpec = motionScheme.fastSpatialSpec<Dp>()
    val smoothIntSizeSpec = motionScheme.fastSpatialSpec<IntSize>()

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) indicatorColor else Color.Transparent,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "navItemBgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
        animationSpec = motionScheme.fastEffectsSpec(),
        label = "navItemTextColor"
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 14.dp,
        animationSpec = smoothDpSpec,
        label = "navItemPadding"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // El icono únicamente se muestra cuando la pestaña está seleccionada
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = motionScheme.fastEffectsSpec()) + expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = smoothIntSizeSpec
                ),
                exit = fadeOut(animationSpec = motionScheme.fastEffectsSpec()) + shrinkHorizontally(
                    shrinkTowards = Alignment.Start,
                    animationSpec = smoothIntSizeSpec
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = selectedContentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

/**
 * Componente ShortNavigationBarWithHorizontalItems según la especificación Material 3 Expressive (M3 Compose).
 * Barra de navegación de altura reducida donde los elementos presentan disposición horizontal (icono y texto lado a lado)
 * e incluye el botón de búsqueda integrado.
 */
@Composable
fun <T> ShortNavigationBarWithHorizontalItems(
    items: List<NavItemData<T>>,
    selectedDestination: T,
    onItemSelect: (T) -> Unit,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    val isSelected = item.destination == selectedDestination
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val animatedCornerPercent by animateIntAsState(
                        targetValue = if (isPressed) 26 else 50,
                        animationSpec = motionScheme.fastSpatialSpec(),
                        label = "shortNavCorner"
                    )

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) indicatorColor else Color.Transparent,
                        animationSpec = motionScheme.fastEffectsSpec(),
                        label = "shortNavContainer"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
                        animationSpec = motionScheme.fastEffectsSpec(),
                        label = "shortNavContent"
                    )

                    val itemShape = RoundedCornerShape(animatedCornerPercent)

                    Surface(
                        shape = itemShape,
                        color = containerColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(itemShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(),
                                onClick = { onItemSelect(item.destination) }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.label,
                                color = contentColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (showSearchButton) {
                Spacer(modifier = Modifier.width(8.dp))
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.tooltip_search)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = onSearchClick,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        buttonSize = 48.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.action_search),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Barra de navegación de ancho completo (no flotante) basada en Material 3 / MD3 Expressive.
 */
@Composable
fun <T> NierikaFullWidthNavBar(
    items: List<NavItemData<T>>,
    selectedDestination: T,
    onItemSelect: (T) -> Unit,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    ShortNavigationBarWithHorizontalItems(
        items = items,
        selectedDestination = selectedDestination,
        onItemSelect = onItemSelect,
        onSearchClick = onSearchClick,
        showSearchButton = showSearchButton,
        modifier = modifier,
        backgroundColor = backgroundColor,
        indicatorColor = indicatorColor,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor
    )
}

/**
 * Cápsula de navegación flotante vertical estilo Foldable / Tablet (inspirada en conceptos ergonómicos para pantallas grandes).
 * Se ubica en el lateral derecho inferior (Alignment.BottomEnd) en zona de alcance del pulgar, agrupando las pestañas
 * y el botón de búsqueda en una única píldora flotante compacta con animaciones espaciales simétricas M3 Expressive.
 */
@Composable
fun <T> NierikaVerticalFloatingNavBar(
    items: List<NavItemData<T>>,
    selectedDestination: T,
    onItemSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cápsula Principal con las pestañas
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 6.dp,
            tonalElevation = 6.dp,
            modifier = Modifier.width(52.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    val isSelected = item.destination == selectedDestination

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) indicatorColor else Color.Transparent,
                        animationSpec = motionScheme.fastEffectsSpec(),
                        label = "verticalNavItemBgColor"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
                        animationSpec = motionScheme.fastEffectsSpec(),
                        label = "verticalNavItemContentColor"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    NierikaPlainTooltipBox(tooltipText = item.label) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(containerColor)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(),
                                    onClick = { onItemSelect(item.destination) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Botón satélite circular de búsqueda con Animated Shape Morphing (Círculo a Squircle), fuera de la cápsula
        if (showSearchButton) {
            val searchInteractionSource = remember { MutableInteractionSource() }
            val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
            val animatedCornerPercent by animateIntAsState(
                targetValue = if (isSearchPressed) 26 else 50,
                animationSpec = motionScheme.fastSpatialSpec(),
                label = "verticalSearchSatMorph"
            )
            val satShape = RoundedCornerShape(animatedCornerPercent)

            NierikaPlainTooltipBox(tooltipText = stringResource(R.string.tooltip_search)) {
                Surface(
                    shape = satShape,
                    color = backgroundColor,
                    shadowElevation = 6.dp,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(satShape)
                        .clickable(
                            interactionSource = searchInteractionSource,
                            indication = ripple(),
                            onClick = onSearchClick
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.action_search),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

