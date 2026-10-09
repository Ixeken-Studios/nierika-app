package com.ixeken.nierika.feature.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.core.designsystem.component.NierikaTopAppBar
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Sección de barra superior para la pantalla de Timeline.
 * Se encarga de alternar limpiamente con animaciones espaciales simétricas entre la barra principal de la galería
 * y la barra de selección múltiple, aplicando un gradiente scrim vertical sobre el área de la barra de estado
 * para garantizar legibilidad óptima sobre el contenido en scroll sin recomposiciones del grid.
 */
@Composable
fun TimelineTopBarSection(
    isSelectionModeActive: Boolean,
    libraryTitle: String,
    currentSubtitle: String,
    splitCountContent: (@Composable () -> Unit)?,
    appBarContentColor: Color,
    showAppLogo: Boolean,
    appLogoRes: Int,
    onToggleSideSheet: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    selectedCount: Int,
    isAllSelected: Boolean,
    onClearSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val topBarScrimBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.background.copy(alpha = 0.60f),
            Color.Transparent
        )
    )

    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !isSelectionModeActive,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarScrimBrush)
                    .statusBarsPadding()
            ) {
                NierikaTopAppBar(
                    title = libraryTitle,
                    subtitle = currentSubtitle,
                    subtitleContent = splitCountContent,
                    backgroundColor = Color.Transparent,
                    contentColor = appBarContentColor,
                    showAppLogo = showAppLogo,
                    appLogoRes = appLogoRes,
                    actions = {
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_more_options)
                        ) {
                            IconButtonWithAnimatedShape(
                                onClick = { onToggleSideSheet(true) },
                                contentColor = appBarContentColor
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.action_more_options),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.action_settings)
                        ) {
                            IconButtonWithAnimatedShape(
                                onClick = onOpenSettings,
                                contentColor = appBarContentColor
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(R.string.action_settings),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(30f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarScrimBrush)
                    .statusBarsPadding()
            ) {
                NierikaSelectionTopBar(
                    selectedCount = selectedCount,
                    isAllSelected = isAllSelected,
                    onClearSelection = onClearSelection,
                    onToggleSelectAll = onToggleSelectAll
                )
            }
        }
    }
}
