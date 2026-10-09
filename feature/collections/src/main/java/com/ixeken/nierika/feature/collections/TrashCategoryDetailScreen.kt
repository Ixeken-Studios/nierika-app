package com.ixeken.nierika.feature.collections

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.TextButtonWithAnimatedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaRichTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.MediaItem

/**
 * Pantalla detallada de la categoría Trash (Papelera).
 * Muestra los elementos multimedia eliminados con soporte completo de selección múltiple,
 * diálogo de confirmación de permisos del sistema y sincronización simétrica.
 */
@Composable
fun TrashCategoryDetailScreen(
    trashItems: List<MediaItem>,
    isDeleteConfirmationEnabled: Boolean = true,
    onBackClick: () -> Unit,
    onEmptyTrashClick: () -> Unit,
    onRestoreAllClick: () -> Unit = {},
    onRestoreItem: (MediaItem, onAccepted: () -> Unit) -> Unit = { _, onAccepted -> onAccepted() },
    onRestoreBatch: (List<MediaItem>) -> Unit = {},
    onDeleteItemPermanently: (MediaItem, onAccepted: () -> Unit) -> Unit = { _, onAccepted -> onAccepted() },
    onDeleteBatchPermanently: (List<MediaItem>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current

    val sortedTrashItems = remember(trashItems) {
        trashItems.sortedByDescending { it.effectiveTrashedAtMillis }
    }

    val selectedMediaIds = remember { mutableStateListOf<Long>() }
    val isSelectionModeActive = selectedMediaIds.isNotEmpty()

    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var selectedPhotoId by remember { mutableStateOf<Long?>(null) }
    var displayedPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var displayedPhotoId by remember { mutableStateOf<Long?>(null) }
    if (selectedPhotoIndex != null) {
        displayedPhotoIndex = selectedPhotoIndex
        displayedPhotoId = selectedPhotoId
    }

    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showRestoreAllDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    BackHandler(onBack = {
        when {
            selectedPhotoIndex != null -> {
                selectedPhotoIndex = null
                selectedPhotoId = null
            }
            isSelectionModeActive -> selectedMediaIds.clear()
            else -> onBackClick()
        }
    })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar con transición fluida entre cabecera normal y modo selección
            AnimatedContent(
                targetState = isSelectionModeActive,
                transitionSpec = {
                    (fadeIn(animationSpec = motionScheme.defaultSpatialSpec()) + slideInVertically(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        initialOffsetY = { if (targetState) -it / 2 else it / 2 }
                    )) togetherWith (fadeOut(animationSpec = motionScheme.defaultSpatialSpec()) + slideOutVertically(
                        animationSpec = motionScheme.defaultSpatialSpec(),
                        targetOffsetY = { if (targetState) -it / 2 else it / 2 }
                    ))
                },
                label = "trashTopBarTransition"
            ) { selectionActive ->
                if (selectionActive) {
                    NierikaSelectionTopBar(
                        selectedCount = selectedMediaIds.size,
                        isAllSelected = selectedMediaIds.size == sortedTrashItems.size && sortedTrashItems.isNotEmpty(),
                        onClearSelection = { selectedMediaIds.clear() },
                        onToggleSelectAll = {
                            if (selectedMediaIds.size == sortedTrashItems.size) {
                                selectedMediaIds.clear()
                            } else {
                                selectedMediaIds.clear()
                                selectedMediaIds.addAll(sortedTrashItems.map { it.id })
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NierikaPlainTooltipBox(
                                tooltipText = stringResource(R.string.tooltip_back)
                            ) {
                                IconButtonWithAnimatedShape(
                                    onClick = onBackClick,
                                    buttonSize = 44.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.action_back),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.category_trash),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (sortedTrashItems.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                NierikaRichTooltipBox(
                                    title = stringResource(R.string.tooltip_restore_all_title),
                                    text = stringResource(R.string.tooltip_restore_all_desc)
                                ) {
                                    IconButtonWithAnimatedShape(
                                        onClick = { showRestoreAllDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.RestoreFromTrash,
                                            contentDescription = stringResource(R.string.action_restore_all),
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                NierikaRichTooltipBox(
                                    title = stringResource(R.string.tooltip_empty_trash_title),
                                    text = stringResource(R.string.tooltip_empty_trash_desc)
                                ) {
                                    IconButtonWithAnimatedShape(
                                        onClick = { showEmptyTrashDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteSweep,
                                            contentDescription = stringResource(R.string.action_empty_trash),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (sortedTrashItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.empty_trash_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.empty_trash_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(sortedTrashItems, key = { _, photo -> photo.id }) { index, photo ->
                        val isSelected = selectedMediaIds.contains(photo.id)
                        val selectionIndex = selectedMediaIds.indexOf(photo.id).takeIf { it >= 0 }?.let { it + 1 }
                        val cellShape = getGridItemShape(
                            index = index,
                            totalCount = sortedTrashItems.size,
                            columnsCount = 3,
                            cornerRadius = 16.dp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.0f)
                        ) {
                            NierikaPhotoGridItem(
                                uri = photo.uri,
                                displayName = photo.displayName,
                                isVideo = photo.isVideo,
                                isGif = photo.isGif,
                                durationMillis = photo.durationMillis,
                                isFavorite = false,
                                isSelected = isSelected,
                                selectionIndex = selectionIndex,
                                isSelectionModeActive = isSelectionModeActive,
                                cellShape = cellShape,
                                targetRatio = 1.0f,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        if (isSelected) {
                                            selectedMediaIds.remove(photo.id)
                                        } else {
                                            selectedMediaIds.add(photo.id)
                                        }
                                    } else {
                                        selectedPhotoIndex = index
                                        selectedPhotoId = photo.id
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (isSelected) {
                                        selectedMediaIds.remove(photo.id)
                                    } else {
                                        selectedMediaIds.add(photo.id)
                                    }
                                }
                            )

                            // Insignia M3 Expressive con contador de días restantes en papelera
                            val daysLeft = photo.daysRemainingInTrash
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${daysLeft}d",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        // Barra inferior de Selección para Papelera (Restaurar + Eliminar definitivamente)
        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(20f)
        ) {
            val selectedItemsList = sortedTrashItems.filter { selectedMediaIds.contains(it.id) }
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Acción: Restaurar Selección
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onRestoreBatch(selectedItemsList)
                                selectedMediaIds.clear()
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RestoreFromTrash,
                            contentDescription = stringResource(R.string.action_restore_photo),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.action_restore_photo),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Acción: Eliminar Definitivamente Selección
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isDeleteConfirmationEnabled) {
                                    showDeleteSelectedDialog = true
                                } else {
                                    onDeleteBatchPermanently(selectedItemsList)
                                    selectedMediaIds.clear()
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.action_delete),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Cerrar visor automáticamente si la papelera queda vacía
        LaunchedEffect(sortedTrashItems.isEmpty()) {
            if (sortedTrashItems.isEmpty() && selectedPhotoIndex != null) {
                selectedPhotoIndex = null
                selectedPhotoId = null
            }
        }

        // Visor de foto en pantalla completa con opcion Restore y Delete permanente
        AnimatedVisibility(
            visible = selectedPhotoIndex != null && sortedTrashItems.isNotEmpty(),
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(100f)
        ) {
            val resolvedIndex = remember(sortedTrashItems, displayedPhotoId, displayedPhotoIndex) {
                if (displayedPhotoId != null) {
                    val found = sortedTrashItems.indexOfFirst { it.id == displayedPhotoId }
                    if (found != -1) found else (displayedPhotoIndex ?: 0)
                } else {
                    (displayedPhotoIndex ?: 0)
                }.coerceIn(0, sortedTrashItems.lastIndex.coerceAtLeast(0))
            }
            com.ixeken.nierika.feature.viewer.MediaDetailScreen(
                items = sortedTrashItems,
                initialIndex = resolvedIndex,
                isInTrash = true,
                onRestoreConfirm = { item, onAccepted ->
                    onRestoreItem(item, onAccepted)
                },
                onDeletePermanentlyConfirm = { item, onAccepted ->
                    onDeleteItemPermanently(item, onAccepted)
                },
                onBackClick = {
                    selectedPhotoIndex = null
                    selectedPhotoId = null
                },
                onPageChange = { page, item ->
                    if (selectedPhotoIndex != null) {
                        selectedPhotoId = item.id
                        val found = sortedTrashItems.indexOfFirst { it.id == item.id }
                        selectedPhotoIndex = if (found != -1) found else page
                    }
                }
            )
        }

        // Diálogo de confirmación para restaurar todos
        if (showRestoreAllDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreAllDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.RestoreFromTrash,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.action_restore_all),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.restore_photo_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButtonWithAnimatedShape(
                        onClick = {
                            showRestoreAllDialog = false
                            onRestoreAllClick()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_restore_all),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButtonWithAnimatedShape(
                        onClick = { showRestoreAllDialog = false }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }

        // Diálogo de confirmación para vaciar papelera
        if (showEmptyTrashDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete_permanently_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButtonWithAnimatedShape(
                        onClick = {
                            showEmptyTrashDialog = false
                            onEmptyTrashClick()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButtonWithAnimatedShape(
                        onClick = { showEmptyTrashDialog = false }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }

        // Diálogo de confirmación para eliminar selección
        if (showDeleteSelectedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.action_delete),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.delete_permanently_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButtonWithAnimatedShape(
                        onClick = {
                            showDeleteSelectedDialog = false
                            val selectedItemsList = sortedTrashItems.filter { selectedMediaIds.contains(it.id) }
                            onDeleteBatchPermanently(selectedItemsList)
                            selectedMediaIds.clear()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButtonWithAnimatedShape(
                        onClick = { showDeleteSelectedDialog = false }
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}
