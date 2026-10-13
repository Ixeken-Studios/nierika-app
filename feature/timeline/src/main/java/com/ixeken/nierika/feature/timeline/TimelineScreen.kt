package com.ixeken.nierika.feature.timeline

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import com.ixeken.nierika.feature.settings.SettingsScreen
import com.ixeken.nierika.feature.collections.CollectionsScreen
import com.ixeken.nierika.feature.collections.AddToAlbumBottomSheet
import com.ixeken.nierika.feature.collections.AlbumFormScreen
import com.ixeken.nierika.feature.collections.AlbumFormMode
import com.ixeken.nierika.core.designsystem.component.NierikaWalkthroughSheet
import com.ixeken.nierika.feature.viewer.MediaDetailScreen
import com.ixeken.nierika.feature.viewer.components.PhotoDeleteBottomSheet
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.FilledTonalButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.paging.LoadState
import com.ixeken.nierika.core.designsystem.component.NierikaFastScroller
import com.ixeken.nierika.core.designsystem.component.NierikaLoadingIndicator
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaSelectionTopBar
import com.ixeken.nierika.feature.timeline.components.TimelineTopBarSection
import com.ixeken.nierika.feature.timeline.components.TimelineTrashBottomBar
import com.ixeken.nierika.core.designsystem.component.GifBadge
import com.ixeken.nierika.core.designsystem.component.ShortNavigationBarWithHorizontalItems
import com.ixeken.nierika.core.designsystem.component.VideoDurationBadge
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.util.shimmerEffect
import com.ixeken.nierika.core.designsystem.R
import androidx.compose.ui.platform.LocalConfiguration
import com.ixeken.nierika.core.designsystem.component.NavItemData
import com.ixeken.nierika.core.designsystem.component.NierikaFloatingNavBar
import com.ixeken.nierika.core.designsystem.component.NierikaVerticalFloatingNavBar
import com.ixeken.nierika.core.designsystem.component.NierikaTopAppBar
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.LayoutMode
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.feature.timeline.components.TimelineComfyJustifiedGrid
import com.ixeken.nierika.feature.timeline.components.TimelineEmptyState
import com.ixeken.nierika.feature.timeline.components.partitionIntoJustifiedRows

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class GalleryTab {
    LIBRARY,
    COLLECTIONS
}

/**
 * Agrupa los elementos multimedia de la captura actual por fecha formateada utilizando etiquetas localizadas.
 */
