package com.ixeken.nierika.feature.collections.components

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.collections.AlbumLayout
import com.ixeken.nierika.feature.collections.AlbumSortBy
import com.ixeken.nierika.feature.collections.AlbumSorting

/**
 * Panel lateral modal (Side Sheet) de opciones del álbum.
 * Permite configurar ordenación, layout (Day, Month, Comfy), visualización de fechas,
 * exportación a formato ZIP y eliminación del álbum.
 */
@Composable
fun AlbumOptionsSideSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    sortBy: AlbumSortBy,
    onSortByChange: (AlbumSortBy) -> Unit,
    sortingMode: AlbumSorting,
    onSortingChange: (AlbumSorting) -> Unit,
    layoutMode: AlbumLayout,
    onLayoutChange: (AlbumLayout) -> Unit,
    showDatesInGrid: Boolean,
    onShowDatesChange: (Boolean) -> Unit,
    onExportAlbumClick: () -> Unit,
    onDeleteAlbumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible, onBack = onDismiss)

    val motionScheme = MaterialTheme.nierikaMotionScheme

    // Scrim independiente: solo fade, sin deslizamiento
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
        exit = fadeOut(animationSpec = motionScheme.defaultEffectsSpec()),
        modifier = modifier.fillMaxSize().zIndex(120f)
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

    // Panel lateral: solo slide horizontal estándar
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
        modifier = modifier.fillMaxSize().zIndex(121f)
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
                    // 1. Ordenar por (Fecha vs Orden de añadido)
                    Text(
                        text = stringResource(R.string.options_sort_by),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val dateLabel = stringResource(R.string.sort_by_date)
                    val dateAddedLabel = stringResource(R.string.sort_by_date_added)

                    NierikaConnectedButtonGroup(
                        items = listOf(AlbumSortBy.DATE, AlbumSortBy.DATE_ADDED),
                        selectedItem = sortBy,
                        onItemSelected = onSortByChange,
                        isLarge = true,
                        labelProvider = { sort ->
                            when (sort) {
                                AlbumSortBy.DATE -> dateLabel
                                AlbumSortBy.DATE_ADDED -> dateAddedLabel
                            }
                        },
                        iconProvider = { sort ->
                            when (sort) {
                                AlbumSortBy.DATE -> Icons.Filled.CalendarToday
                                AlbumSortBy.DATE_ADDED -> Icons.Filled.History
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Dirección de ordenación
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
                        items = listOf(AlbumSorting.TOP_TO_BOTTOM, AlbumSorting.BOTTOM_TO_TOP),
                        selectedItem = sortingMode,
                        onItemSelected = onSortingChange,
                        isLarge = true,
                        labelProvider = { sort ->
                            when (sort) {
                                AlbumSorting.TOP_TO_BOTTOM -> topToBottomLabel
                                AlbumSorting.BOTTOM_TO_TOP -> bottomToTopLabel
                            }
                        },
                        iconProvider = { sort ->
                            when (sort) {
                                AlbumSorting.TOP_TO_BOTTOM -> Icons.Filled.ArrowDownward
                                AlbumSorting.BOTTOM_TO_TOP -> Icons.Filled.ArrowUpward
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
                        items = listOf(AlbumLayout.DAY, AlbumLayout.MONTH, AlbumLayout.COMFORTABLE),
                        selectedItem = layoutMode,
                        onItemSelected = onLayoutChange,
                        isLarge = true,
                        labelProvider = { layout ->
                            when (layout) {
                                AlbumLayout.DAY -> dayLabel
                                AlbumLayout.MONTH -> monthLabel
                                AlbumLayout.COMFORTABLE -> comfortableLabel
                            }
                        },
                        iconProvider = { layout ->
                            when (layout) {
                                AlbumLayout.DAY -> Icons.Filled.GridView
                                AlbumLayout.MONTH -> Icons.Filled.CalendarViewMonth
                                AlbumLayout.COMFORTABLE -> Icons.Filled.AutoAwesomeMosaic
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onShowDatesChange(!showDatesInGrid) }
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        SwitchWithThumbIcon(
                            checked = showDatesInGrid,
                            onCheckedChange = onShowDatesChange
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Botón para exportar álbum a .ZIP
                    ButtonWithAnimatedShape(
                        onClick = onExportAlbumClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderZip,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.album_detail_export_album),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón para borrar álbum con animación de morphing
                    ButtonWithAnimatedShape(
                        onClick = onDeleteAlbumClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.album_detail_delete_album),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
