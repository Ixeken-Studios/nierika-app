package com.ixeken.nierika.feature.timeline

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoLibrary
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedFlowButtonGroup
import com.ixeken.nierika.core.domain.model.LayoutMode
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder

/**
 * Panel lateral modal (Side Sheet) de opciones de la galería.
 * Implementa las animaciones M3 Expressive Motion, Switch con iconos de estado Check/Close (M3 Standard)
 * y delimitación estricta de efectos táctiles clip-before-click.
 */
@Composable
fun LibraryOptionsSideSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    selectedLayoutMode: LayoutMode,
    onLayoutModeChange: (LayoutMode) -> Unit,
    showDatesInGrid: Boolean,
    onShowDatesInGridChange: (Boolean) -> Unit,
    selectedSortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    selectedFilter: QuickFilter,
    onFilterChange: (QuickFilter) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible, onBack = onDismiss)

    val motionScheme = MaterialTheme.nierikaMotionScheme

    // Scrim independiente: solo fade, sin deslizamiento
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
        exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec()),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )
    }

    // Panel lateral: solo slide horizontal
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = motionScheme.defaultSpatialSpec()
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = motionScheme.defaultSpatialSpec()
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(320.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = onDismiss
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.options_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(R.string.options_sorting),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                Spacer(modifier = Modifier.height(12.dp))

                val topToBottomLabel = stringResource(R.string.sort_top_to_bottom)
                val bottomToTopLabel = stringResource(R.string.sort_bottom_to_top)

                NierikaConnectedButtonGroup(
                    items = listOf(SortOrder.DESCENDING, SortOrder.ASCENDING),
                    selectedItem = selectedSortOrder,
                    onItemSelected = onSortOrderChange,
                    isLarge = true,
                    labelProvider = { sort ->
                        when (sort) {
                            SortOrder.DESCENDING -> topToBottomLabel
                            SortOrder.ASCENDING -> bottomToTopLabel
                        }
                    },
                    iconProvider = { sort ->
                        when (sort) {
                            SortOrder.DESCENDING -> Icons.Filled.ArrowDownward
                            SortOrder.ASCENDING -> Icons.Filled.ArrowUpward
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.options_filter),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val allItemsLabel = stringResource(R.string.filter_all_items)
                val favoritesLabel = stringResource(R.string.filter_favorites)
                val videosLabel = stringResource(R.string.filter_videos)
                val cameraLabel = stringResource(R.string.category_camera)
                val screenshotsLabel = stringResource(R.string.filter_screenshots)

                NierikaConnectedFlowButtonGroup(
                    items = listOf(QuickFilter.ALL, QuickFilter.FAVORITES, QuickFilter.VIDEOS, QuickFilter.CAMERA, QuickFilter.SCREENSHOTS),
                    selectedItem = selectedFilter,
                    onItemSelected = onFilterChange,
                    labelProvider = { filter ->
                        when (filter) {
                            QuickFilter.ALL -> allItemsLabel
                            QuickFilter.FAVORITES -> favoritesLabel
                            QuickFilter.VIDEOS -> videosLabel
                            QuickFilter.CAMERA -> cameraLabel
                            QuickFilter.SCREENSHOTS -> screenshotsLabel
                            QuickFilter.TRASH,
                            QuickFilter.NOT_IN_ALBUM -> ""
                        }
                    },
                    iconProvider = { filter ->
                        when (filter) {
                            QuickFilter.ALL -> Icons.Filled.Apps
                            QuickFilter.FAVORITES -> Icons.Filled.Favorite
                            QuickFilter.VIDEOS -> Icons.Filled.VideoLibrary
                            QuickFilter.CAMERA -> Icons.Filled.PhotoCamera
                            QuickFilter.SCREENSHOTS -> Icons.Filled.Smartphone
                            QuickFilter.TRASH,
                            QuickFilter.NOT_IN_ALBUM -> Icons.Filled.Apps
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.options_layout),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val dayLabel = stringResource(R.string.layout_day)
                val monthLabel = stringResource(R.string.layout_month)
                val comfortableLabel = stringResource(R.string.layout_comfortable)

                NierikaConnectedButtonGroup(
                    items = listOf(LayoutMode.DAY, LayoutMode.MONTH, LayoutMode.COMFORTABLE),
                    selectedItem = selectedLayoutMode,
                    onItemSelected = onLayoutModeChange,
                    isLarge = true,
                    labelProvider = { layout ->
                        when (layout) {
                            LayoutMode.DAY -> dayLabel
                            LayoutMode.MONTH -> monthLabel
                            LayoutMode.COMFORTABLE -> comfortableLabel
                        }
                    },
                    iconProvider = { layout ->
                        when (layout) {
                            LayoutMode.DAY -> Icons.Filled.GridView
                            LayoutMode.MONTH -> Icons.Filled.CalendarViewMonth
                            LayoutMode.COMFORTABLE -> Icons.Filled.AutoAwesomeMosaic
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onShowDatesInGridChange(!showDatesInGrid) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(R.string.options_show_dates),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SwitchWithThumbIcon(
                        checked = showDatesInGrid,
                        onCheckedChange = onShowDatesInGridChange
                    )
                }
            }
        }
    }
}
}