private fun groupMediaItemsByDate(
    items: List<MediaItem>,
    todayStr: String,
    yesterdayStr: String,
    layoutMode: LayoutMode,
    sortOrder: SortOrder
): List<Pair<String, List<MediaItem>>> {
    val groups = LinkedHashMap<String, MutableList<MediaItem>>()
    val now = Calendar.getInstance()

    val isTrash = items.isNotEmpty() && items.all { it.isTrash }
    val sortedItems = if (isTrash) {
        if (sortOrder == SortOrder.ASCENDING) {
            items.sortedBy { it.effectiveTrashedAtMillis }
        } else {
            items.sortedByDescending { it.effectiveTrashedAtMillis }
        }
    } else {
        if (sortOrder == SortOrder.ASCENDING) {
            items.sortedBy { it.dateTakenMillis }
        } else {
            items.sortedByDescending { it.dateTakenMillis }
        }
    }

    sortedItems.forEach { item ->
        val timestamp = if (isTrash) item.effectiveTrashedAtMillis else item.dateTakenMillis
        val photoCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val dateLabel = if (layoutMode == LayoutMode.MONTH) {
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(photoCal.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            when {
                isSameDay(now, photoCal) -> todayStr
                isYesterday(now, photoCal) -> yesterdayStr
                isSameWeek(now, photoCal) -> SimpleDateFormat("EEEE", Locale.getDefault()).format(photoCal.time)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                else -> SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(photoCal.time)
            }
        }
        groups.getOrPut(dateLabel) { mutableListOf() }.add(item)
    }

    return groups.toList()
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val clone1 = cal1.clone() as Calendar
    clone1.add(Calendar.DAY_OF_YEAR, -1)
    return isSameDay(clone1, cal2)
}

private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

/**
 * Pantalla principal de la aplicación con la navegación flotante M3 Expressive entre Library (Fotos) y Collections (Colecciones).
 * Permite visualizar cualquier elemento multimedia en la vista inmersiva MediaDetailScreen al pulsar sobre él.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    pagingItems: LazyPagingItems<MediaItem>,
    uiState: TimelineUiState,
    timelineViewModel: TimelineViewModel = hiltViewModel(),
    onMediaClick: (MediaItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenMenu: () -> Unit = {},
    onNavigateTab: (GalleryTab) -> Unit = {},
    onSortOrderChange: (SortOrder) -> Unit = {},
    onFilterChange: (QuickFilter) -> Unit = {},
    onLayoutModeChange: (LayoutMode) -> Unit = {},
    onShowDatesInGridChange: (Boolean) -> Unit = {},
    onToggleSideSheet: (Boolean) -> Unit = {},
    onRequestPermission: () -> Unit = {},
    isPickerMode: Boolean = false,
    allowMultiplePick: Boolean = false,
    onPickMedia: ((List<MediaItem>) -> Unit)? = null,
    initialViewUri: Uri? = null,
    onDismissExternalView: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(GalleryTab.LIBRARY) }
    var isViewerVisible by rememberSaveable { mutableStateOf(false) }
    var activeViewerMediaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var activeViewerMediaIndex by rememberSaveable { mutableStateOf(0) }
    var isSettingsVisible by remember { mutableStateOf(false) }
    var isSearchVisible by rememberSaveable { mutableStateOf(false) }
    var isVaultVisible by rememberSaveable { mutableStateOf(false) }
    var externalMediaItem by remember(initialViewUri) { mutableStateOf<MediaItem?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val duplicateSuccessMsg = stringResource(R.string.toast_duplicate_success)
    val editSuccessMsg = stringResource(R.string.toast_edit_success)

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }
    val userPreferences by timelineViewModel.userPreferences.collectAsState()
    val mediaCounts by timelineViewModel.mediaCounts.collectAsState()
    val pendingTrashIds by timelineViewModel.pendingTrashIds.collectAsState()
    var isMultiSelectDeleteConfirmVisible by remember { mutableStateOf(false) }
    val favoriteOverrides = remember { mutableStateMapOf<Long, Boolean>() }
    var viewerItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    LaunchedEffect(pagingItems.loadState.refresh) {
        if (pagingItems.loadState.refresh is androidx.paging.LoadState.NotLoading) {
            timelineViewModel.clearPendingTrashIds()
        }
    }

    val context = LocalContext.current
    var pendingDeleteItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingDeleteOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }
    var isCollectionsDetailActive by remember { mutableStateOf(false) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (pendingDeleteItems.isNotEmpty()) {
                timelineViewModel.deletePermanentlyFromDb(pendingDeleteItems.map { it.id })
                pendingDeleteItems = emptyList()
            }
            pendingDeleteOnAccepted?.invoke()
            timelineViewModel.refreshSilent()
        } else {
            pendingDeleteItems = emptyList()
        }
        pendingDeleteOnAccepted = null
    }

    fun requestPermanentDelete(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingDeleteItems = items
        pendingDeleteOnAccepted = onAccepted
        val intentSender = timelineViewModel.createDeleteIntentSender(context, items)
        if (intentSender != null) {
            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            timelineViewModel.deletePermanentlyFromDb(items.map { it.id })
            pendingDeleteItems = emptyList()
            onAccepted?.invoke()
            pendingDeleteOnAccepted = null
        }
    }

    var pendingTrashItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingTrashOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (pendingTrashItems.isNotEmpty()) {
                timelineViewModel.moveToTrashBatch(pendingTrashItems)
                pendingTrashItems = emptyList()
            }
            pendingTrashOnAccepted?.invoke()
            timelineViewModel.refreshSilent()
        } else {
            pendingTrashItems = emptyList()
        }
        pendingTrashOnAccepted = null
    }

    fun requestTrash(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingTrashItems = items
        pendingTrashOnAccepted = onAccepted
        val intentSender = timelineViewModel.createTrashIntentSender(context, items, isTrash = true)
        if (intentSender != null) {
            trashLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            timelineViewModel.moveToTrashBatch(items)
            pendingTrashItems = emptyList()
            onAccepted?.invoke()
            pendingTrashOnAccepted = null
        }
    }

    var pendingRestoreItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingRestoreOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (pendingRestoreItems.isNotEmpty()) {
                timelineViewModel.restoreBatch(pendingRestoreItems)
                pendingRestoreItems = emptyList()
            }
            pendingRestoreOnAccepted?.invoke()
            timelineViewModel.refreshSilent()
        } else {
            pendingRestoreItems = emptyList()
        }
        pendingRestoreOnAccepted = null
    }

    fun requestRestore(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingRestoreItems = items
        pendingRestoreOnAccepted = onAccepted
        val intentSender = timelineViewModel.createTrashIntentSender(context, items, isTrash = false)
        if (intentSender != null) {
            restoreLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            timelineViewModel.restoreBatch(items)
            pendingRestoreItems = emptyList()
            onAccepted?.invoke()
            pendingRestoreOnAccepted = null
        }
    }

    val selectedMediaIds = remember { mutableStateListOf<Long>() }
    val isSelectionModeActive = selectedMediaIds.isNotEmpty()

    val albums by timelineViewModel.albums.collectAsState()
    var isAddToAlbumSheetVisible by remember { mutableStateOf(false) }
    var isCreateAlbumFormVisible by remember { mutableStateOf(false) }

    BackHandler(enabled = externalMediaItem != null, onBack = {
        externalMediaItem = null
        onDismissExternalView?.invoke()
    })
    BackHandler(enabled = isViewerVisible, onBack = {
        isViewerVisible = false
        if (initialViewUri != null) {
            onDismissExternalView?.invoke()
        }
    })
    BackHandler(enabled = isSearchVisible, onBack = { isSearchVisible = false })
    BackHandler(enabled = isSelectionModeActive, onBack = { selectedMediaIds.clear() })

    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            val startTime = System.currentTimeMillis()
            while (pagingItems.loadState.refresh is LoadState.Loading) {
                delay(50)
            }
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 600) {
                delay(600 - elapsedTime)
            }
            isRefreshing = false
        }
    }

    val motionScheme = MaterialTheme.nierikaMotionScheme
    val gridState = rememberLazyGridState()
    val comfyListState = rememberLazyListState()
    var zoomScale by remember { mutableFloatStateOf(1f) }

    val animatedGridZoomScale by animateFloatAsState(
        targetValue = zoomScale,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "gridPinchZoom"
    )

    LaunchedEffect(zoomScale) {
        if (zoomScale != 1f) {
            delay(250)
            zoomScale = 1f
        }
    }

    val isComfortableMode = uiState.selectedLayoutMode == LayoutMode.COMFORTABLE
    val isScrollInProgress = if (isComfortableMode) comfyListState.isScrollInProgress else gridState.isScrollInProgress

    var isScrollActive by remember { mutableStateOf(false) }

    LaunchedEffect(isScrollInProgress) {
        if (isScrollInProgress) {
            isScrollActive = true
        } else {
            delay(1800)
            isScrollActive = false
        }
    }

    LaunchedEffect(uiState.selectedSortOrder) {
        if (pagingItems.itemCount > 0) {
            if (uiState.selectedSortOrder == SortOrder.ASCENDING) {
                val lastIndex = (pagingItems.itemCount - 1).coerceAtLeast(0)
                if (isComfortableMode) {
                    comfyListState.scrollToItem(lastIndex)
                } else {
                    gridState.scrollToItem(lastIndex)
                }
            } else {
                if (isComfortableMode) {
                    comfyListState.scrollToItem(0)
                } else {
                    gridState.scrollToItem(0)
                }
            }
        }
    }

    val isScrolled by remember {
        derivedStateOf {
            if (isComfortableMode) {
                comfyListState.firstVisibleItemIndex > 0 || comfyListState.firstVisibleItemScrollOffset > 40
            } else {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 40
            }
        }
    }

    val isFastScrollerVisible by remember {
        derivedStateOf {
            val minRowThreshold = if (uiState.selectedLayoutMode == LayoutMode.MONTH) 12 else 8
            val hasScrolled = if (isComfortableMode) {
                comfyListState.firstVisibleItemIndex >= 3
            } else {
                gridState.firstVisibleItemIndex >= minRowThreshold
            }
            hasScrolled && isScrollActive
        }
    }

    val libraryTitle = stringResource(R.string.nav_tab_library)
    val collectionsTitle = stringResource(R.string.nav_tab_collections)
    val todayStr = stringResource(R.string.date_today)
    val yesterdayStr = stringResource(R.string.date_yesterday)

    val navItems = remember(libraryTitle, collectionsTitle) {
        listOf(
            NavItemData(
                destination = GalleryTab.LIBRARY,
                label = libraryTitle,
                icon = Icons.Filled.PhotoLibrary
            ),
            NavItemData(
                destination = GalleryTab.COLLECTIONS,
                label = collectionsTitle,
                icon = Icons.Filled.CollectionsBookmark
            )
        )
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val columnsCount = when (uiState.selectedLayoutMode) {
        LayoutMode.DAY -> if (isWideScreen) 5 else 3
        LayoutMode.MONTH -> if (isWideScreen) 7 else 4
        LayoutMode.COMFORTABLE -> if (isWideScreen) 3 else 2
    }

    val snapshotItems = remember(pagingItems.itemSnapshotList, pendingTrashIds, favoriteOverrides.toMap()) {
        val raw = pagingItems.itemSnapshotList.items.filterNotNull()
        val filtered = if (pendingTrashIds.isEmpty()) {
            raw
        } else {
            raw.filter { it.id !in pendingTrashIds }
        }
        if (favoriteOverrides.isEmpty()) {
            filtered
        } else {
            filtered.map { item ->
                val override = favoriteOverrides[item.id]
                if (override != null && override != item.isFavorite) item.copy(isFavorite = override) else item
            }
        }
    }

    LaunchedEffect(isViewerVisible) {
        if (isViewerVisible) {
            viewerItems = snapshotItems
        } else {
            viewerItems = emptyList()
        }
    }

    LaunchedEffect(snapshotItems) {
        if (isViewerVisible && snapshotItems.size > viewerItems.size) {
            viewerItems = snapshotItems
        }
    }

    LaunchedEffect(initialViewUri, snapshotItems) {
        if (initialViewUri != null) {
            val uriStr = initialViewUri.toString()
            val found = snapshotItems.find { it.uri == uriStr }
            if (found != null) {
                activeViewerMediaId = found.id
                activeViewerMediaIndex = snapshotItems.indexOf(found)
                isViewerVisible = true
            } else if (externalMediaItem == null) {
                val mime = context.contentResolver.getType(initialViewUri)
                    ?: if (uriStr.contains("video", ignoreCase = true)) "video/mp4" else "image/jpeg"
                externalMediaItem = MediaItem(
                    id = -1L,
                    uri = uriStr,
                    displayName = initialViewUri.lastPathSegment ?: "Media",
                    mimeType = mime,
                    dateTakenMillis = System.currentTimeMillis(),
                    dateModifiedMillis = System.currentTimeMillis(),
                    sizeBytes = 0L,
                    width = 0,
                    height = 0
                )
            }
        }
    }

    val visibleDateText by remember {
        derivedStateOf {
            if (pagingItems.itemCount > 0) {
                val item: MediaItem? = if (isComfortableMode) {
                    val rows = partitionIntoJustifiedRows(snapshotItems, 1000f, 10f)
                    val rowIdx = comfyListState.firstVisibleItemIndex.coerceIn(0, rows.lastIndex.coerceAtLeast(0))
                    rows.getOrNull(rowIdx)?.firstOrNull() ?: snapshotItems.firstOrNull()
                } else {
                    val photoIdx = gridState.firstVisibleItemIndex.coerceIn(0, pagingItems.itemCount - 1)
                    pagingItems[photoIdx]
                }
                if (item != null) {
                    val pattern = if (uiState.selectedLayoutMode == LayoutMode.MONTH) "MMMM yyyy" else "EEE, MMM d, yyyy"
                    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
                    formatter.format(Date(item.dateTakenMillis))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                } else {
                    ""
                }
            } else {
                ""
            }
        }
    }

    val groupedItems = remember(snapshotItems, uiState.showDatesInGrid, uiState.selectedLayoutMode, uiState.selectedSortOrder, todayStr, yesterdayStr) {
        if (uiState.showDatesInGrid) {
            groupMediaItemsByDate(snapshotItems, todayStr, yesterdayStr, uiState.selectedLayoutMode, uiState.selectedSortOrder)
        } else {
            emptyList()
        }
    }

    val itemCountSubtitle = stringResource(R.string.album_items_count, mediaCounts.totalCount)

    // Colores leídos en scope composable para forzar recomposición del TopAppBar dentro del LazyGrid
    val appBarBgColor = MaterialTheme.colorScheme.background
    val appBarContentColor = MaterialTheme.colorScheme.onBackground
    val appBarIconAccent = MaterialTheme.colorScheme.primary

    val isNavBarVisible = !isSelectionModeActive &&
            !isViewerVisible &&
            !isSettingsVisible &&
            !isSearchVisible &&
            !isVaultVisible &&
            !isCollectionsDetailActive &&
            uiState.selectedFilter != QuickFilter.TRASH

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Cápsula flotante vertical para pantallas grandes (>= 600dp, Fold / Tablet) inspirada en iPhone Duo
                AnimatedVisibility(
                    visible = isNavBarVisible && isWideScreen,
                    enter = slideInVertically(
                        initialOffsetY = { it * 3 },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it * 3 },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 24.dp)
                        .zIndex(if (isCollectionsDetailActive) 0f else 100f)
                ) {
                    NierikaVerticalFloatingNavBar(
                        items = navItems,
                        selectedDestination = selectedTab,
                        onItemSelect = { destination ->
                            selectedTab = destination
                        },
                        onSearchClick = {
                            isSearchVisible = true
                        }
                    )
                }

            if (!uiState.permissionGranted) {
                PermissionRationaleScreen(
                    onRequestPermission = onRequestPermission,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState == GalleryTab.COLLECTIONS) {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioLowBouncy
                                ),
                                initialOffsetX = { it }
                            ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    ),
                                    targetOffsetX = { -it }
                                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            )
                        } else {
                            (slideInHorizontally(
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    dampingRatio = Spring.DampingRatioLowBouncy
                                ),
                                initialOffsetX = { -it }
                            ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))).togetherWith(
                                slideOutHorizontally(
                                    animationSpec = spring(
                                        stiffness = Spring.StiffnessMediumLow,
                                        dampingRatio = Spring.DampingRatioLowBouncy
                                    ),
                                    targetOffsetX = { it }
                                ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            )
                        }
                    },
                    label = "tabAnimatedContent",
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(if (isCollectionsDetailActive) 50f else 0f)
                ) { currentTab ->
                    if (currentTab == GalleryTab.LIBRARY) {
                        val pullRefreshState = rememberPullToRefreshState()
                        val animatedStretchY by animateFloatAsState(
                            targetValue = if (isRefreshing) 84f else (pullRefreshState.distanceFraction * 130f).coerceAtMost(160f),
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            label = "pullToRefreshElasticStretch"
                        )

                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = {
                                isRefreshing = true
                                pagingItems.refresh()
                            },
                            state = pullRefreshState,
                            indicator = {
                                val showIndicator = isRefreshing || pullRefreshState.distanceFraction > 0.05f
                                val indicatorOffsetY = (animatedStretchY * 0.55f) + 8f

                                AnimatedVisibility(
                                    visible = showIndicator,
                                    enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
                                    exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .graphicsLayer {
                                            translationY = indicatorOffsetY
                                        }
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        tonalElevation = 6.dp,
                                        shadowElevation = 4.dp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val dynamicSize = (34 + (pullRefreshState.distanceFraction * 12).coerceAtMost(14f)).dp
                                            NierikaLoadingIndicator(
                                                size = dynamicSize,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        translationY = animatedStretchY
                                        scaleX = animatedGridZoomScale
                                        scaleY = animatedGridZoomScale
                                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                                    }
                                    .pointerInput(uiState.selectedLayoutMode) {
                                        detectTransformGestures { _, _, zoom, _ ->
                                            zoomScale = (zoomScale * zoom).coerceIn(0.65f, 1.65f)
                                            when {
                                                zoomScale > 1.25f -> {
                                                    zoomScale = 1f
                                                    val nextMode = when (uiState.selectedLayoutMode) {
                                                        LayoutMode.MONTH -> LayoutMode.DAY
                                                        LayoutMode.DAY -> LayoutMode.COMFORTABLE
                                                        LayoutMode.COMFORTABLE -> LayoutMode.COMFORTABLE
                                                    }
                                                    if (nextMode != uiState.selectedLayoutMode) {
                                                        onLayoutModeChange(nextMode)
                                                    }
                                                }
                                                zoomScale < 0.75f -> {
                                                    zoomScale = 1f
                                                    val nextMode = when (uiState.selectedLayoutMode) {
                                                        LayoutMode.COMFORTABLE -> LayoutMode.DAY
                                                        LayoutMode.DAY -> LayoutMode.MONTH
                                                        LayoutMode.MONTH -> LayoutMode.MONTH
                                                    }
                                                    if (nextMode != uiState.selectedLayoutMode) {
                                                        onLayoutModeChange(nextMode)
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                if (isComfortableMode) {
                                    TimelineComfyJustifiedGrid(
                                        showDatesInGrid = uiState.showDatesInGrid,
                                        groupedItems = groupedItems,
                                        selectedMediaIds = selectedMediaIds,
                                        isSelectionModeActive = isSelectionModeActive,
                                        snapshotItems = snapshotItems,
                                        comfyListState = comfyListState,
                                        onSelectIndex = { index ->
                                            activeViewerMediaIndex = index
                                            activeViewerMediaId = snapshotItems.getOrNull(index)?.id
                                            isViewerVisible = true
                                        },
                                        isPickerMode = isPickerMode,
                                        allowMultiplePick = allowMultiplePick
                                    )
                                } else {
                                    LazyVerticalGrid(
                                        state = gridState,
                                        columns = GridCells.Fixed(columnsCount),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(
                                            start = 4.dp,
                                            end = 4.dp,
                                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 88.dp,
                                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isWideScreen) 88.dp else 96.dp
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (uiState.showDatesInGrid) {
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
                                                    key = { photoIndex -> groupPhotos[photoIndex]?.id ?: photoIndex },
                                                    contentType = { "photo_item" },
                                                    span = { photoIndex ->
                                                        GridItemSpan(1)
                                                    }
                                                ) { photoIndex ->
                                                    val item = groupPhotos[photoIndex]
                                                    val cellShape = getGridItemShape(
                                                        index = photoIndex,
                                                        totalCount = groupPhotos.size,
                                                        columnsCount = columnsCount,
                                                        cornerRadius = 16.dp
                                                    )

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(1.0f)
                                                    ) {
                                                        val isSelected = item.id in selectedMediaIds
                                                        val selectionIndex = if (isSelected) selectedMediaIds.indexOf(item.id) + 1 else null
                                                        val globalIndex = snapshotItems.indexOf(item).takeIf { it >= 0 } ?: photoIndex
                                                        TimelinePhotoGridItem(
                                                            item = item,
                                                            isSelected = isSelected,
                                                            selectionIndex = selectionIndex,
                                                            isSelectionModeActive = isSelectionModeActive,
                                                            cellShape = cellShape,
                                                            targetRatio = 1.0f,
                                                            onClick = {
                                                                if (isSelectionModeActive || (isPickerMode && allowMultiplePick)) {
                                                                    if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                                                } else {
                                                                    activeViewerMediaIndex = globalIndex
                                                                    activeViewerMediaId = item.id
                                                                    isViewerVisible = true
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            items(
                                                count = pagingItems.itemCount,
                                                key = { photoIndex -> pagingItems.peek(photoIndex)?.id ?: photoIndex },
                                                contentType = { "photo_item" },
                                                span = { photoIndex ->
                                                    GridItemSpan(1)
                                                }
                                            ) { photoIndex ->
                                                val item = pagingItems[photoIndex]
                                                val cellShape = getGridItemShape(
                                                    index = photoIndex,
                                                    totalCount = pagingItems.itemCount,
                                                    columnsCount = columnsCount,
                                                    cornerRadius = 16.dp
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(1.0f)
                                                ) {
                                                    if (item != null) {
                                                        val isSelected = item.id in selectedMediaIds
                                                        val selectionIndex = if (isSelected) selectedMediaIds.indexOf(item.id) + 1 else null
                                                        TimelinePhotoGridItem(
                                                            item = item,
                                                            isSelected = isSelected,
                                                            selectionIndex = selectionIndex,
                                                            isSelectionModeActive = isSelectionModeActive,
                                                            cellShape = cellShape,
                                                            targetRatio = 1.0f,
                                                            onClick = {
                                                                if (isSelectionModeActive || (isPickerMode && allowMultiplePick)) {
                                                                    if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                                                } else {
                                                                    activeViewerMediaIndex = photoIndex
                                                                    activeViewerMediaId = item.id
                                                                    isViewerVisible = true
                                                                }
                                                            },
                                                            onLongClick = {
                                                                if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (pagingItems.loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0) {
                                    TimelineEmptyState(
                                        quickFilter = uiState.selectedFilter,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                val isShowingDateSubtitle = !uiState.showDatesInGrid && isScrolled && visibleDateText.isNotBlank()
                                val currentSubtitle = if (isShowingDateSubtitle) visibleDateText else itemCountSubtitle

                                val splitCountContent: (@Composable () -> Unit)? = if (userPreferences.splitHeaderMediaCount && !isShowingDateSubtitle) {
                                    {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Photo,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "${mediaCounts.photosCount}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "•",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 2.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.Videocam,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "${mediaCounts.videosCount}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                } else {
                                    null
                                }

                                TimelineTopBarSection(
                                    isSelectionModeActive = isSelectionModeActive,
                                    libraryTitle = libraryTitle,
                                    currentSubtitle = currentSubtitle,
                                    splitCountContent = splitCountContent,
                                    appBarContentColor = appBarContentColor,
                                    showAppLogo = userPreferences.showAppLogoInTopBar,
                                    appLogoRes = if (userPreferences.appLauncherAlias.iconStyle == com.ixeken.nierika.core.domain.model.AppIconStyle.PHOTO_RAINBOW) {
                                        R.drawable.ic_launcher_photo_rainbow
                                    } else {
                                        R.drawable.nierika_logo
                                    },
                                    onToggleSideSheet = onToggleSideSheet,
                                    onOpenSettings = {
                                        isSettingsVisible = true
                                        onOpenSettings()
                                    },
                                    selectedCount = selectedMediaIds.size,
                                    isAllSelected = selectedMediaIds.size == snapshotItems.size && snapshotItems.isNotEmpty(),
                                    onClearSelection = { selectedMediaIds.clear() },
                                    onToggleSelectAll = {
                                        if (selectedMediaIds.size == snapshotItems.size) {
                                            selectedMediaIds.clear()
                                        } else {
                                            selectedMediaIds.clear()
                                            selectedMediaIds.addAll(snapshotItems.map { it.id })
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )

                                val fastScrollerTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 96.dp
                                val fastScrollerBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isWideScreen) 88.dp else 96.dp

                                if (isComfortableMode) {
                                    NierikaFastScroller(
                                        listState = comfyListState,
                                        totalItemCount = pagingItems.itemCount,
                                        currentDateLabel = visibleDateText,
                                        isVisible = isFastScrollerVisible && !isSelectionModeActive,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(top = fastScrollerTopPadding, bottom = fastScrollerBottomPadding)
                                    )
                                } else {
                                    NierikaFastScroller(
                                        gridState = gridState,
                                        totalItemCount = pagingItems.itemCount,
                                        currentDateLabel = visibleDateText,
                                        isVisible = isFastScrollerVisible && !isSelectionModeActive,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(top = fastScrollerTopPadding, bottom = fastScrollerBottomPadding)
                                    )
                                }
                            }
                        }
                    } else {
                        CollectionsScreen(
                            onOpenSettings = { isSettingsVisible = true },
                            onCategoryClick = { category ->
                                when (category) {
                                    "FAVORITES" -> onFilterChange(QuickFilter.FAVORITES)
                                    "VIDEOS" -> onFilterChange(QuickFilter.VIDEOS)
                                    else -> onFilterChange(QuickFilter.ALL)
                                }
                                onNavigateTab(GalleryTab.LIBRARY)
                            },
                            onDetailVisibilityChange = { active ->
                                if (isCollectionsDetailActive != active) {
                                    isCollectionsDetailActive = active
                                }
                            },
                            onPickMedia = onPickMedia,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            AnimatedVisibility(
                visible = isSelectionModeActive,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val isTrashFilter = uiState.selectedFilter == QuickFilter.TRASH
                val itemsToFav = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                val allSelectedAreFavorites = itemsToFav.isNotEmpty() && itemsToFav.all { it.isFavorite }

                com.ixeken.nierika.core.designsystem.component.NierikaSelectionBottomBar(
                    onShareClick = {
                        val itemsToShare = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                        if (itemsToShare.isNotEmpty()) {
                            val uris = ArrayList<Uri>(itemsToShare.map { Uri.parse(it.uri) })
                            var hasImage = false
                            var hasVideo = false
                            itemsToShare.forEach { item ->
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
                    onFavoriteClick = if (!isTrashFilter) {
                        {
                            val newFavState = !allSelectedAreFavorites
                            itemsToFav.forEach { favoriteOverrides[it.id] = newFavState }
                            timelineViewModel.toggleFavoriteBatch(itemsToFav, newFavState)
                            selectedMediaIds.clear()
                        }
                    } else null,
                    isAllFavorites = allSelectedAreFavorites,
                    onAddToAlbumClick = if (!isTrashFilter) {
                        { isAddToAlbumSheetVisible = true }
                    } else null,
                    onSelectClick = if (onPickMedia != null) {
                        {
                            val itemsToPick = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                            if (itemsToPick.isNotEmpty()) {
                                onPickMedia(itemsToPick)
                                selectedMediaIds.clear()
                            }
                        }
                    } else null,
                    onRestoreClick = if (isTrashFilter) {
                        {
                            val itemsToRestore = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                            if (itemsToRestore.isNotEmpty()) {
                                timelineViewModel.restoreBatch(itemsToRestore)
                                selectedMediaIds.clear()
                            }
                        }
                    } else null,
                    onDeleteClick = {
                        val itemsToMove = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                        if (itemsToMove.isNotEmpty()) {
                            if (isTrashFilter) {
                                requestPermanentDelete(itemsToMove)
                                selectedMediaIds.clear()
                            } else {
                                if (userPreferences.isDeleteConfirmationEnabled) {
                                    isMultiSelectDeleteConfirmVisible = true
                                } else {
                                    requestTrash(itemsToMove)
                                    selectedMediaIds.clear()
                                }
                            }
                        }
                    },
                    deleteLabel = stringResource(if (isTrashFilter) R.string.selection_action_delete else R.string.selection_action_trash),
                    deleteIcon = Icons.Filled.Delete
                )
            }
        }

            TimelineTrashBottomBar(
                isVisible = uiState.selectedFilter == QuickFilter.TRASH && !isSelectionModeActive && snapshotItems.isNotEmpty(),
                onRestoreAll = {
                    val allTrashItems = snapshotItems.toList()
                    if (allTrashItems.isNotEmpty()) {
                        timelineViewModel.restoreBatch(allTrashItems)
                    }
                },
                onEmptyTrash = {
                    val allTrashItems = snapshotItems.toList()
                    if (allTrashItems.isNotEmpty()) {
                        requestPermanentDelete(allTrashItems)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            AnimatedVisibility(
                visible = isNavBarVisible && !isWideScreen,
                enter = slideInVertically(
                    initialOffsetY = { it * 3 },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it * 3 },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(if (isCollectionsDetailActive) 0f else 10f)
                    .then(
                        if (uiState.floatingTabBarEnabled) {
                            Modifier
                                .navigationBarsPadding()
                                .padding(bottom = 16.dp)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
            ) {
                if (uiState.floatingTabBarEnabled) {
                    NierikaFloatingNavBar(
                        items = navItems,
                        selectedDestination = selectedTab,
                        onItemSelect = { destination ->
                            selectedTab = destination
                        },
                        onSearchClick = {
                            isSearchVisible = true
                        }
                    )
                } else {
                    ShortNavigationBarWithHorizontalItems(
                        items = navItems,
                        selectedDestination = selectedTab,
                        onItemSelect = { destination ->
                            selectedTab = destination
                        },
                        onSearchClick = {
                            isSearchVisible = true
                        },
                        showSearchButton = true
                    )
                }
            }

            LibraryOptionsSideSheet(
                isVisible = uiState.isSideSheetVisible,
                onDismiss = { onToggleSideSheet(false) },
                selectedLayoutMode = uiState.selectedLayoutMode,
                onLayoutModeChange = onLayoutModeChange,
                showDatesInGrid = uiState.showDatesInGrid,
                onShowDatesInGridChange = onShowDatesInGridChange,
                selectedSortOrder = uiState.selectedSortOrder,
                onSortOrderChange = onSortOrderChange,
                selectedFilter = uiState.selectedFilter,
                onFilterChange = onFilterChange,
                onOpenSettings = { isSettingsVisible = true }
            )

            val selectedItemsList = snapshotItems.filter { selectedMediaIds.contains(it.id) }
            PhotoDeleteBottomSheet(
                isVisible = isMultiSelectDeleteConfirmVisible,
                items = selectedItemsList,
                onDismiss = { isMultiSelectDeleteConfirmVisible = false },
                isPermanentDelete = uiState.selectedFilter == QuickFilter.TRASH,
                onConfirmDelete = {
                    val itemsToMove = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                    if (uiState.selectedFilter == QuickFilter.TRASH) {
                        requestPermanentDelete(itemsToMove)
                    } else {
                        requestTrash(itemsToMove)
                    }
                    selectedMediaIds.clear()
                }
            )

            AnimatedVisibility(
                visible = isSettingsVisible,
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
                SettingsScreen(
                    onBackClick = { isSettingsVisible = false },
                    onRequestPermissions = onRequestPermission
                )
            }

            AnimatedVisibility(
                visible = isViewerVisible && (viewerItems.isNotEmpty() || snapshotItems.isNotEmpty()),
                enter = scaleIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    ),
                    initialScale = 0.85f
                ) + fadeIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ),
                exit = scaleOut(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    ),
                    targetScale = 0.85f
                ) + fadeOut(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ),
                label = "detailAnimatedScope",
                modifier = Modifier.fillMaxSize().zIndex(150f)
            ) {
                val currentViewerItems = if (viewerItems.isNotEmpty()) viewerItems else snapshotItems
                val validIndex = remember(currentViewerItems, activeViewerMediaId, activeViewerMediaIndex) {
                    if (activeViewerMediaId != null) {
                        val found = currentViewerItems.indexOfFirst { it.id == activeViewerMediaId }
                        if (found != -1) found else activeViewerMediaIndex
                    } else {
                        activeViewerMediaIndex
                    }.coerceIn(0, currentViewerItems.lastIndex.coerceAtLeast(0))
                }
                if (currentViewerItems.isNotEmpty()) {
                    MediaDetailScreen(
                        items = currentViewerItems,
                        initialIndex = validIndex,
                        onBackClick = {
                            isViewerVisible = false
                            viewerItems = emptyList()
                            if (uiState.selectedFilter == QuickFilter.FAVORITES) {
                                timelineViewModel.refreshSilent()
                            }
                            if (initialViewUri != null) {
                                onDismissExternalView?.invoke()
                            }
                        },
                        onDeleteConfirm = { item, onAccepted ->
                            requestTrash(listOf(item), onAccepted)
                        },
                        onDeletePermanentlyConfirm = { item, onAccepted ->
                            requestPermanentDelete(listOf(item), onAccepted)
                        },
                        onRestoreConfirm = { item, onAccepted ->
                            requestRestore(listOf(item), onAccepted)
                        },
                        onToggleFavorite = { item ->
                            favoriteOverrides[item.id] = item.isFavorite
                            timelineViewModel.toggleFavorite(item.id, item.isFavorite)
                        },
                        onDuplicateClick = { item, onDuplicated ->
                            timelineViewModel.duplicateMedia(item) { newUri ->
                                if (newUri != null) {
                                    toastMessage = if (item.isVideo) {
                                        context.getString(R.string.toast_duplicate_video_success)
                                    } else {
                                        context.getString(R.string.toast_duplicate_photo_success)
                                    }
                                    onDuplicated(newUri)
                                }
                            }
                        },
                        onPageChange = { page, item ->
                            if (isViewerVisible) {
                                activeViewerMediaId = item.id
                                val found = currentViewerItems.indexOfFirst { it.id == item.id }
                                activeViewerMediaIndex = if (found != -1) found else page
                            }
                        },
                        onEditSuccess = { _ ->
                            timelineViewModel.refreshSilent()
                            toastMessage = editSuccessMsg
                        },
                        onRefreshSilent = {
                            timelineViewModel.refreshSilent()
                        },
                        customAlbums = albums,
                        onCreateAlbumClick = { photo ->
                            selectedMediaIds.clear()
                            selectedMediaIds.add(photo.id)
                            isCreateAlbumFormVisible = true
                        },
                        onAddToAlbumClick = { item, album ->
                            timelineViewModel.addItemsToAlbum(album, listOf(item)) { result ->
                                toastMessage = if (result.addedCount > 0) {
                                    context.getString(R.string.added_to_album_success, result.addedCount, album.name)
                                } else {
                                    context.getString(R.string.photo_already_in_album, album.name)
                                }
                            }
                        },
                        onPickMedia = if (onPickMedia != null) { { onPickMedia(listOf(it)) } } else null
                    )
                }
            }

            AnimatedVisibility(
                visible = externalMediaItem != null,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                modifier = Modifier.fillMaxSize().zIndex(200f)
            ) {
                externalMediaItem?.let { item ->
                    MediaDetailScreen(
                        items = listOf(item),
                        initialIndex = 0,
                        onBackClick = {
                            externalMediaItem = null
                            onDismissExternalView?.invoke()
                        },
                        onPickMedia = if (onPickMedia != null) { { onPickMedia(listOf(it)) } } else null
                    )
                }
            }

            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    initialOffsetY = { -it * 2 }
                ) + scaleIn(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    initialScale = 0.85f
                ),
                exit = slideOutVertically(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    targetOffsetY = { -it * 2 }
                ) + scaleOut(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    targetScale = 0.85f
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .zIndex(100f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = toastMessage.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AddToAlbumBottomSheet(
                isVisible = isAddToAlbumSheetVisible,
                albums = albums,
                onDismiss = { isAddToAlbumSheetVisible = false },
                onCreateNewAlbumClick = {
                    isAddToAlbumSheetVisible = false
                    isCreateAlbumFormVisible = true
                },
                onAlbumSelected = { album ->
                    val itemsToAdd = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                    if (itemsToAdd.isNotEmpty()) {
                        timelineViewModel.addItemsToAlbum(album, itemsToAdd) { result ->
                            toastMessage = when {
                                result.addedCount == 0 -> {
                                    if (itemsToAdd.size == 1) {
                                        context.getString(R.string.photo_already_in_album, album.name)
                                    } else {
                                        context.getString(R.string.items_already_in_album, album.name)
                                    }
                                }
                                result.alreadyPresentCount > 0 -> {
                                    context.getString(R.string.added_to_album_partial_success, result.addedCount, album.name, result.alreadyPresentCount)
                                }
                                else -> {
                                    context.getString(R.string.added_to_album_success, result.addedCount, album.name)
                                }
                            }
                            selectedMediaIds.clear()
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = isCreateAlbumFormVisible,
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
                val selectedPhotos = snapshotItems.filter { selectedMediaIds.contains(it.id) }
                AlbumFormScreen(
                    mode = AlbumFormMode.NEW,
                    initialPhotos = selectedPhotos,
                    onCloseClick = { isCreateAlbumFormVisible = false },
                    onSaveClick = { title, description, coverUri, photos, tags ->
                        val albumName = title.ifBlank { "Untitled album" }
                        timelineViewModel.createAlbumWithPhotos(
                            name = albumName,
                            description = description,
                            coverUri = coverUri,
                            tags = tags,
                            photos = photos
                        ) {
                            toastMessage = context.getString(R.string.album_created_success, albumName)
                            selectedMediaIds.clear()
                        }
                        isCreateAlbumFormVisible = false
                    }
                )
            }

            // Pantalla Completa de Búsqueda de Material 3 Expressive
            AnimatedVisibility(
                visible = isSearchVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ) + fadeOut(animationSpec = motionScheme.defaultEffectsSpec()),
                modifier = Modifier.fillMaxSize().zIndex(160f)
            ) {
                SearchFullScreen(
                    onCloseClick = { isSearchVisible = false },
                    onMediaClick = { mediaItem, searchResults ->
                        val index = snapshotItems.indexOfFirst { it.id == mediaItem.id }
                        activeViewerMediaIndex = if (index != -1) index else 0
                        activeViewerMediaId = mediaItem.id
                        isViewerVisible = true
                    },
                    onAlbumClick = { album ->
                        isSearchVisible = false
                        selectedTab = GalleryTab.COLLECTIONS
                    },
                    onNavigateToVault = {
                        if (!isPickerMode) {
                            isSearchVisible = false
                            isVaultVisible = true
                        }
                    }
                )
            }

            // Subpantalla de Bóveda Oculta
            AnimatedVisibility(
                visible = isVaultVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = motionScheme.defaultSpatialSpec()
                ),
                modifier = Modifier.fillMaxSize().zIndex(170f)
            ) {
                com.ixeken.nierika.feature.collections.HiddenVaultScreen(
                    mediaPagingFlow = timelineViewModel.mediaPagingData,
                    onRefreshSilent = { timelineViewModel.refreshSilent() },
                    onBackClick = {
                        timelineViewModel.refreshSilent()
                        isVaultVisible = false
                    }
                )
            }

            if (uiState.permissionGranted && !userPreferences.hasCompletedWalkthrough) {
                NierikaWalkthroughSheet(
                    onDismissRequest = { timelineViewModel.completeWalkthrough() },
                    onComplete = { timelineViewModel.completeWalkthrough() }
                )
            }
        }
    }
}
}

@Composable
private fun TimelinePhotoGridItem(
    item: MediaItem,
    isSelected: Boolean,
    selectionIndex: Int? = null,
    isSelectionModeActive: Boolean,
    cellShape: androidx.compose.ui.graphics.Shape,
    targetRatio: Float,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem(
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
        targetRatio = targetRatio,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
    )
}
