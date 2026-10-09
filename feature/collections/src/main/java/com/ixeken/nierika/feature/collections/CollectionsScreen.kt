package com.ixeken.nierika.feature.collections

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaTopAppBar
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation
import com.ixeken.nierika.core.domain.model.MediaItem

/**
 * Pantalla principal de la pestaña Collections.
 * Muestra el TopAppBar flotante y transparente con gradiente de sombra,
 * la retícula de álbumes en tarjetas M3 Expressive, cápsulas de categorías y estados completos.
 */
@Composable
fun CollectionsScreen(
    onOpenSettings: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onDetailVisibilityChange: (Boolean) -> Unit = {},
    onPickMedia: ((List<MediaItem>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val collectionsTitle = stringResource(R.string.nav_tab_collections)
    val uiState by viewModel.uiState.collectAsState()
    val trashMedia by viewModel.trashMedia.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val importState by viewModel.albumImportState.collectAsState()
    val zipPreviewInfo by viewModel.zipPreviewInfo.collectAsState()

    var isCreateOrImportSheetVisible by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    var activeAlbumFormMode by remember { mutableStateOf<AlbumFormMode?>(null) }
    var displayedAlbumFormMode by remember { mutableStateOf<AlbumFormMode?>(null) }
    if (activeAlbumFormMode != null) {
        displayedAlbumFormMode = activeAlbumFormMode
    }

    var selectedAlbumForDetail by remember { mutableStateOf<Album?>(null) }
    var displayedAlbumForDetail by remember { mutableStateOf<Album?>(null) }
    if (selectedAlbumForDetail != null) {
        val updated = uiState.albums.find { it.id == selectedAlbumForDetail?.id } ?: selectedAlbumForDetail
        displayedAlbumForDetail = updated
    }

    val openZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectZipArchive(context, uri) { preview ->
                if (preview == null) {
                    Toast.makeText(context, context.getString(R.string.import_album_no_media), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(importState.isCompleted, importState.error) {
        if (importState.isCompleted) {
            val createdId = importState.importedAlbumId
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(
                context,
                context.getString(R.string.import_album_success, importState.albumName),
                Toast.LENGTH_LONG
            ).show()
            if (createdId != null) {
                val album = uiState.albums.find { it.id == createdId }
                if (album != null) {
                    selectedAlbumForDetail = album
                }
            }
            viewModel.resetAlbumImportState()
            viewModel.setZipPreviewInfo(null)
        } else if (importState.error != null) {
            Toast.makeText(
                context,
                context.getString(R.string.import_album_error, importState.error),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetAlbumImportState()
            viewModel.setZipPreviewInfo(null)
        }
    }

    var isTrashDetailVisible by remember { mutableStateOf(false) }

    var selectedCategoryForDetail by remember { mutableStateOf<com.ixeken.nierika.core.domain.model.QuickFilter?>(null) }
    var displayedCategoryForDetail by remember { mutableStateOf<com.ixeken.nierika.core.domain.model.QuickFilter?>(null) }
    if (selectedCategoryForDetail != null) {
        displayedCategoryForDetail = selectedCategoryForDetail
    }


    val isDetailActive = selectedAlbumForDetail != null || activeAlbumFormMode != null || isTrashDetailVisible || selectedCategoryForDetail != null
    SideEffect {
        onDetailVisibilityChange(isDetailActive)
    }

    LaunchedEffect(selectedAlbumForDetail, activeAlbumFormMode, isTrashDetailVisible, selectedCategoryForDetail) {
        onDetailVisibilityChange(isDetailActive)
    }

    BackHandler(enabled = activeAlbumFormMode != null) {
        activeAlbumFormMode = null
        onDetailVisibilityChange(selectedAlbumForDetail != null || isTrashDetailVisible || selectedCategoryForDetail != null)
    }
    BackHandler(enabled = selectedAlbumForDetail != null) {
        selectedAlbumForDetail = null
        onDetailVisibilityChange(activeAlbumFormMode != null || isTrashDetailVisible || selectedCategoryForDetail != null)
    }
    BackHandler(enabled = isTrashDetailVisible) {
        isTrashDetailVisible = false
        onDetailVisibilityChange(selectedAlbumForDetail != null || activeAlbumFormMode != null || selectedCategoryForDetail != null)
    }
    BackHandler(enabled = selectedCategoryForDetail != null) {
        selectedCategoryForDetail = null
        onDetailVisibilityChange(selectedAlbumForDetail != null || activeAlbumFormMode != null || isTrashDetailVisible)
    }

    var pendingDeleteItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var pendingDeleteOnAccepted by remember { mutableStateOf<(() -> Unit)?>(null) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (pendingDeleteItems.isNotEmpty()) {
                viewModel.deletePermanentlyFromDb(pendingDeleteItems.map { it.id })
                pendingDeleteItems = emptyList()
            }
            pendingDeleteOnAccepted?.invoke()
            viewModel.refreshSilent()
        } else {
            pendingDeleteItems = emptyList()
        }
        pendingDeleteOnAccepted = null
    }

    fun requestPermanentDelete(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingDeleteItems = items
        pendingDeleteOnAccepted = onAccepted
        val intentSender = viewModel.createDeleteIntentSender(context, items)
        if (intentSender != null) {
            deleteLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            viewModel.deletePermanentlyFromDb(items.map { it.id })
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
            trashLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            viewModel.moveToTrashBatch(items)
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
                viewModel.onRestoreBatch(pendingRestoreItems)
                pendingRestoreItems = emptyList()
            }
            pendingRestoreOnAccepted?.invoke()
            viewModel.refreshSilent()
        } else {
            pendingRestoreItems = emptyList()
        }
        pendingRestoreOnAccepted = null
    }

    fun requestRestore(items: List<MediaItem>, onAccepted: (() -> Unit)? = null) {
        pendingRestoreItems = items
        pendingRestoreOnAccepted = onAccepted
        val intentSender = viewModel.createTrashIntentSender(context, items, isTrash = false)
        if (intentSender != null) {
            restoreLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        } else {
            viewModel.onRestoreBatch(items)
            pendingRestoreItems = emptyList()
            onAccepted?.invoke()
            pendingRestoreOnAccepted = null
        }
    }

    val handleCategoryClick: (String) -> Unit = { category ->
        when (category) {
            "TRASH" -> {
                isTrashDetailVisible = true
                onDetailVisibilityChange(true)
            }
            "FAVORITES" -> {
                selectedCategoryForDetail = com.ixeken.nierika.core.domain.model.QuickFilter.FAVORITES
                onDetailVisibilityChange(true)
            }
            "VIDEOS" -> {
                selectedCategoryForDetail = com.ixeken.nierika.core.domain.model.QuickFilter.VIDEOS
                onDetailVisibilityChange(true)
            }
            "CAMERA" -> {
                selectedCategoryForDetail = com.ixeken.nierika.core.domain.model.QuickFilter.CAMERA
                onDetailVisibilityChange(true)
            }
            "SCREENSHOTS" -> {
                selectedCategoryForDetail = com.ixeken.nierika.core.domain.model.QuickFilter.SCREENSHOTS
                onDetailVisibilityChange(true)
            }
            "NOT_IN_ALBUM" -> {
                selectedCategoryForDetail = com.ixeken.nierika.core.domain.model.QuickFilter.NOT_IN_ALBUM
                onDetailVisibilityChange(true)
            }
            else -> onCategoryClick(category)
        }
    }

    val isWideScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isWideScreen) 88.dp else 96.dp
    val topBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 84.dp

    val appBarBgColor = MaterialTheme.colorScheme.background
    val topBarScrimBrush = remember(appBarBgColor) {
        Brush.verticalGradient(
            colors = listOf(
                appBarBgColor.copy(alpha = 0.88f),
                appBarBgColor.copy(alpha = 0.60f),
                Color.Transparent
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 152.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = topBarPadding,
                bottom = navBarBottomPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.albumButtonsLocation == AlbumButtonsLocation.TOP) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "category_tiles") {
                    CategoryGridSection(
                        onCategoryClick = handleCategoryClick,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            if (uiState.albums.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "empty_state") {
                    EmptyAlbumsState(
                        onCreateAlbumClick = { isCreateOrImportSheetVisible = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            } else {
                items(
                    items = uiState.albums,
                    key = { it.id },
                    contentType = { "album_card" }
                ) { album ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedAlbumForDetail = album
                                onDetailVisibilityChange(true)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.0f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!album.coverUri.isNullOrEmpty()) {
                                val coverRequest = remember(album.coverUri) {
                                    ImageRequest.Builder(context)
                                        .data(album.coverUri)
                                        .memoryCacheKey("${album.coverUri}_cover")
                                        .crossfade(false)
                                        .build()
                                }
                                AsyncImage(
                                    model = coverRequest,
                                    contentDescription = album.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.album_items_count, album.itemCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (uiState.albumButtonsLocation == AlbumButtonsLocation.BOTTOM) {
                item(span = { GridItemSpan(maxLineSpan) }, contentType = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item(span = { GridItemSpan(maxLineSpan) }, contentType = "category_tiles") {
                    CategoryGridSection(
                        onCategoryClick = handleCategoryClick,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // TopBar flotante estilo Timeline (desaparece al entrar a un álbum o categoría)
        AnimatedVisibility(
            visible = selectedAlbumForDetail == null && activeAlbumFormMode == null && !isTrashDetailVisible && selectedCategoryForDetail == null,
            enter = androidx.compose.animation.fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = androidx.compose.animation.fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarScrimBrush)
                    .statusBarsPadding()
            ) {
                val totalCollections = uiState.albums.size
                val collectionsSubtitle = stringResource(R.string.album_items_count, totalCollections)
                NierikaTopAppBar(
                    title = collectionsTitle,
                    subtitle = collectionsSubtitle,
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    showAppLogo = userPreferences.showAppLogoInTopBar,
                    appLogoRes = if (userPreferences.appLauncherAlias.iconStyle == com.ixeken.nierika.core.domain.model.AppIconStyle.PHOTO_RAINBOW) {
                        R.drawable.ic_launcher_photo_rainbow
                    } else {
                        R.drawable.nierika_logo
                    },
                    actions = {
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.tooltip_create_album)
                        ) {
                            IconButton(
                                onClick = { isCreateOrImportSheetVisible = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.action_add_album),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.action_settings)
                        ) {
                            IconButton(
                                onClick = onOpenSettings
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(R.string.action_settings),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                )
            }
        }

        // Formulario de Crear/Editar Álbum con física spring de arriba hacia abajo
        AnimatedVisibility(
            visible = activeAlbumFormMode != null,
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
            modifier = Modifier.fillMaxSize().zIndex(150f)
        ) {
            displayedAlbumFormMode?.let { mode ->
                AlbumFormScreen(
                    mode = mode,
                    onCloseClick = { activeAlbumFormMode = null },
                    onSaveClick = { title, description, coverUri, photos, tags ->
                        viewModel.onCreateAlbumWithPhotos(
                            name = title.ifBlank { "Untitled album" },
                            description = description,
                            coverUri = coverUri,
                            tags = tags,
                            photos = photos
                        )
                        activeAlbumFormMode = null
                    }
                )
            }
        }

        // Detalle del Álbum Mini-Timeline
        AnimatedVisibility(
            visible = selectedAlbumForDetail != null,
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
            displayedAlbumForDetail?.let { currentAlbum ->
                AlbumDetailScreen(
                    album = currentAlbum,
                    onBackClick = { selectedAlbumForDetail = null },
                    onDeleteAlbumClick = {
                        selectedAlbumForDetail = null
                    },
                    onPickMedia = onPickMedia
                )
            }
        }

        // Detalle de la Papelera
        AnimatedVisibility(
            visible = isTrashDetailVisible,
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
            TrashCategoryDetailScreen(
                trashItems = trashMedia,
                isDeleteConfirmationEnabled = true,
                onBackClick = { isTrashDetailVisible = false },
                onEmptyTrashClick = {
                    if (trashMedia.isNotEmpty()) {
                        requestPermanentDelete(trashMedia)
                    }
                },
                onRestoreAllClick = { requestRestore(trashMedia) },
                onRestoreItem = { item, onAccepted -> requestRestore(listOf(item), onAccepted) },
                onRestoreBatch = { items -> requestRestore(items) },
                onDeleteItemPermanently = { item, onAccepted -> requestPermanentDelete(listOf(item), onAccepted) },
                onDeleteBatchPermanently = { items -> requestPermanentDelete(items) }
            )
        }

        // Detalle de Categorías del Sistema (Favoritos, Vídeos, Cámara, Capturas)
        AnimatedVisibility(
            visible = selectedCategoryForDetail != null,
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
            displayedCategoryForDetail?.let { category ->
                PredefinedCategoryDetailScreen(
                    category = category,
                    onBackClick = { selectedCategoryForDetail = null },
                    onPickMedia = onPickMedia
                )
            }
        }

        // BottomSheet para crear nuevo álbum o importar desde .ZIP
        CreateOrImportBottomSheet(
            isVisible = isCreateOrImportSheetVisible,
            onDismiss = { isCreateOrImportSheetVisible = false },
            onCreateNewAlbumClick = {
                activeAlbumFormMode = AlbumFormMode.NEW
            },
            onImportZipClick = {
                openZipLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "multipart/x-zip"))
            }
        )

        // Diálogo modal de previsualización para confirmar o editar el nombre del álbum antes de importar
        ImportAlbumPreviewDialog(
            previewInfo = zipPreviewInfo,
            onDismiss = { viewModel.setZipPreviewInfo(null) },
            onConfirmImport = { albumName ->
                val uri = zipPreviewInfo?.zipUri
                if (uri != null) {
                    viewModel.importAlbumFromZip(context, uri, albumName)
                }
            }
        )

        // Diálogo modal de progreso de importación con barra ondulada M3 Expressive
        ImportAlbumProgressDialog(
            importState = importState,
            onCancel = {
                viewModel.cancelAlbumImport()
                viewModel.setZipPreviewInfo(null)
            }
        )
    }
}

/**
 * Componente de estado vacío visual M3 Expressive para cuando no existen álbumes creados.
 * Sigue la jerarquía visual de TimelineEmptyState con contenedor tonal de icono y botón primario.
 */
@Composable
private fun EmptyAlbumsState(
    onCreateAlbumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.empty_albums_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.empty_albums_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onCreateAlbumClick()
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.action_create_album),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Mosaico tonal de categoría estilo Material 3 Expressive (Google Photos) con distribución vertical.
 * Contenedor compacto con esquinas redondeadas de 22.dp, icono sólido superior de 26.dp,
 * tipografía secundaria de 11.5.sp optimizada para visualización sin corte y respuesta háptica.
 */
@Composable
private fun CategoryGridTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 74.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            }
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Matriz simétrica de 3 columnas con los 6 mosaicos tonales de categorías del sistema (3×2).
 * Optimiza el espacio vertical en Colecciones organizando los accesos directos en 2 filas compactas.
 */
@Composable
private fun CategoryGridSection(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryGridTile(
                label = stringResource(R.string.category_favorites),
                icon = Icons.Filled.Favorite,
                onClick = { onCategoryClick("FAVORITES") },
                modifier = Modifier.weight(1f)
            )
            CategoryGridTile(
                label = stringResource(R.string.category_videos),
                icon = Icons.Filled.VideoLibrary,
                onClick = { onCategoryClick("VIDEOS") },
                modifier = Modifier.weight(1f)
            )
            CategoryGridTile(
                label = stringResource(R.string.category_screenshots),
                icon = Icons.Filled.Smartphone,
                onClick = { onCategoryClick("SCREENSHOTS") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryGridTile(
                label = stringResource(R.string.category_camera),
                icon = Icons.Filled.PhotoCamera,
                onClick = { onCategoryClick("CAMERA") },
                modifier = Modifier.weight(1f)
            )
            CategoryGridTile(
                label = stringResource(R.string.category_not_in_album),
                icon = Icons.Filled.FolderOff,
                onClick = { onCategoryClick("NOT_IN_ALBUM") },
                modifier = Modifier.weight(1f)
            )
            CategoryGridTile(
                label = stringResource(R.string.category_trash),
                icon = Icons.Filled.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                onClick = { onCategoryClick("TRASH") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
