package com.ixeken.nierika.feature.collections

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionBottomBar
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter

/**
 * Pantalla de Detalle para las Categorías del Sistema (Favoritos, Vídeos, Cámara y Capturas).
 * Proporciona una vista en cuadrícula adaptativa con menú de opciones modal lateral (SideSheet),
 * soporte de selección múltiple, visualizador a pantalla completa y consistencia visual con Timeline.
 */
@Composable
fun PredefinedCategoryDetailScreen(
    category: QuickFilter,
    onBackClick: () -> Unit,
    onPickMedia: ((List<MediaItem>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val rawMediaItems by viewModel.getMediaForCategory(category).collectAsState(initial = emptyList())
    val pendingTrashIds by viewModel.pendingTrashIds.collectAsState()
    val mediaItems = remember(rawMediaItems, pendingTrashIds) {
        if (pendingTrashIds.isEmpty()) rawMediaItems else rawMediaItems.filter { it.id !in pendingTrashIds }
    }
    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()

    var isOptionsVisible by remember { mutableStateOf(false) }
    var sortingMode by remember { mutableStateOf(AlbumSorting.TOP_TO_BOTTOM) }
    var layoutMode by remember { mutableStateOf(AlbumLayout.DAY) }
    var showDatesInGrid by remember(userPreferences.showDatesInCollectionsGrid) { mutableStateOf(userPreferences.showDatesInCollectionsGrid) }

    val todayStr = stringResource(R.string.date_today)
    val yesterdayStr = stringResource(R.string.date_yesterday)

    val selectedMediaIds = remember { mutableStateListOf<Long>() }
    val isSelectionModeActive = selectedMediaIds.isNotEmpty()
    var isAddToAlbumSheetVisible by remember { mutableStateOf(false) }
    var isCreateNewAlbumFromViewerVisible by remember { mutableStateOf(false) }
    var viewerPreselectedPhotos by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var selectedMediaIndex by remember { mutableStateOf<Int?>(null) }
    var selectedMediaId by remember { mutableStateOf<Long?>(null) }
    var displayedMediaIndex by remember { mutableStateOf<Int?>(null) }
    var displayedMediaId by remember { mutableStateOf<Long?>(null) }
    if (selectedMediaIndex != null) {
        displayedMediaIndex = selectedMediaIndex
        displayedMediaId = selectedMediaId
    }

    var pendingTrashItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingTrashOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }

    val trashLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (pendingTrashItems.isNotEmpty()) {
                viewModel.moveToTrashBatch(pendingTrashItems)
                pendingTrashItems = emptyList()
            }
            // Se ejecuta la transición visual únicamente cuando el usuario autoriza la acción en el diálogo del sistema
            pendingTrashOnAccepted?.invoke()
            viewModel.refreshSilent()
        } else {
            pendingTrashItems = emptyList()
        }
        pendingTrashOnAccepted = null
    }

    /**
     * Solicita mover elementos a la papelera.
     * Retiene el callback [onAccepted] para ejecutar el avance de imagen únicamente cuando
     * la operación es confirmada por el usuario, evitando saltos de pantalla prematuros si cancela.
     */
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

    val displayedPhotos = remember(mediaItems, sortingMode) {
        if (sortingMode == AlbumSorting.BOTTOM_TO_TOP) {
            mediaItems.sortedBy { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
        } else {
            mediaItems.sortedByDescending { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
        }
    }

    val groupedItems = remember(displayedPhotos, showDatesInGrid, layoutMode, sortingMode, todayStr, yesterdayStr) {
        if (showDatesInGrid) {
            groupMediaItemsByDateForCollections(displayedPhotos, todayStr, yesterdayStr, layoutMode, sortingMode)
        } else {
            emptyList()
        }
    }

    val categoryTitle = when (category) {
        QuickFilter.FAVORITES -> stringResource(R.string.category_favorites)
        QuickFilter.VIDEOS -> stringResource(R.string.category_videos)
        QuickFilter.CAMERA -> stringResource(R.string.category_camera)
        QuickFilter.SCREENSHOTS -> stringResource(R.string.category_screenshots)
        QuickFilter.NOT_IN_ALBUM -> stringResource(R.string.category_not_in_album)
        else -> stringResource(R.string.nav_tab_collections)
    }

    val categoryIcon = when (category) {
        QuickFilter.FAVORITES -> Icons.Filled.Favorite
        QuickFilter.VIDEOS -> Icons.Filled.VideoLibrary
        QuickFilter.CAMERA -> Icons.Filled.PhotoCamera
        QuickFilter.SCREENSHOTS -> Icons.Filled.Smartphone
        QuickFilter.NOT_IN_ALBUM -> Icons.Filled.FolderOff
        else -> Icons.Filled.Favorite
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
                label = "categoryTopBarTransition"
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
                                    text = categoryTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = stringResource(R.string.album_items_count, displayedPhotos.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Botón More para abrir SideSheet de Opciones
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_more_options)
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

            // Grid de Fotos
            if (displayedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.empty_timeline_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 96.dp),
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
                                val item = groupPhotos[photoIndex]
                                val isSelected = selectedMediaIds.contains(item.id)
                                val selectionIndex = if (isSelected) selectedMediaIds.indexOf(item.id) + 1 else null
                                val globalIndex = displayedPhotos.indexOf(item).takeIf { it >= 0 } ?: photoIndex
                                val cellShape = getGridItemShape(
                                    index = photoIndex,
                                    totalCount = groupPhotos.size,
                                    columnsCount = columnsCount,
                                    cornerRadius = 16.dp
                                )

                                NierikaPhotoGridItem(
                                    uri = item.uri,
                                    displayName = item.displayName,
                                    isVideo = item.isVideo,
                                    isGif = item.isGif,
                                    durationMillis = item.durationMillis,
                                    isFavorite = item.isFavorite,
                                    isSelected = isSelected,
                                    selectionIndex = selectionIndex,
                                    isSelectionModeActive = isSelectionModeActive,
                                    cellShape = cellShape,
                                    targetRatio = 1f,
                                    onClick = {
                                        if (isSelectionModeActive) {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                        } else {
                                            selectedMediaIndex = globalIndex
                                            selectedMediaId = item.id
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (!isSelectionModeActive) {
                                            if (!selectedMediaIds.contains(item.id)) selectedMediaIds.add(item.id)
                                        } else {
                                            if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = displayedPhotos,
                            key = { _, item -> item.id },
                            contentType = { _, _ -> "photo_item" }
                        ) { index, item ->
                            val isSelected = selectedMediaIds.contains(item.id)
                            val selectionIndex = if (isSelected) selectedMediaIds.indexOf(item.id) + 1 else null
                            val cellShape = getGridItemShape(
                                index = index,
                                totalCount = displayedPhotos.size,
                                columnsCount = columnsCount,
                                cornerRadius = 16.dp
                            )

                            NierikaPhotoGridItem(
                                uri = item.uri,
                                displayName = item.displayName,
                                isVideo = item.isVideo,
                                isGif = item.isGif,
                                durationMillis = item.durationMillis,
                                isFavorite = item.isFavorite,
                                isSelected = isSelected,
                                selectionIndex = selectionIndex,
                                isSelectionModeActive = isSelectionModeActive,
                                cellShape = cellShape,
                                targetRatio = 1f,
                                onClick = {
                                    if (isSelectionModeActive) {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                    } else {
                                        selectedMediaIndex = index
                                        selectedMediaId = item.id
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isSelectionModeActive) {
                                        if (!selectedMediaIds.contains(item.id)) selectedMediaIds.add(item.id)
                                    } else {
                                        if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }



        // Barra inferior de selección múltiple
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
                .zIndex(90f)
        ) {
            val selectedItemsList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
            val allSelectedAreFavorites = selectedItemsList.isNotEmpty() && selectedItemsList.all { it.isFavorite }

            NierikaSelectionBottomBar(
                onShareClick = {
                    if (selectedItemsList.isNotEmpty()) {
                        val uris = ArrayList<Uri>(selectedItemsList.map { Uri.parse(it.uri) })
                        var hasImage = false
                        var hasVideo = false
                        selectedItemsList.forEach { item ->
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
                    selectedItemsList.forEach { item ->
                        viewModel.onToggleFavorite(item.id, newFavState)
                    }
                    selectedMediaIds.clear()
                },
                isAllFavorites = allSelectedAreFavorites,
                onAddToAlbumClick = {
                    isAddToAlbumSheetVisible = true
                },
                onDeleteClick = {
                    requestTrash(selectedItemsList)
                },
                onSelectClick = if (onPickMedia != null) { { onPickMedia(selectedItemsList) } } else null,
                deleteLabel = stringResource(R.string.selection_action_trash)
            )
        }

        // BottomSheet para Agregar a Álbum
        AddToAlbumBottomSheet(
            isVisible = isAddToAlbumSheetVisible,
            albums = uiState.albums,
            onDismiss = { isAddToAlbumSheetVisible = false },
            onCreateNewAlbumClick = {
                val selectedItemsList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
                viewerPreselectedPhotos = selectedItemsList
                isAddToAlbumSheetVisible = false
                isCreateNewAlbumFromViewerVisible = true
            },
            onAlbumSelected = { targetAlbum ->
                val selectedItemsList = displayedPhotos.filter { selectedMediaIds.contains(it.id) }
                viewModel.onAddMediaBatchToAlbum(targetAlbum, selectedItemsList) { result ->
                    val msg = when {
                        result.addedCount == 0 -> {
                            if (selectedItemsList.size == 1) {
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
                isAddToAlbumSheetVisible = false
                selectedMediaIds.clear()
            }
        )

        // Panel lateral (Side Sheet) de Opciones de la Categoría
        CategoryOptionsSideSheet(
            isVisible = isOptionsVisible,
            onDismiss = { isOptionsVisible = false },
            sortingMode = sortingMode,
            onSortingChange = { sortingMode = it },
            layoutMode = layoutMode,
            onLayoutChange = { layoutMode = it },
            showDatesInGrid = showDatesInGrid,
            onShowDatesChange = {
                showDatesInGrid = it
                viewModel.onShowDatesInGridChange(it)
            }
        )

        // Cerrar visor automáticamente si la lista de fotos de la categoría queda vacía
        LaunchedEffect(displayedPhotos.isEmpty()) {
            if (displayedPhotos.isEmpty() && selectedMediaIndex != null) {
                selectedMediaIndex = null
                selectedMediaId = null
            }
        }

        // Visor de Medios a pantalla completa
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
    }
}

/**
 * Panel lateral modal (Side Sheet) de opciones para Categorías Predeterminadas.
 * Comparte el diseño exacto del SideSheet del Timeline (Ordenación, Modo de Cuadrícula, Mostrar Fechas)
 * omitiendo selectores de filtros redundantes y botones de borrado de álbum.
 */
@Composable
private fun CategoryOptionsSideSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    sortingMode: AlbumSorting,
    onSortingChange: (AlbumSorting) -> Unit,
    layoutMode: AlbumLayout,
    onLayoutChange: (AlbumLayout) -> Unit,
    showDatesInGrid: Boolean,
    onShowDatesChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible, onBack = onDismiss)

    val motionScheme = MaterialTheme.nierikaMotionScheme

    // Scrim independiente: solo fade
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

    // Panel lateral: solo slide horizontal estándar (igual que Timeline y AlbumDetail)
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
                        fontSize = 20.sp,
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
                        fontSize = 16.sp,
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
                        fontSize = 16.sp,
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        SwitchWithThumbIcon(
                            checked = showDatesInGrid,
                            onCheckedChange = onShowDatesChange
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}
