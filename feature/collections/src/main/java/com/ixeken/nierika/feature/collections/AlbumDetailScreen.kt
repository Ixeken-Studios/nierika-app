package com.ixeken.nierika.feature.collections

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.HorizontalFloatingToolbar
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaRichTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionBottomBar
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import com.ixeken.nierika.core.designsystem.component.TextButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.feature.collections.components.AlbumOptionsSideSheet

enum class AlbumSortBy {
    DATE,
    DATE_ADDED
}

enum class AlbumSorting {
    TOP_TO_BOTTOM,
    BOTTOM_TO_TOP
}

enum class AlbumLayout {
    DAY,
    MONTH,
    COMFORTABLE
}

/**
 * Pantalla de Detalle de Álbum tipo Mini-Timeline.
 * Muestra las fotos pertenecientes al álbum con menú de opciones, selector de fotos y edición.
 * Oculta la barra de navegación general y muestra en su lugar los botones de agregar fotos y editar álbum.
 */
@Composable
fun AlbumDetailScreen(
    album: Album,
    onBackClick: () -> Unit,
    onDeleteAlbumClick: () -> Unit = {},
    onPickMedia: ((List<MediaItem>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val rawAlbumPhotos by viewModel.getMediaForAlbum(album.id).collectAsState(initial = emptyList())
    val pendingTrashIds by viewModel.pendingTrashIds.collectAsState()
    val albumPhotos = remember(rawAlbumPhotos, pendingTrashIds) {
        if (pendingTrashIds.isEmpty()) rawAlbumPhotos else rawAlbumPhotos.filter { it.id !in pendingTrashIds }
    }
    val currentAlbumState by viewModel.getAlbumById(album.id).collectAsState(initial = album)
    val activeAlbum = currentAlbumState ?: album

    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val exportState by viewModel.albumExportState.collectAsState()

    val createZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destinationUri ->
        if (destinationUri != null) {
            viewModel.exportAlbumToZip(context, activeAlbum, destinationUri)
        }
    }

    var pendingTrashItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingTrashOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (pendingTrashItems.isNotEmpty()) {
                viewModel.moveToTrashBatch(pendingTrashItems)
                pendingTrashItems = emptyList()
            }
            pendingTrashOnAccepted?.invoke()
            viewModel.refreshSilent()
        } else {
            pendingTrashItems = emptyList()
        }
        pendingTrashOnAccepted = null
    }

    fun requestTrash(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingTrashItems = items
        pendingTrashOnAccepted = onAccepted
        val intentSender = viewModel.createTrashIntentSender(context, items, isTrash = true)
        if (intentSender != null) {
            trashLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(intentSender).build())
        } else {
            viewModel.moveToTrashBatch(items)
            pendingTrashItems = emptyList()
            onAccepted?.invoke()
            pendingTrashOnAccepted = null
        }
    }

    LaunchedEffect(exportState.isCompleted, exportState.error) {
        if (exportState.isCompleted) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(
                context,
                context.getString(R.string.export_album_success, exportState.albumName),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetAlbumExportState()
        } else if (exportState.error != null) {
            Toast.makeText(
                context,
                context.getString(R.string.export_album_error, exportState.error),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetAlbumExportState()
        }
    }

    var isOptionsVisible by remember { mutableStateOf(false) }
    var isAddToOtherAlbumVisible by remember { mutableStateOf(false) }
    var isAddPhotosVisible by remember { mutableStateOf(false) }
    var isEditFormVisible by remember { mutableStateOf(false) }
    var isCreateNewAlbumFromViewerVisible by remember { mutableStateOf(false) }
    var viewerPreselectedPhotos by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isDeleteConfirmDialogVisible by remember { mutableStateOf(false) }

    var sortBy by remember { mutableStateOf(AlbumSortBy.DATE) }
    var sortingMode by remember { mutableStateOf(AlbumSorting.TOP_TO_BOTTOM) }
    var layoutMode by remember { mutableStateOf(AlbumLayout.DAY) }
    var showDatesInGrid by remember(userPreferences.showDatesInCollectionsGrid) { mutableStateOf(userPreferences.showDatesInCollectionsGrid) }

    val todayStr = stringResource(R.string.date_today)
    val yesterdayStr = stringResource(R.string.date_yesterday)

    val selectedMediaIds = remember { mutableStateListOf<Long>() }
    val isSelectionModeActive = selectedMediaIds.isNotEmpty()
    var selectedMediaIndex by remember { mutableStateOf<Int?>(null) }
    var selectedMediaId by remember { mutableStateOf<Long?>(null) }
    var displayedMediaIndex by remember { mutableStateOf<Int?>(null) }
    var displayedMediaId by remember { mutableStateOf<Long?>(null) }
    if (selectedMediaIndex != null) {
        displayedMediaIndex = selectedMediaIndex
        displayedMediaId = selectedMediaId
    }

    val displayedPhotos = remember(albumPhotos, sortBy, sortingMode) {
        when (sortBy) {
            AlbumSortBy.DATE -> {
                if (sortingMode == AlbumSorting.BOTTOM_TO_TOP) {
                    albumPhotos.sortedBy { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
                } else {
                    albumPhotos.sortedByDescending { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
                }
            }
            AlbumSortBy.DATE_ADDED -> {
                if (sortingMode == AlbumSorting.BOTTOM_TO_TOP) {
                    albumPhotos.sortedBy { it.addedToAlbumMillis.takeIf { t -> t > 0 } ?: (it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis) }
                } else {
                    albumPhotos.sortedByDescending { it.addedToAlbumMillis.takeIf { t -> t > 0 } ?: (it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis) }
                }
            }
        }
    }

    val groupedItems = remember(displayedPhotos, showDatesInGrid, layoutMode, sortingMode, sortBy, todayStr, yesterdayStr) {
        if (showDatesInGrid) {
            groupMediaItemsByDateForCollections(displayedPhotos, todayStr, yesterdayStr, layoutMode, sortingMode, sortBy)
        } else {
            emptyList()
        }
    }

    BackHandler(onBack = {
        when {
            selectedMediaIndex != null -> {
                selectedMediaIndex = null
                selectedMediaId = null
            }
            isCreateNewAlbumFromViewerVisible -> isCreateNewAlbumFromViewerVisible = false
            isSelectionModeActive -> selectedMediaIds.clear()
            isOptionsVisible -> isOptionsVisible = false
            isAddPhotosVisible -> isAddPhotosVisible = false
            isEditFormVisible -> isEditFormVisible = false
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
            // Header Top Bar con transición fluida entre cabecera normal y modo selección
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
                label = "albumTopBarTransition"
            ) { selectionActive ->
                if (selectionActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_cancel_selection)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable(onClick = { selectedMediaIds.clear() })
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.action_back),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.items_selected_count, selectedMediaIds.size),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_select_all)
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedMediaIds.size == displayedPhotos.size) {
                                        selectedMediaIds.clear()
                                    } else {
                                        selectedMediaIds.clear()
                                        selectedMediaIds.addAll(displayedPhotos.map { it.id })
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SelectAll,
                                    contentDescription = stringResource(R.string.tooltip_select_all),
                                    tint = if (selectedMediaIds.size == displayedPhotos.size && displayedPhotos.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            NierikaPlainTooltipBox(
                                tooltipText = stringResource(R.string.tooltip_back)
                            ) {
                                IconButtonWithAnimatedShape(
                                    onClick = onBackClick
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.action_back),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeAlbum.name.ifEmpty { stringResource(R.string.album_form_album_title) },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.album_items_count, displayedPhotos.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (activeAlbum.tags.isNotEmpty()) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = activeAlbum.tags.joinToString(" ") { "#$it" },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_album_options)
                        ) {
                            IconButtonWithAnimatedShape(
                                onClick = { isOptionsVisible = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.album_detail_options_title),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Timeline Grid de Fotos del Álbum
            if (displayedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.empty_timeline_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        ButtonWithAnimatedShape(
                            onClick = { isAddPhotosVisible = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(R.string.album_form_add_photo))
                        }
                    }
                }
            } else {
                val columnsCount = when (layoutMode) {
                    AlbumLayout.DAY -> 3
                    AlbumLayout.MONTH -> 4
                    AlbumLayout.COMFORTABLE -> 2
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 4.dp,
                        end = 4.dp,
                        top = 4.dp,
                        bottom = 110.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (showDatesInGrid) {
                        groupedItems.forEach { (dateHeader, groupPhotos) ->
                            item(
                                span = { GridItemSpan(maxLineSpan) },
                                key = "header_$dateHeader",
                                contentType = "date_header"
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp, bottom = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dateHeader,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            items(
                                count = groupPhotos.size,
                                key = { photoIndex -> groupPhotos[photoIndex].id },
                                contentType = { "photo_item" },
                                span = { GridItemSpan(1) }
                            ) { photoIndex ->
                                val photo = groupPhotos[photoIndex]
                                val isSelected = selectedMediaIds.contains(photo.id)
                                val selectionIndex = if (isSelected) selectedMediaIds.indexOf(photo.id) + 1 else null
                                val globalIndex = displayedPhotos.indexOf(photo).takeIf { it >= 0 } ?: photoIndex
                                val cellShape = getGridItemShape(
                                    index = photoIndex,
                                    totalCount = groupPhotos.size,
                                    columnsCount = columnsCount,
                                    cornerRadius = 16.dp
                                )

                                NierikaPhotoGridItem(
                                    uri = photo.uri,
                                    displayName = photo.displayName,
                                    isVideo = photo.isVideo,
                                    isGif = photo.isGif,
                                    durationMillis = photo.durationMillis,
                                    isFavorite = photo.isFavorite,
                                    isSelected = isSelected,
                                    selectionIndex = selectionIndex,
                                    isSelectionModeActive = isSelectionModeActive,
                                    cellShape = cellShape,
                                    targetRatio = 1.0f,
                                    onClick = {
                                        if (isSelectionModeActive) {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            if (isSelected) selectedMediaIds.remove(photo.id)
                                            else selectedMediaIds.add(photo.id)
                                        } else {
                                            selectedMediaIndex = globalIndex
                                            selectedMediaId = photo.id
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionModeActive) {
                                            if (!selectedMediaIds.contains(photo.id)) selectedMediaIds.add(photo.id)
                                        } else {
                                            if (isSelected) selectedMediaIds.remove(photo.id) else selectedMediaIds.add(photo.id)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = displayedPhotos,
                            key = { _, photo -> photo.id },
                            contentType = { _, _ -> "photo_item" }
                        ) { index, photo ->
                            val isSelected = selectedMediaIds.contains(photo.id)
                            val selectionIndex = if (isSelected) selectedMediaIds.indexOf(photo.id) + 1 else null
                            val cellShape = getGridItemShape(
                                index = index,
                                totalCount = displayedPhotos.size,
                                columnsCount = columnsCount,
                                cornerRadius = 16.dp
                            )

                            NierikaPhotoGridItem(
                                uri = photo.uri,
                                displayName = photo.displayName,
                                isVideo = photo.isVideo,
                                isGif = photo.isGif,
                                durationMillis = photo.durationMillis,
                                isFavorite = photo.isFavorite,
                                isSelected = isSelected,
                                selectionIndex = selectionIndex,
                                isSelectionModeActive = isSelectionModeActive,
                                cellShape = cellShape,
                                targetRatio = 1.0f,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        if (isSelected) selectedMediaIds.remove(photo.id)
                                        else selectedMediaIds.add(photo.id)
                                    } else {
                                        selectedMediaIndex = index
                                        selectedMediaId = photo.id
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isSelectionModeActive) {
                                        if (!selectedMediaIds.contains(photo.id)) selectedMediaIds.add(photo.id)
                                    } else {
                                        if (isSelected) selectedMediaIds.remove(photo.id) else selectedMediaIds.add(photo.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }



        // Barra inferior de Selección Múltiple (cuando hay fotos seleccionadas)
        AnimatedVisibility(
            visible = isSelectionModeActive,
            enter = slideInVertically(
                initialOffsetY = { it * 2 },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutVertically(
                targetOffsetY = { it * 2 },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(20f)
        ) {
            val selectedPhotosList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
            val allSelectedAreFavorites = selectedPhotosList.isNotEmpty() && selectedPhotosList.all { it.isFavorite }

            NierikaSelectionBottomBar(
                onShareClick = {
                    if (selectedPhotosList.isNotEmpty()) {
                        val uris = ArrayList<Uri>(selectedPhotosList.map { Uri.parse(it.uri) })
                        var hasImage = false
                        var hasVideo = false
                        selectedPhotosList.forEach { item ->
                            if (item.mimeType.startsWith("video")) {
                                hasVideo = true
                            } else {
                                hasImage = true
                            }
                        }
                        val mimeType = when {
                            hasImage && hasVideo -> "*/*"
                            hasVideo -> "video/*"
                            else -> "image/*"
                        }
                        val shareIntent = if (uris.size == 1) {
                            Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_STREAM, uris[0])
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        } else {
                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = mimeType
                                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.action_share)))
                        selectedMediaIds.clear()
                    }
                },
                onFavoriteClick = {
                    val newFavState = !allSelectedAreFavorites
                    selectedPhotosList.forEach { photo ->
                        viewModel.onToggleFavorite(photo.id, newFavState)
                    }
                    selectedMediaIds.clear()
                },
                isAllFavorites = allSelectedAreFavorites,
                onAddToAlbumClick = {
                    isAddToOtherAlbumVisible = true
                },
                onRemoveFromAlbumClick = {
                    viewModel.onRemoveMediaBatchFromAlbum(activeAlbum, selectedPhotosList)
                    Toast.makeText(context, context.getString(R.string.removed_from_album_success, activeAlbum.name), Toast.LENGTH_SHORT).show()
                    selectedMediaIds.clear()
                },
                onDeleteClick = {
                    requestTrash(selectedPhotosList)
                    selectedMediaIds.clear()
                },
                onSelectClick = if (onPickMedia != null) { { onPickMedia(selectedPhotosList) } } else null,
                deleteLabel = stringResource(R.string.selection_action_trash)
            )
        }

        // BottomSheet para Agregar Fotos a otro Álbum
        AddToAlbumBottomSheet(
            isVisible = isAddToOtherAlbumVisible,
            albums = uiState.albums.filter { it.id != activeAlbum.id },
            onDismiss = { isAddToOtherAlbumVisible = false },
            onCreateNewAlbumClick = {
                val selectedPhotosList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
                viewerPreselectedPhotos = selectedPhotosList
                isAddToOtherAlbumVisible = false
                isCreateNewAlbumFromViewerVisible = true
            },
            onAlbumSelected = { targetAlbum ->
                val selectedPhotosList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
                viewModel.onAddMediaBatchToAlbum(targetAlbum, selectedPhotosList) { result ->
                    val msg = when {
                        result.addedCount == 0 -> {
                            if (selectedPhotosList.size == 1) {
                                context.getString(R.string.photo_already_in_album, targetAlbum.name)
                            } else {
                                context.getString(R.string.items_already_in_album, targetAlbum.name)
                            }
                        }
                        result.alreadyPresentCount > 0 -> {
                            context.getString(R.string.added_to_album_partial_success, result.addedCount, targetAlbum.name, result.alreadyPresentCount)
                        }
                        else -> {
                            context.getString(R.string.added_to_album_success, result.addedCount, targetAlbum.name)
                        }
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                isAddToOtherAlbumVisible = false
                selectedMediaIds.clear()
            }
        )

        // Barra Flotante Inferior de Acciones del Álbum (Reemplaza a la NavBar general mientras está en el álbum)
        AnimatedVisibility(
            visible = !isSelectionModeActive && selectedMediaIndex == null,
            enter = slideInVertically(
                initialOffsetY = { it * 3 },
                animationSpec = motionScheme.fastSpatialSpec()
            ),
            exit = slideOutVertically(
                targetOffsetY = { it * 3 },
                animationSpec = motionScheme.fastSpatialSpec()
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .zIndex(10f)
        ) {
            HorizontalFloatingToolbar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón 1: Agregar más fotos
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.album_form_add_photo)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            isAddPhotosVisible = true
                        },
                        buttonSize = 48.dp,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.album_form_add_photo),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }



                // Botón 3: Editar álbum
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.album_form_edit_title)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            isEditFormVisible = true
                        },
                        buttonSize = 48.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.album_form_edit_title),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Botón 3: Seleccionar fotos
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.action_select_photos)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            if (displayedPhotos.isNotEmpty()) {
                                selectedMediaIds.add(displayedPhotos.first().id)
                            }
                        },
                        buttonSize = 48.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.action_select_photos),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Cerrar visor automáticamente si la lista de fotos del álbum queda vacía
        LaunchedEffect(displayedPhotos.isEmpty()) {
            if (displayedPhotos.isEmpty() && selectedMediaIndex != null) {
                selectedMediaIndex = null
                selectedMediaId = null
            }
        }

        // Visor de Medios a pantalla completa cuando se pulsa una foto
        AnimatedVisibility(
            visible = selectedMediaIndex != null && displayedPhotos.isNotEmpty(),
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
            val resolvedIndex = remember(displayedPhotos, displayedMediaId, displayedMediaIndex) {
                if (displayedMediaId != null) {
                    val found = displayedPhotos.indexOfFirst { it.id == displayedMediaId }
                    if (found != -1) found else (displayedMediaIndex ?: 0)
                } else {
                    (displayedMediaIndex ?: 0)
                }.coerceIn(0, displayedPhotos.lastIndex.coerceAtLeast(0))
            }
            com.ixeken.nierika.feature.viewer.MediaDetailScreen(
                items = displayedPhotos,
                initialIndex = resolvedIndex,
                onBackClick = {
                    selectedMediaIndex = null
                    selectedMediaId = null
                },
                onDeleteConfirm = { item, onAccepted ->
                    requestTrash(listOf(item), onAccepted)
                },
                onDeletePermanentlyConfirm = { item, onAccepted ->
                    viewModel.deletePermanently(listOf(item))
                    onAccepted()
                },
                onToggleFavorite = { item ->
                    viewModel.onToggleFavorite(item.id, item.isFavorite)
                },
                onPickMedia = if (onPickMedia != null) { { item -> onPickMedia(listOf(item)) } } else null,
                onPageChange = { page, item ->
                    if (selectedMediaIndex != null) {
                        selectedMediaId = item.id
                        val found = displayedPhotos.indexOfFirst { it.id == item.id }
                        selectedMediaIndex = if (found != -1) found else page
                    }
                },
                customAlbums = uiState.albums,
                onCreateAlbumClick = { photo ->
                    viewerPreselectedPhotos = listOf(photo)
                    isCreateNewAlbumFromViewerVisible = true
                },
                onAddToAlbumClick = { item, targetAlbum ->
                    viewModel.onAddMediaBatchToAlbum(targetAlbum, listOf(item)) { result ->
                        val msg = if (result.addedCount > 0) {
                            context.getString(R.string.added_to_album_success, result.addedCount, targetAlbum.name)
                        } else {
                            context.getString(R.string.photo_already_in_album, targetAlbum.name)
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onRemoveFromAlbumClick = { item, onAccepted ->
                    viewModel.onRemoveMediaBatchFromAlbum(activeAlbum, listOf(item))
                    onAccepted()
                    Toast.makeText(context, context.getString(R.string.removed_from_album_success, activeAlbum.name), Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Selector de Fotos para agregar al Álbum
        AnimatedVisibility(
            visible = isAddPhotosVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(110f)
        ) {
            AlbumPhotoPickerScreen(
                albumTitle = activeAlbum.name,
                initialSelectedPhotos = displayedPhotos,
                mediaPagingFlow = viewModel.mediaPagingData,
                onDismiss = { isAddPhotosVisible = false },
                onPhotosSelected = { selectedItems ->
                    viewModel.onAddMediaBatchToAlbum(activeAlbum, selectedItems) { result ->
                        val msg = when {
                            result.addedCount == 0 -> {
                                if (selectedItems.size == 1) {
                                    context.getString(R.string.photo_already_in_album, activeAlbum.name)
                                } else {
                                    context.getString(R.string.items_already_in_album, activeAlbum.name)
                                }
                            }
                            result.alreadyPresentCount > 0 -> {
                                context.getString(R.string.added_to_album_partial_success, result.addedCount, activeAlbum.name, result.alreadyPresentCount)
                            }
                            else -> {
                                context.getString(R.string.added_to_album_success, result.addedCount, activeAlbum.name)
                            }
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    isAddPhotosVisible = false
                }
            )
        }

        // Formulario de Editar Álbum con física spring de arriba hacia abajo
        AnimatedVisibility(
            visible = isEditFormVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            ),
            modifier = Modifier.fillMaxSize().zIndex(110f)
        ) {
            AlbumFormScreen(
                mode = AlbumFormMode.EDIT,
                initialTitle = activeAlbum.name,
                initialDescription = activeAlbum.description,
                initialCoverUri = activeAlbum.coverUri,
                initialTags = activeAlbum.tags,
                initialPhotos = displayedPhotos,
                onCloseClick = { isEditFormVisible = false },
                onDeleteAlbumClick = {
                    isEditFormVisible = false
                    isDeleteConfirmDialogVisible = true
                },
                onSaveClick = { title, description, coverUri, photos, tags ->
                    viewModel.onUpdateAlbum(
                        albumId = activeAlbum.id,
                        name = title.ifBlank { activeAlbum.name },
                        description = description,
                        coverUri = coverUri,
                        tags = tags,
                        photos = photos
                    )
                    isEditFormVisible = false
                }
            )
        }

        // Formulario de Crear Nuevo Álbum desde el Visor
        AnimatedVisibility(
            visible = isCreateNewAlbumFromViewerVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(150f)
        ) {
            key(isCreateNewAlbumFromViewerVisible) {
                AlbumFormScreen(
                    mode = AlbumFormMode.NEW,
                    initialPhotos = viewerPreselectedPhotos,
                    initialCoverUri = viewerPreselectedPhotos.firstOrNull()?.uri,
                    onCloseClick = { isCreateNewAlbumFromViewerVisible = false },
                    onSaveClick = { title, description, coverUri, photos, tags ->
                        val albumName = title.ifBlank { "Untitled album" }
                        viewModel.onCreateAlbumWithPhotos(
                            name = albumName,
                            description = description,
                            coverUri = coverUri ?: photos.firstOrNull()?.uri,
                            tags = tags,
                            photos = photos
                        )
                        Toast.makeText(context, context.getString(R.string.album_created_success, albumName), Toast.LENGTH_SHORT).show()
                        selectedMediaIds.clear()
                        isCreateNewAlbumFromViewerVisible = false
                    }
                )
            }
        }

        // Panel lateral (Side Sheet) de Opciones del Álbum
        AlbumOptionsSideSheet(
            isVisible = isOptionsVisible,
            onDismiss = { isOptionsVisible = false },
            sortBy = sortBy,
            onSortByChange = { sortBy = it },
            sortingMode = sortingMode,
            onSortingChange = { sortingMode = it },
            layoutMode = layoutMode,
            onLayoutChange = { layoutMode = it },
            showDatesInGrid = showDatesInGrid,
            onShowDatesChange = {
                showDatesInGrid = it
                viewModel.onShowDatesInGridChange(it)
            },
            onExportAlbumClick = {
                isOptionsVisible = false
                if (albumPhotos.isEmpty()) {
                    Toast.makeText(context, context.getString(R.string.export_album_empty), Toast.LENGTH_SHORT).show()
                } else {
                    val cleanName = activeAlbum.name.trim().ifEmpty { "Album" }
                    createZipLauncher.launch("$cleanName.zip")
                }
            },
            onDeleteAlbumClick = {
                isOptionsVisible = false
                isDeleteConfirmDialogVisible = true
            }
        )

        // Diálogo de progreso de exportación a .ZIP
        ExportAlbumProgressDialog(
            exportState = exportState,
            onCancel = { viewModel.cancelAlbumExport() }
        )

        // Diálogo de confirmación para eliminar álbum
        if (isDeleteConfirmDialogVisible) {
            AlertDialog(
                onDismissRequest = { isDeleteConfirmDialogVisible = false },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.album_detail_delete_album),
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
                            isDeleteConfirmDialogVisible = false
                            viewModel.onDeleteAlbum(activeAlbum.id)
                            onDeleteAlbumClick()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButtonWithAnimatedShape(
                        onClick = { isDeleteConfirmDialogVisible = false }
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

