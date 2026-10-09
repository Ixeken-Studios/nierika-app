package com.ixeken.nierika.feature.viewer

import com.ixeken.nierika.feature.viewer.components.VideoPlayerView
import com.ixeken.nierika.feature.viewer.components.OcrTextOverlay
import com.ixeken.nierika.feature.viewer.components.PhotoInfoBottomSheet
import com.ixeken.nierika.feature.viewer.components.PhotoDeleteBottomSheet
import com.ixeken.nierika.feature.viewer.components.PhotoRestoreBottomSheet
import com.ixeken.nierika.feature.viewer.components.PhotoRemoveExifBottomSheet
import com.ixeken.nierika.feature.editor.EditorActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import com.ixeken.nierika.core.mediastore.renameMediaFile
import com.ixeken.nierika.core.mediastore.removeExifMetadata
import com.ixeken.nierika.core.mediastore.updateMediaDateTime
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import com.ixeken.nierika.core.mediastore.VideoFrameExtractor
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaRichTooltipBox
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.viewer.components.ZoomableMediaContainer
import com.ixeken.nierika.feature.viewer.components.MediaThumbnailHeroCarousel
import com.ixeken.nierika.feature.viewer.components.MediaViewerActionsPill
import com.ixeken.nierika.feature.viewer.components.MediaViewerBottomBar
import com.ixeken.nierika.feature.viewer.components.MediaViewerTopBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.unit.DpOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.compose.runtime.mutableIntStateOf
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Size
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla inmersiva de visualización de foto individual en negro puro (#000000).
 * Incluye un M3 Vertical Menu desplegable con animación M3 Expressive Motion y morphing del icono de 3 puntos a cruz.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaDetailScreen(
    items: List<MediaItem>,
    initialIndex: Int,
    onBackClick: () -> Unit,
    onDeleteConfirm: (MediaItem, onAccepted: () -> Unit) -> Unit = { _, onAccepted -> onAccepted() },
    onDeletePermanentlyConfirm: (MediaItem, onAccepted: () -> Unit) -> Unit = { _, onAccepted -> onAccepted() },
    onRestoreConfirm: (MediaItem, onAccepted: () -> Unit) -> Unit = { _, onAccepted -> onAccepted() },
    onToggleFavorite: (MediaItem) -> Unit = {},
    onDuplicateClick: (MediaItem, (String?) -> Unit) -> Unit = { _, _ -> },
    onEditClick: (MediaItem) -> Unit = {},
    onEditSuccess: (String) -> Unit = {},
    onRefreshSilent: () -> Unit = {},
    onPageChange: (Int, MediaItem) -> Unit = { _, _ -> },
    customAlbums: List<Album> = emptyList(),
    onCreateAlbumClick: (MediaItem) -> Unit = {},
    onAddToAlbumClick: (MediaItem, Album) -> Unit = { _, _ -> },
    onRemoveFromAlbumClick: ((MediaItem, onAccepted: () -> Unit) -> Unit)? = null,
    isInTrash: Boolean = false,
    isInVault: Boolean = false,
    onRestoreFromVaultClick: ((MediaItem, onAccepted: () -> Unit) -> Unit)? = null,
    onPickMedia: ((MediaItem) -> Unit)? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    viewerViewModel: MediaViewerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    // Lista optimista de IDs descartados/eliminados en memoria para brindar feedback instantáneo (0ms)
    // sin tener que esperar a que los callbacks asíncronos de Room o MediaStore refresquen el repositorio.
    val locallyDismissedIds = remember { mutableStateListOf<Long>() }
    val activeItems = remember(items, locallyDismissedIds.toList()) {
        if (locallyDismissedIds.isEmpty()) items else items.filter { it.id !in locallyDismissedIds }
    }

    // Sincronización: cuando el repositorio externo finalmente confirma la eliminación y la lista se actualiza,
    // se remueven los IDs de la lista optimista para evitar acumulación innecesaria de estado en memoria.
    LaunchedEffect(items) {
        if (locallyDismissedIds.isNotEmpty()) {
            locallyDismissedIds.removeAll { id -> items.none { it.id == id } }
        }
    }

    // Si todos los elementos fueron eliminados o removidos, cerrar automáticamente el visor
    LaunchedEffect(activeItems.isEmpty()) {
        if (activeItems.isEmpty()) {
            onBackClick()
        }
    }

    if (activeItems.isEmpty()) return

    val userPreferences by viewerViewModel.userPreferences.collectAsStateWithLifecycle()
    val isThumbnailCarouselEnabled = userPreferences.isThumbnailCarouselEnabled
    val isDeleteConfirmationEnabled = userPreferences.isDeleteConfirmationEnabled
    val isVideoMuted = userPreferences.isVideoMuted

    val view = LocalView.current

    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper && ctx !is Activity) {
            ctx = ctx.baseContext
        }
        ctx as? Activity
    }
    val safeInitialIndex = initialIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val targetInitialId = items.getOrNull(safeInitialIndex)?.id
    val pagerState = rememberPagerState(initialPage = safeInitialIndex) { activeItems.size }
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var targetMediaUriToFocus by remember { mutableStateOf<String?>(null) }
    var currentMediaId by remember { mutableStateOf<Long?>(targetInitialId) }

    // Sincronización cuando el identificador inicial externo cambia intencionalmente a otra foto
    // desde la cuadrícula externa, evitando sobreescrituras indeseadas mientras el usuario desliza con el dedo.
    LaunchedEffect(targetInitialId) {
        val targetId = targetInitialId ?: return@LaunchedEffect
        if (targetId != currentMediaId) {
            val matchingIndex = activeItems.indexOfFirst { it.id == targetId }
            if (matchingIndex != -1 && matchingIndex != pagerState.currentPage) {
                pagerState.scrollToPage(matchingIndex)
            }
            currentMediaId = targetId
        }
    }

    LaunchedEffect(pagerState.currentPage, activeItems) {
        val current = activeItems.getOrNull(pagerState.currentPage) ?: activeItems.lastOrNull()
        if (current != null) {
            currentMediaId = current.id
            val resolvedPage = activeItems.indexOfFirst { it.id == current.id }.coerceAtLeast(0)
            onPageChange(resolvedPage, current)
        }
    }

    // Asegurar que si la lista de items se recarga o desplaza al volver de segundo plano, el visor permanezca exactamente en la foto activa
    LaunchedEffect(activeItems) {
        val idToFind = currentMediaId ?: return@LaunchedEffect
        val matchingIndex = activeItems.indexOfFirst { it.id == idToFind }
        if (matchingIndex != -1 && matchingIndex != pagerState.currentPage) {
            pagerState.scrollToPage(matchingIndex)
        }
    }

    // Al duplicar una foto/video, desplaza el visor automáticamente hacia el nuevo elemento y centra el carrusel
    LaunchedEffect(activeItems, targetMediaUriToFocus) {
        val targetUri = targetMediaUriToFocus ?: return@LaunchedEffect
        val newIndex = activeItems.indexOfFirst { it.uri == targetUri }
        if (newIndex != -1) {
            pagerState.animateScrollToPage(
                page = newIndex,
                animationSpec = motionScheme.defaultSpatialSpec()
            )
            targetMediaUriToFocus = null
        }
    }

    val currentItem = activeItems.getOrNull(pagerState.currentPage) ?: activeItems.firstOrNull() ?: items.first()

    var isEditingActive by remember { mutableStateOf(false) }

    val editorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isEditingActive = false
        if (result.resultCode == Activity.RESULT_OK) {
            val savedUri = result.data?.dataString
                ?: result.data?.getStringExtra(EditorActivity.EXTRA_SAVED_URI)
            if (!savedUri.isNullOrBlank()) {
                targetMediaUriToFocus = savedUri
                onEditSuccess(savedUri)
            }
        }
    }

    var isLandscapeMode by remember { mutableStateOf(false) }

    // Restaurar orientación a portrait cuando el composable se desmonta
    DisposableEffect(activity) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // En modo landscape, back sale del landscape primero. En modo normal, sale de la pantalla.
    BackHandler {
        if (isLandscapeMode) {
            isLandscapeMode = false
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            onBackClick()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isCompactLandscape = isLandscape && configuration.screenHeightDp < 520

    var isControlsVisible by remember { mutableStateOf(true) }
    var isZoomedIn by remember { mutableStateOf(false) }
    var currentVideoPositionMs by remember { mutableLongStateOf(0L) }
    var isTopMenuExpanded by remember { mutableStateOf(false) }
    var isSubmenuVisible by remember { mutableStateOf(false) }

    var isOcrActive by remember { mutableStateOf(false) }
    var isOcrScanning by remember { mutableStateOf(false) }
    var ocrTextBlocks by remember { mutableStateOf<List<Text.TextBlock>>(emptyList()) }
    var ocrImageWidth by remember { mutableIntStateOf(0) }
    var ocrImageHeight by remember { mutableIntStateOf(0) }

    BackHandler(enabled = isOcrActive) {
        isOcrActive = false
    }

    val favoritesMap = remember { mutableStateMapOf<Long, Boolean>() }
    val isFavorite = favoritesMap[currentItem.id] ?: currentItem.isFavorite
    val isItemInTrash = isInTrash || currentItem.isTrash
    var isInfoSheetVisible by remember { mutableStateOf(false) }
    var isDeleteSheetVisible by remember { mutableStateOf(false) }
    var isRestoreSheetVisible by remember { mutableStateOf(false) }
    var isRemoveExifSheetVisible by remember { mutableStateOf(false) }

    var isUserInteracting by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Auto-ocultamiento inteligente de controles tras 3.5s si se está reproduciendo un video y el usuario no está interactuando
    LaunchedEffect(
        isControlsVisible,
        pagerState.currentPage,
        isUserInteracting,
        lastInteractionTime,
        isTopMenuExpanded,
        isInfoSheetVisible,
        isDeleteSheetVisible,
        isRestoreSheetVisible,
        isRemoveExifSheetVisible
    ) {
        if (isControlsVisible) {
            val item = activeItems.getOrNull(pagerState.currentPage)
            if (item?.isVideo == true) {
                val hasActiveInteraction = isUserInteracting ||
                        isTopMenuExpanded ||
                        isInfoSheetVisible ||
                        isDeleteSheetVisible ||
                        isRestoreSheetVisible ||
                        isRemoveExifSheetVisible
                if (!hasActiveInteraction) {
                    val elapsed = System.currentTimeMillis() - lastInteractionTime
                    val remaining = (3500L - elapsed).coerceAtLeast(0L)
                    delay(remaining)
                    if (isControlsVisible && !isUserInteracting && !isTopMenuExpanded && !isInfoSheetVisible && !isDeleteSheetVisible && !isRestoreSheetVisible && !isRemoveExifSheetVisible) {
                        isControlsVisible = false
                    }
                }
            }
        }
    }

    val localDateTimeOverrides = remember { mutableStateMapOf<Long, Long>() }
    var pendingExifRemovalUri by remember { mutableStateOf<Uri?>(null) }
    var pendingDateTimeUpdate by remember { mutableStateOf<Pair<Uri, Long>?>(null) }
    var pendingRenameFile by remember { mutableStateOf<Pair<Uri, String>?>(null) }

    val resolvedCurrentItem = remember(currentItem, localDateTimeOverrides[currentItem.id]) {
        localDateTimeOverrides[currentItem.id]?.let { overrideMillis ->
            currentItem.copy(dateTakenMillis = overrideMillis)
        } ?: currentItem
    }

    val exifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingExifRemovalUri?.let { uri ->
                coroutineScope.launch(Dispatchers.IO) {
                    val retryResult = context.removeExifMetadata(uri)
                    withContext(Dispatchers.Main) {
                        if (retryResult is com.ixeken.nierika.core.mediastore.ExifRemovalResult.Success) {
                            Toast.makeText(context, context.getString(R.string.remove_exif_success), Toast.LENGTH_SHORT).show()
                            onRefreshSilent()
                        } else if (retryResult is com.ixeken.nierika.core.mediastore.ExifRemovalResult.Error) {
                            val msg = when (retryResult.code) {
                                com.ixeken.nierika.core.mediastore.ExifErrorCode.READ_FAILED -> context.getString(R.string.remove_exif_error_read)
                                com.ixeken.nierika.core.mediastore.ExifErrorCode.WRITE_FAILED -> context.getString(R.string.remove_exif_error_write)
                                com.ixeken.nierika.core.mediastore.ExifErrorCode.CORRUPT_OR_EMPTY -> context.getString(R.string.remove_exif_error_corrupt)
                                com.ixeken.nierika.core.mediastore.ExifErrorCode.UNKNOWN -> retryResult.message ?: context.getString(R.string.remove_exif_error)
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            pendingDateTimeUpdate?.let { (uri, newTimestamp) ->
                coroutineScope.launch(Dispatchers.IO) {
                    val retryResult = context.updateMediaDateTime(uri, newTimestamp)
                    withContext(Dispatchers.Main) {
                        if (retryResult is com.ixeken.nierika.core.mediastore.UpdateDateTimeResult.Success) {
                            localDateTimeOverrides[currentItem.id] = newTimestamp
                            Toast.makeText(context, context.getString(R.string.edit_date_time_success), Toast.LENGTH_SHORT).show()
                            onRefreshSilent()
                        } else if (retryResult is com.ixeken.nierika.core.mediastore.UpdateDateTimeResult.Error) {
                            Toast.makeText(context, retryResult.message ?: context.getString(R.string.edit_date_time_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            pendingRenameFile?.let { (uri, newName) ->
                coroutineScope.launch(Dispatchers.IO) {
                    val retryResult = context.renameMediaFile(uri, newName)
                    withContext(Dispatchers.Main) {
                        if (retryResult is com.ixeken.nierika.core.mediastore.RenameResult.Success) {
                            Toast.makeText(context, context.getString(R.string.rename_success), Toast.LENGTH_SHORT).show()
                            onRefreshSilent()
                        } else if (retryResult is com.ixeken.nierika.core.mediastore.RenameResult.Error) {
                            Toast.makeText(context, retryResult.message ?: context.getString(R.string.rename_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(context, context.getString(R.string.remove_exif_permission_denied), Toast.LENGTH_SHORT).show()
        }
        pendingExifRemovalUri = null
        pendingDateTimeUpdate = null
        pendingRenameFile = null
    }

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        pendingExifRemovalUri?.let { uri ->
            coroutineScope.launch(Dispatchers.IO) {
                val retryResult = context.removeExifMetadata(uri)
                withContext(Dispatchers.Main) {
                    if (retryResult is com.ixeken.nierika.core.mediastore.ExifRemovalResult.Success) {
                        Toast.makeText(context, context.getString(R.string.remove_exif_success), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        pendingExifRemovalUri = null
    }

    /**
     * Avanza el visor de manera optimista e inmediata (0ms) al siguiente elemento (o al previo si era el último),
     * añadiendo el elemento a descartar a `locallyDismissedIds` para que desaparezca de la composición al instante
     * sin congelar la interfaz esperando respuestas lentas de Room o MediaStore.
     */
    val advanceToNextItemOptimistically: (MediaItem) -> Unit = { dismissedItem ->
        val currentIdx = pagerState.currentPage
        if (currentIdx >= activeItems.size - 1) {
            val prevIdx = (activeItems.size - 2).coerceAtLeast(0)
            coroutineScope.launch {
                pagerState.scrollToPage(prevIdx)
            }
            activeItems.getOrNull(prevIdx)?.let { prevItem ->
                currentMediaId = prevItem.id
            }
        } else {
            activeItems.getOrNull(currentIdx + 1)?.let { nextItem ->
                currentMediaId = nextItem.id
            }
        }
        locallyDismissedIds.add(dismissedItem.id)
    }

    val handleDeleteCurrentItem: () -> Unit = {
        val itemToDelete = currentItem
        val onAccepted: () -> Unit = {
            if (activeItems.size <= 1) {
                onBackClick()
            } else {
                advanceToNextItemOptimistically(itemToDelete)
            }
        }
        if (isItemInTrash || isInVault) {
            onDeletePermanentlyConfirm(itemToDelete, onAccepted)
        } else {
            onDeleteConfirm(itemToDelete, onAccepted)
        }
    }

    val handleRestoreCurrentItem: () -> Unit = {
        val itemToRestore = currentItem
        val onAccepted: () -> Unit = {
            if (activeItems.size <= 1) {
                onBackClick()
            } else {
                advanceToNextItemOptimistically(itemToRestore)
            }
        }
        onRestoreConfirm(itemToRestore, onAccepted)
    }

    val handleRestoreFromVault: () -> Unit = {
        val itemToRestore = currentItem
        val onAccepted: () -> Unit = {
            if (activeItems.size <= 1) {
                onBackClick()
            } else {
                advanceToNextItemOptimistically(itemToRestore)
            }
        }
        onRestoreFromVaultClick?.invoke(itemToRestore, onAccepted)
    }

    val handleRemoveFromAlbum: () -> Unit = {
        val itemToRemove = currentItem
        val onAccepted: () -> Unit = {
            if (activeItems.size <= 1) {
                onBackClick()
            } else {
                advanceToNextItemOptimistically(itemToRemove)
            }
        }
        onRemoveFromAlbumClick?.invoke(itemToRemove, onAccepted)
    }

    val dateMonthFormatter = remember { SimpleDateFormat("MMMM d", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val dateLine1 = remember(resolvedCurrentItem.dateTakenMillis) {
        dateMonthFormatter.format(Date(resolvedCurrentItem.dateTakenMillis))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    val timeLine2 = remember(resolvedCurrentItem.dateTakenMillis) {
        timeFormatter.format(Date(resolvedCurrentItem.dateTakenMillis))
    }

    val systemShortDateFormat = remember(context) { android.text.format.DateFormat.getDateFormat(context) }
    val systemTimeFormat = remember(context) { android.text.format.DateFormat.getTimeFormat(context) }
    val compactDateTimeLine = remember(resolvedCurrentItem.dateTakenMillis, systemShortDateFormat, systemTimeFormat) {
        val date = Date(resolvedCurrentItem.dateTakenMillis)
        "${systemShortDateFormat.format(date)} ${systemTimeFormat.format(date)}"
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val adaptiveHorizontalPadding = remember(screenWidthDp) {
        when {
            screenWidthDp >= 840 -> 32.dp
            screenWidthDp >= 600 -> 24.dp
            else -> 16.dp
        }
    }

    var isInPipMode by remember { mutableStateOf(activity?.isInPictureInPictureMode == true) }

    DisposableEffect(activity) {
        val act = activity as? androidx.activity.ComponentActivity
        if (act == null) return@DisposableEffect onDispose {}

        val consumer = androidx.core.util.Consumer<androidx.core.app.PictureInPictureModeChangedInfo> { info ->
            isInPipMode = info.isInPictureInPictureMode
        }
        act.addOnPictureInPictureModeChangedListener(consumer)

        onDispose {
            act.removeOnPictureInPictureModeChangedListener(consumer)
        }
    }

    var offsetY by remember { mutableFloatStateOf(0f) }
    val maxDismissDistancePx = with(LocalDensity.current) { 240.dp.toPx() }

    val isDraggingToDismiss = kotlin.math.abs(offsetY) > 0f
    val areControlsVisible = isControlsVisible && !isDraggingToDismiss && !isOcrActive && !isInPipMode && !isInfoSheetVisible

    DisposableEffect(areControlsVisible) {
        val window = (view.context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, view) }
        val originalLightStatusBars = insetsController?.isAppearanceLightStatusBars ?: false

        insetsController?.isAppearanceLightStatusBars = false
        insetsController?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (areControlsVisible) {
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        } else {
            insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        }

        onDispose {
            insetsController?.isAppearanceLightStatusBars = originalLightStatusBars
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "swipeToDismissY"
    )

    val backgroundAlpha = (1f - (kotlin.math.abs(animatedOffsetY) / maxDismissDistancePx)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val anyPressed = event.changes.any { it.pressed }
                        isUserInteracting = anyPressed
                        lastInteractionTime = System.currentTimeMillis()
                    }
                }
            }
            .background(Color.Black.copy(alpha = backgroundAlpha))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = animatedOffsetY
                    val scaleFactor = (1f - (kotlin.math.abs(animatedOffsetY) / (maxDismissDistancePx * 3f))).coerceIn(0.78f, 1f)
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .pointerInput(isZoomedIn) {
                    if (!isZoomedIn) {
                        var hasTriggeredThresholdHaptic = false
                        val dismissThreshold = maxDismissDistancePx * 0.45f
                        detectVerticalDragGestures(
                            onDragStart = {
                                hasTriggeredThresholdHaptic = false
                            },
                            onDragEnd = {
                                if (kotlin.math.abs(offsetY) >= dismissThreshold) {
                                    onBackClick()
                                }
                                offsetY = 0f
                                hasTriggeredThresholdHaptic = false
                            },
                            onDragCancel = {
                                offsetY = 0f
                                hasTriggeredThresholdHaptic = false
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                offsetY += dragAmount
                                val isPast = kotlin.math.abs(offsetY) >= dismissThreshold
                                if (isPast && !hasTriggeredThresholdHaptic) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    hasTriggeredThresholdHaptic = true
                                } else if (!isPast && hasTriggeredThresholdHaptic) {
                                    hasTriggeredThresholdHaptic = false
                                }
                            }
                        )
                    }
                }
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = if (isInPipMode) 0.dp else 16.dp,
                userScrollEnabled = !isZoomedIn && !isInPipMode,
                modifier = Modifier.fillMaxSize(),
                key = { pageIndex -> activeItems.getOrNull(pageIndex)?.id ?: pageIndex }
            ) { pageIndex ->
                val pageItem = activeItems.getOrNull(pageIndex) ?: return@HorizontalPager
                if (pageItem.isVideo) {
                    VideoPlayerView(
                        uriString = pageItem.uri,
                        isCurrentPage = (pageIndex == pagerState.currentPage) && !isEditingActive,
                        areControlsVisible = areControlsVisible,
                        onToggleControls = { isControlsVisible = !isControlsVisible },
                        isVideoMuted = isVideoMuted,
                        onToggleMute = viewerViewModel::setVideoMuted,
                        hasCarousel = (activeItems.size > 1) && isThumbnailCarouselEnabled && !isLandscape && !isLandscapeMode,
                        onPositionChanged = { pos -> currentVideoPositionMs = pos },
                        onZoomStateChanged = { zoomed -> isZoomedIn = zoomed },
                        onDismiss = onBackClick
                    )
                } else {
                    ZoomableImagePage(
                        mediaItem = pageItem,
                        isCurrentPage = (pageIndex == pagerState.currentPage) && !isEditingActive,
                        onToggleControls = { isControlsVisible = !isControlsVisible },
                        onZoomStateChanged = { zoomed -> isZoomedIn = zoomed },
                        onDismiss = onBackClick
                    )
                }
            }
        }



        MediaViewerTopBar(
            isVisible = areControlsVisible,
            isDraggingToDismiss = isDraggingToDismiss,
            isLandscape = isLandscape,
            isCompactLandscape = isCompactLandscape,
            isInfoSheetVisible = isInfoSheetVisible,
            screenWidthDp = screenWidthDp,
            adaptiveHorizontalPadding = adaptiveHorizontalPadding,
            dateLine1 = dateLine1,
            timeLine2 = timeLine2,
            compactDateTimeLine = compactDateTimeLine,
            onBackClick = onBackClick,
            actionsContent = if (isCompactLandscape) {
                {
                    MediaViewerActionsPill(
                        isCompact = true,
                        isLandscape = isLandscape,
                        isItemInTrash = isItemInTrash,
                        isInVault = isInVault,
                        isFavorite = isFavorite,
                        onPickMedia = if (onPickMedia != null) { { onPickMedia(currentItem) } } else null,
                        onRestoreClick = {
                            if (isDeleteConfirmationEnabled) {
                                isRestoreSheetVisible = true
                            } else {
                                handleRestoreCurrentItem()
                            }
                        },
                        onRestoreFromVaultClick = if (onRestoreFromVaultClick != null) { { handleRestoreFromVault() } } else null,
                        onInfoClick = { isInfoSheetVisible = true },
                        onEditClick = {
                            isEditingActive = true
                            onEditClick(currentItem)
                            val intent = EditorActivity.createIntent(
                                context = context,
                                mediaUri = currentItem.uri,
                                isVideo = currentItem.isVideo
                            )
                            editorLauncher.launch(intent)
                        },
                        onToggleFavorite = {
                            val nextFav = !isFavorite
                            favoritesMap[currentItem.id] = nextFav
                            onToggleFavorite(currentItem.copy(isFavorite = nextFav))
                        },
                        onRemoveFromAlbumClick = if (onRemoveFromAlbumClick != null) { { handleRemoveFromAlbum() } } else null
                    )
                }
            } else null,
            menuContent = {
                Box {
                            NierikaPlainTooltipBox(
                                tooltipText = stringResource(R.string.tooltip_more_options)
                            ) {
                                IconButtonWithAnimatedShape(
                                    onClick = {
                                        if (isTopMenuExpanded) {
                                            isTopMenuExpanded = false
                                            isSubmenuVisible = false
                                        } else {
                                            isTopMenuExpanded = true
                                        }
                                    },
                                    buttonSize = if (isLandscape) 40.dp else 48.dp,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    shadowElevation = 4.dp
                                ) {
                                    AnimatedContent(
                                        targetState = isTopMenuExpanded,
                                        transitionSpec = {
                                            (scaleIn(
                                                animationSpec = motionScheme.fastSpatialSpec()
                                            ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec())).togetherWith(
                                                scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                                            )
                                        },
                                        label = "menuIconMorph"
                                    ) { expanded ->
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.MoreVert,
                                            contentDescription = stringResource(R.string.action_more_options),
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                                        )
                                    }
                                }
                            }

                            DropdownMenu(
                                expanded = isTopMenuExpanded,
                            onDismissRequest = {
                                isTopMenuExpanded = false
                                isSubmenuVisible = false
                            },
                            offset = DpOffset(x = 0.dp, y = 12.dp),
                            shape = RoundedCornerShape(24.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 6.dp,
                            modifier = Modifier.width(260.dp)
                        ) {
                            AnimatedContent(
                                targetState = isSubmenuVisible,
                                transitionSpec = {
                                    (fadeIn(animationSpec = motionScheme.fastEffectsSpec()) + slideInHorizontally { it / 2 }).togetherWith(
                                        fadeOut(animationSpec = motionScheme.fastEffectsSpec()) + slideOutHorizontally { -it / 2 }
                                    )
                                },
                                label = "verticalMenuSubmenu"
                            ) { inSubmenu ->
                                if (!inSubmenu) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        if (isInTrash) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.menu_restore),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Filled.RestoreFromTrash,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    isTopMenuExpanded = false
                                                    isSubmenuVisible = false
                                                    if (isDeleteConfirmationEnabled) {
                                                        isRestoreSheetVisible = true
                                                    } else {
                                                        handleRestoreCurrentItem()
                                                    }
                                                }
                                            )

                                            HorizontalDivider(
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.menu_copy_to_clipboard),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.FileCopy,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                isTopMenuExpanded = false
                                                isSubmenuVisible = false
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newUri(context.contentResolver, "Image", Uri.parse(currentItem.uri))
                                                clipboard.setPrimaryClip(clip)
                                            }
                                        )

                                        if (currentItem.isVideo) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.menu_save_frame),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Filled.PhotoCamera,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    isTopMenuExpanded = false
                                                    isSubmenuVisible = false
                                                    coroutineScope.launch {
                                                        val result = VideoFrameExtractor.saveVideoFrame(
                                                            context = context,
                                                            videoUri = Uri.parse(currentItem.uri),
                                                            positionMs = currentVideoPositionMs
                                                        )
                                                        if (result.isSuccess) {
                                                            onRefreshSilent()
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.video_frame_saved_success),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.video_frame_save_error),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            )

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = stringResource(R.string.video_cd_pip),
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.PictureInPictureAlt,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        isTopMenuExpanded = false
                                                        isSubmenuVisible = false
                                                        val act = activity
                                                        if (act != null && !act.isFinishing && !act.isDestroyed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !act.isInPictureInPictureMode) {
                                                            try {
                                                                val builder = android.app.PictureInPictureParams.Builder()
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                                    builder.setAutoEnterEnabled(true)
                                                                }
                                                                act.enterPictureInPictureMode(builder.build())
                                                            } catch (t: Throwable) { }
                                                        }
                                                    }
                                                )
                                            }

                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.menu_open_with),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    isTopMenuExpanded = false
                                                    isSubmenuVisible = false
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            setDataAndType(Uri.parse(currentItem.uri), "video/*")
                                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }
                                                        val chooser = Intent.createChooser(intent, context.getString(R.string.menu_open_with))
                                                        context.startActivity(chooser)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.menu_detect_text),
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Filled.DocumentScanner,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                },
                                                onClick = {
                                                    isTopMenuExpanded = false
                                                    isSubmenuVisible = false
                                                    isOcrScanning = true
                                                    coroutineScope.launch {
                                                        try {
                                                            val uri = Uri.parse(currentItem.uri)
                                                            val inputImage = prepareInputImageForOcr(context, uri)
                                                            ocrImageWidth = if (currentItem.width > 0) currentItem.width else inputImage.width
                                                            ocrImageHeight = if (currentItem.height > 0) currentItem.height else inputImage.height

                                                            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                                            val visionText = withContext(Dispatchers.IO) {
                                                                suspendCancellableCoroutine { continuation ->
                                                                    recognizer.process(inputImage)
                                                                        .addOnSuccessListener { result ->
                                                                            if (continuation.isActive) {
                                                                                continuation.resume(result)
                                                                            }
                                                                        }
                                                                        .addOnFailureListener { error ->
                                                                            if (continuation.isActive) {
                                                                                continuation.resumeWithException(error)
                                                                            }
                                                                        }
                                                                }
                                                            }
                                                            isOcrScanning = false
                                                            if (visionText.textBlocks.isEmpty()) {
                                                                Toast.makeText(context, context.getString(R.string.ocr_no_text_found), Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                ocrTextBlocks = visionText.textBlocks
                                                                isOcrActive = true
                                                            }
                                                        } catch (_: Throwable) {
                                                            isOcrScanning = false
                                                            Toast.makeText(context, context.getString(R.string.ocr_no_text_found), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.menu_duplicate),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.AddToPhotos,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                isTopMenuExpanded = false
                                                isSubmenuVisible = false
                                                onDuplicateClick(currentItem) { newUri ->
                                                    targetMediaUriToFocus = newUri
                                                }
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.menu_rotate),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.ScreenRotation,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                isTopMenuExpanded = false
                                                isSubmenuVisible = false
                                                if (isLandscapeMode) {
                                                    isLandscapeMode = false
                                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                                } else {
                                                    isLandscapeMode = true
                                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                                }
                                            }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.menu_add_to),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.CreateNewFolder,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = { isSubmenuVisible = true }
                                        )
                                    }
                                } else {
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isSubmenuVisible = false }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.action_back),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.menu_add_to),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.album_form_new_title),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.Add,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                isTopMenuExpanded = false
                                                isSubmenuVisible = false
                                                onCreateAlbumClick(currentItem)
                                            }
                                        )

                                        val recentAlbums = remember(customAlbums) { customAlbums.take(5) }
                                        if (recentAlbums.isNotEmpty()) {
                                            recentAlbums.forEach { album ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = album.name,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Filled.Folder,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    },
                                                    onClick = {
                                                        isTopMenuExpanded = false
                                                        isSubmenuVisible = false
                                                        onAddToAlbumClick(currentItem, album)
                                                    }
                                                )
                                            }
                                        } else {
                                            DropdownMenuItem(
                                                enabled = false,
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.album_no_custom_albums),
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                onClick = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter)
            )

        if (!isInfoSheetVisible) {
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = slideInVertically(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    initialOffsetY = { it * 3 }
                ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
                exit = if (isDraggingToDismiss) androidx.compose.animation.ExitTransition.None
                else slideOutVertically(
                    animationSpec = motionScheme.fastSpatialSpec(),
                    targetOffsetY = { it * 3 }
                ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = if (isLandscape) 4.dp else if (screenWidthDp >= 600) 24.dp else 16.dp)
                ) {
                    if (isThumbnailCarouselEnabled && !isLandscape && !isLandscapeMode) {
                        MediaThumbnailHeroCarousel(
                            mediaItems = activeItems,
                            pagerState = pagerState,
                            onSelectIndex = { index ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = index,
                                        animationSpec = motionScheme.fastSpatialSpec()
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    MediaViewerBottomBar(
                        isLandscape = isLandscape,
                        isCompactLandscape = isCompactLandscape,
                        isItemInTrash = isItemInTrash,
                        isInVault = isInVault,
                        isFavorite = isFavorite,
                        adaptiveHorizontalPadding = adaptiveHorizontalPadding,
                        onShareClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/*"
                                putExtra(Intent.EXTRA_STREAM, Uri.parse(currentItem.uri))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, null))
                        },
                        onDeleteClick = {
                            if (isItemInTrash || isInVault) {
                                if (isDeleteConfirmationEnabled) {
                                    isDeleteSheetVisible = true
                                } else {
                                    handleDeleteCurrentItem()
                                }
                            } else if (isDeleteConfirmationEnabled) {
                                isDeleteSheetVisible = true
                            } else {
                                handleDeleteCurrentItem()
                            }
                        },
                        onPickMedia = if (onPickMedia != null) { { onPickMedia(currentItem) } } else null,
                        onRestoreClick = {
                            if (isDeleteConfirmationEnabled) {
                                isRestoreSheetVisible = true
                            } else {
                                handleRestoreCurrentItem()
                            }
                        },
                        onRestoreFromVaultClick = if (onRestoreFromVaultClick != null) { { handleRestoreFromVault() } } else null,
                        onInfoClick = { isInfoSheetVisible = true },
                        onEditClick = {
                            isEditingActive = true
                            onEditClick(currentItem)
                            val intent = EditorActivity.createIntent(
                                context = context,
                                mediaUri = currentItem.uri,
                                isVideo = currentItem.isVideo
                            )
                            editorLauncher.launch(intent)
                        },
                        onToggleFavorite = {
                            val nextFav = !isFavorite
                            favoritesMap[currentItem.id] = nextFav
                            onToggleFavorite(currentItem.copy(isFavorite = nextFav))
                        },
                        onRemoveFromAlbumClick = if (onRemoveFromAlbumClick != null) { { handleRemoveFromAlbum() } } else null
                    )
                }
            }
        }

        val currentPhotoTags by remember(resolvedCurrentItem.id) {
            viewerViewModel.getTagsForMedia(resolvedCurrentItem.id)
        }.collectAsState(initial = emptyList())

        PhotoInfoBottomSheet(
            isVisible = isInfoSheetVisible,
            mediaItem = resolvedCurrentItem,
            tags = currentPhotoTags,
            onDismiss = { isInfoSheetVisible = false },
            onAddTag = { newTag ->
                viewerViewModel.addTag(resolvedCurrentItem.id, newTag)
            },
            onRemoveTag = { tag ->
                viewerViewModel.removeTag(resolvedCurrentItem.id, tag)
            },
            onRemoveMetadata = {
                val uri = Uri.parse(currentItem.uri)
                pendingExifRemovalUri = uri
                isRemoveExifSheetVisible = true
            },
            onRenameFile = { newFullName ->
                val uri = Uri.parse(currentItem.uri)
                coroutineScope.launch(Dispatchers.IO) {
                    val result = context.renameMediaFile(uri, newFullName)
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is com.ixeken.nierika.core.mediastore.RenameResult.Success -> {
                                Toast.makeText(context, context.getString(R.string.rename_success), Toast.LENGTH_SHORT).show()
                                onRefreshSilent()
                            }
                            is com.ixeken.nierika.core.mediastore.RenameResult.PermissionRequired -> {
                                val sender = result.intentSender
                                if (sender != null) {
                                    pendingRenameFile = uri to newFullName
                                    val request = IntentSenderRequest.Builder(sender).build()
                                    exifPermissionLauncher.launch(request)
                                }
                            }
                            is com.ixeken.nierika.core.mediastore.RenameResult.Error -> {
                                val msg = result.message ?: context.getString(R.string.rename_error)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            },
            onUpdateDateTime = { newTimestampMillis ->
                val uri = Uri.parse(currentItem.uri)
                coroutineScope.launch(Dispatchers.IO) {
                    val result = context.updateMediaDateTime(uri, newTimestampMillis)
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is com.ixeken.nierika.core.mediastore.UpdateDateTimeResult.Success -> {
                                localDateTimeOverrides[currentItem.id] = newTimestampMillis
                                Toast.makeText(context, context.getString(R.string.edit_date_time_success), Toast.LENGTH_SHORT).show()
                                onRefreshSilent()
                            }
                            is com.ixeken.nierika.core.mediastore.UpdateDateTimeResult.PermissionRequired -> {
                                val sender = result.intentSender
                                if (sender != null) {
                                    pendingDateTimeUpdate = uri to newTimestampMillis
                                    val request = IntentSenderRequest.Builder(sender).build()
                                    exifPermissionLauncher.launch(request)
                                } else {
                                    Toast.makeText(context, context.getString(R.string.edit_date_time_permission_required), Toast.LENGTH_SHORT).show()
                                }
                            }
                            is com.ixeken.nierika.core.mediastore.UpdateDateTimeResult.Error -> {
                                val msg = result.message ?: context.getString(R.string.edit_date_time_error)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )

        PhotoDeleteBottomSheet(
            isVisible = isDeleteSheetVisible,
            items = listOf(currentItem),
            onDismiss = { isDeleteSheetVisible = false },
            isPermanentDelete = isItemInTrash || isInVault,
            onConfirmDelete = {
                isDeleteSheetVisible = false
                handleDeleteCurrentItem()
            }
        )

        PhotoRestoreBottomSheet(
            isVisible = isRestoreSheetVisible,
            onDismiss = { isRestoreSheetVisible = false },
            onConfirmRestore = {
                isRestoreSheetVisible = false
                handleRestoreCurrentItem()
            }
        )

        PhotoRemoveExifBottomSheet(
            isVisible = isRemoveExifSheetVisible,
            onDismiss = { isRemoveExifSheetVisible = false },
            onConfirmRemoveExif = {
                val targetUri = Uri.parse(currentItem.uri)
                coroutineScope.launch(Dispatchers.IO) {
                    val result = context.removeExifMetadata(targetUri)
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is com.ixeken.nierika.core.mediastore.ExifRemovalResult.Success -> {
                                Toast.makeText(context, context.getString(R.string.remove_exif_success), Toast.LENGTH_SHORT).show()
                            }
                            is com.ixeken.nierika.core.mediastore.ExifRemovalResult.PermissionRequired -> {
                                pendingExifRemovalUri = targetUri
                                val sender = result.intentSender
                                if (sender != null) {
                                    val request = IntentSenderRequest.Builder(sender).build()
                                    exifPermissionLauncher.launch(request)
                                } else {
                                    val reasonRes = when (result.reason) {
                                        com.ixeken.nierika.core.mediastore.ExifPermissionReason.PERMISSION_REQUIRED -> R.string.remove_exif_permission_required
                                        com.ixeken.nierika.core.mediastore.ExifPermissionReason.STORAGE_ACCESS_REQUIRED -> R.string.remove_exif_storage_access_required
                                    }
                                    Toast.makeText(context, context.getString(reasonRes), Toast.LENGTH_LONG).show()
                                    manageStorageLauncher.launch(com.ixeken.nierika.core.mediastore.StoragePermissionHelper.createManageStorageIntent(context))
                                }
                            }
                            is com.ixeken.nierika.core.mediastore.ExifRemovalResult.Error -> {
                                val errorMsg = when (result.code) {
                                    com.ixeken.nierika.core.mediastore.ExifErrorCode.READ_FAILED -> context.getString(R.string.remove_exif_error_read)
                                    com.ixeken.nierika.core.mediastore.ExifErrorCode.WRITE_FAILED -> context.getString(R.string.remove_exif_error_write)
                                    com.ixeken.nierika.core.mediastore.ExifErrorCode.CORRUPT_OR_EMPTY -> context.getString(R.string.remove_exif_error_corrupt)
                                    com.ixeken.nierika.core.mediastore.ExifErrorCode.UNKNOWN -> result.message ?: context.getString(R.string.remove_exif_error)
                                }
                                Toast.makeText(context, "${context.getString(R.string.remove_exif_error)}: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        )

        if (isOcrActive) {
            OcrTextOverlay(
                imageWidth = if (ocrImageWidth > 0) ocrImageWidth else currentItem.width,
                imageHeight = if (ocrImageHeight > 0) ocrImageHeight else currentItem.height,
                textBlocks = ocrTextBlocks,
                onClose = { isOcrActive = false },
                onCopyAll = { fullText ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(context.getString(R.string.menu_detect_text), fullText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.ocr_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    isOcrActive = false
                }
            )
        }
    }
}

/**
 * Prepara la imagen de entrada para el reconocimiento de texto OCR de ML Kit.
 * Utiliza InputImage.fromFilePath directamente para delegar la orientación y muestreo nativo,
 * o aplica un muestreo seguro en memoria para evitar saturar la memoria en fotos de 50-200MP.
 */
private suspend fun prepareInputImageForOcr(context: Context, uri: Uri): InputImage = withContext(Dispatchers.IO) {
    try {
        InputImage.fromFilePath(context, uri)
    } catch (_: Throwable) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            var inSampleSize = 1
            while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
            if (bitmap != null) {
                InputImage.fromBitmap(bitmap, 0)
            } else {
                InputImage.fromFilePath(context, uri)
            }
        } catch (_: Throwable) {
            InputImage.fromFilePath(context, uri)
        }
    }
}

/**
 * Componente individual de imagen en el Pager que delega en [ZoomableMediaContainer]
 * los gestos optimizados de zoom, arrastre y doble toque sin recomposiciones.
 */
@Composable
private fun ZoomableImagePage(
    mediaItem: MediaItem,
    isCurrentPage: Boolean = true,
    onToggleControls: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val fullImageRequest = remember(mediaItem.uri) {
        ImageRequest.Builder(context)
            .data(mediaItem.uri)
            .size(Size.ORIGINAL)
            .precision(Precision.EXACT)
            .memoryCacheKey("${mediaItem.uri}_full")
            .crossfade(true)
            .build()
    }

    ZoomableMediaContainer(
        isCurrentPage = isCurrentPage,
        onToggleControls = onToggleControls,
        onZoomStateChanged = onZoomStateChanged,
        onDismiss = onDismiss
    ) { graphicsModifier ->
        AsyncImage(
            model = fullImageRequest,
            contentDescription = mediaItem.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .then(graphicsModifier)
        )
    }
}




