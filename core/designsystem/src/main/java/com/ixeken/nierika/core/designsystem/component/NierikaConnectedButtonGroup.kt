package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Componente Material 3 Expressive Connected Button Group con morphing dinámico de forma.
 * Transiciona suavemente la forma del botón seleccionado a una píldora manteniendo
 * esquinas suavemente redondeadas en los elementos no seleccionados, según especificaciones M3 y MotionScheme.
 */
@Composable
fun <T> NierikaConnectedButtonGroup(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    iconProvider: (T) -> ImageVector,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    selectedContainerColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContainerColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val actualSelectedContainer = if (selectedContainerColor != Color.Unspecified) selectedContainerColor else MaterialTheme.colorScheme.primary
    val actualSelectedContent = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onPrimary
    val actualUnselectedContainer = if (unselectedContainerColor != Color.Unspecified) unselectedContainerColor else MaterialTheme.colorScheme.surfaceContainerHighest
    val actualUnselectedContent = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem

            val cornerRadius by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 12.dp,
                animationSpec = motionScheme.fastSpatialSpec(),
                label = "connectedButtonShapeMorph"
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) actualSelectedContainer else actualUnselectedContainer,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "connectedButtonBg"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) actualSelectedContent else actualUnselectedContent,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "connectedButtonFg"
            )

            val interactionSource = remember { MutableInteractionSource() }

            val itemShape = RoundedCornerShape(cornerRadius)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(itemShape)
                    .background(containerColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onItemSelected(item) }
                    )
                    .padding(vertical = if (isLarge) 16.dp else 10.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLarge) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = iconProvider(item),
                            contentDescription = labelProvider(item),
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = labelProvider(item),
                            color = contentColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = iconProvider(item),
                            contentDescription = labelProvider(item),
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = labelProvider(item),
                            color = contentColor,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente Material 3 Expressive Flow Connected Button Group para listas expandibles de filtros.
 * Organiza los botones en un FlowRow adaptable con morphing de forma dinámico (Píldora al seleccionar).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> NierikaConnectedFlowButtonGroup(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    iconProvider: ((T) -> ImageVector)? = null,
    selectedContainerColor: Color = Color.Unspecified,
    selectedContentColor: Color = Color.Unspecified,
    unselectedContainerColor: Color = Color.Unspecified,
    unselectedContentColor: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
    fontSize: TextUnit = 14.sp,
    iconSize: Dp = 18.dp
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val actualSelectedContainer = if (selectedContainerColor != Color.Unspecified) selectedContainerColor else MaterialTheme.colorScheme.primary
    val actualSelectedContent = if (selectedContentColor != Color.Unspecified) selectedContentColor else MaterialTheme.colorScheme.onPrimary
    val actualUnselectedContainer = if (unselectedContainerColor != Color.Unspecified) unselectedContainerColor else MaterialTheme.colorScheme.surfaceContainerHighest
    val actualUnselectedContent = if (unselectedContentColor != Color.Unspecified) unselectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem

            val cornerRadius by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 12.dp,
                animationSpec = motionScheme.fastSpatialSpec(),
                label = "connectedFlowButtonShapeMorph"
            )

            val containerColor by animateColorAsState(
                targetValue = if (isSelected) actualSelectedContainer else actualUnselectedContainer,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "connectedFlowButtonBg"
            )

            val contentColor by animateColorAsState(
                targetValue = if (isSelected) actualSelectedContent else actualUnselectedContent,
                animationSpec = motionScheme.fastEffectsSpec(),
                label = "connectedFlowButtonFg"
            )

            val interactionSource = remember { MutableInteractionSource() }

            val itemShape = RoundedCornerShape(cornerRadius)

            Box(
                modifier = Modifier
                    .clip(itemShape)
                    .background(containerColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onItemSelected(item) }
                    )
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (iconProvider != null) {
                        Icon(
                            imageVector = iconProvider(item),
                            contentDescription = labelProvider(item),
                            tint = contentColor,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    Text(
                        text = labelProvider(item),
                        color = contentColor,
                        fontSize = fontSize,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
