package com.ixeken.nierika.feature.timeline

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ixeken.nierika.core.domain.model.AddToAlbumResult
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.LayoutMode
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.AlbumRepository
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.domain.repository.UserPreferences
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import com.ixeken.nierika.core.domain.usecase.GetMediaTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import javax.inject.Inject

/**
 * Conteo reactivo y exacto de elementos multimedia clasificados por tipo.
 */
@Immutable
data class TimelineMediaCounts(
    val photosCount: Int = 0,
    val videosCount: Int = 0
) {
    val totalCount: Int get() = photosCount + videosCount
}

/**
 * Estado UI inmutable para la pantalla principal de la galería.
 */
@Immutable
data class TimelineUiState(
    val permissionGranted: Boolean = false,
    val selectedFilter: QuickFilter = QuickFilter.ALL,
    val selectedSortOrder: SortOrder = SortOrder.DESCENDING,
    val selectedLayoutMode: LayoutMode = LayoutMode.DAY,
    val showDatesInGrid: Boolean = true,
    val floatingTabBarEnabled: Boolean = true,
    val isSideSheetVisible: Boolean = false
)

private data class TimelineRefreshParams(
    val filter: QuickFilter,
    val sortOrder: SortOrder,
    val permissionGranted: Boolean,
    val refreshId: Long
)

/**
 * ViewModel principal de la pestaña Fotos (Library).
 * Conecta los casos de uso del dominio y MediaStore con la interfaz gráfica de Compose.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getMediaTimelineUseCase: GetMediaTimelineUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mediaRepository: MediaRepository,
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    val albums: StateFlow<List<Album>> = albumRepository.getAllAlbums().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserPreferences()
    )

    private val _refreshTrigger = MutableStateFlow(0L)
    private val _pendingTrashIds = MutableStateFlow<Set<Long>>(emptySet())
    val pendingTrashIds: StateFlow<Set<Long>> = _pendingTrashIds.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaPagingData: Flow<PagingData<MediaItem>> = combine(
        _uiState,
        userPreferencesRepository.userPreferences,
        _refreshTrigger
    ) { state, prefs, refreshId ->
        TimelineRefreshParams(state.selectedFilter, state.selectedSortOrder, state.permissionGranted, refreshId)
    }.flatMapLatest { params ->
        if (params.permissionGranted) {
            getMediaTimelineUseCase(quickFilter = params.filter, sortOrder = params.sortOrder)
        } else {
            MutableStateFlow(PagingData.empty())
        }
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val mediaCounts: StateFlow<TimelineMediaCounts> = combine(
        _uiState.map { it.selectedFilter }.distinctUntilChanged(),
        _uiState.map { it.permissionGranted }.distinctUntilChanged(),
        _refreshTrigger
    ) { filter, permissionGranted, _ ->
        Pair(filter, permissionGranted)
    }.flatMapLatest { (filter, permissionGranted) ->
        if (permissionGranted) {
            mediaRepository.getMediaForCategory(filter).map { items ->
                var photos = 0
                var videos = 0
                for (item in items) {
                    if (item.isVideo) {
                        videos++
                    } else {
                        photos++
                    }
                }
                TimelineMediaCounts(photosCount = photos, videosCount = videos)
            }
        } else {
            flowOf(TimelineMediaCounts())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TimelineMediaCounts()
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        selectedLayoutMode = prefs.layoutMode,
                        selectedSortOrder = prefs.sortOrder,
                        selectedFilter = if (prefs.quickFilter == QuickFilter.TRASH) QuickFilter.ALL else prefs.quickFilter,
                        showDatesInGrid = prefs.showDatesInTimelineGrid,
                        floatingTabBarEnabled = prefs.floatingTabBarEnabled
                    )
                }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permissionGranted = granted) }
    }

    fun onFilterSelected(filter: QuickFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        viewModelScope.launch {
            userPreferencesRepository.setQuickFilter(filter)
        }
    }

    fun onSortOrderSelected(order: SortOrder) {
        _uiState.update { it.copy(selectedSortOrder = order) }
        viewModelScope.launch {
            userPreferencesRepository.setSortOrder(order)
        }
    }

    fun onLayoutModeSelected(mode: LayoutMode) {
        _uiState.update { it.copy(selectedLayoutMode = mode) }
        viewModelScope.launch {
            userPreferencesRepository.setLayoutMode(mode)
        }
    }

    fun onToggleShowDatesInGrid(show: Boolean) {
        _uiState.update { it.copy(showDatesInGrid = show) }
        viewModelScope.launch {
            userPreferencesRepository.setShowDatesInTimelineGrid(show)
        }
    }

    fun onToggleSideSheet(visible: Boolean) {
        _uiState.update { it.copy(isSideSheetVisible = visible) }
    }

    fun refreshSilent() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun moveToTrashBatch(items: List<MediaItem>) {
        val ids = items.map { it.id }.toSet()
        _pendingTrashIds.update { it + ids }
        viewModelScope.launch {
            mediaRepository.moveToTrashBatch(
                mediaIds = items.map { it.id },
                uris = items.map { it.uri }
            )
            refreshSilent()
        }
    }

    fun restoreBatch(items: List<MediaItem>) {
        val ids = items.map { it.id }.toSet()
        _pendingTrashIds.update { it + ids }
        viewModelScope.launch {
            mediaRepository.restoreBatch(items.map { it.id })
            refreshSilent()
        }
    }

    fun deletePermanentlyFromDb(mediaIds: List<Long>) {
        val ids = mediaIds.toSet()
        _pendingTrashIds.update { it + ids }
        viewModelScope.launch {
            mediaRepository.deletePermanentlyBatch(mediaIds)
            refreshSilent()
        }
    }

    fun clearPendingTrashIds() {
        if (_pendingTrashIds.value.isNotEmpty()) {
            _pendingTrashIds.value = emptySet()
        }
    }

    fun createTrashIntentSender(context: Context, items: List<MediaItem>, isTrash: Boolean): IntentSender? {
        val contentUris = items.mapNotNull {
            try {
                val parsed = Uri.parse(it.uri)
                if (parsed.scheme == "content") parsed else null
            } catch (_: Exception) {
                null
            }
        }
        if (contentUris.isEmpty()) return null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createTrashRequest(context.contentResolver, contentUris, isTrash)
                pendingIntent.intentSender
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun createDeleteIntentSender(context: Context, items: List<MediaItem>): IntentSender? {
        val contentUris = items.mapNotNull {
            try {
                val parsed = Uri.parse(it.uri)
                if (parsed.scheme == "content") parsed else null
            } catch (_: Exception) {
                null
            }
        }
        if (contentUris.isEmpty()) return null

        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, contentUris)
                    pendingIntent.intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    try {
                        context.contentResolver.delete(contentUris.first(), null, null)
                        contentUris.drop(1).forEach { uri ->
                            try {
                                context.contentResolver.delete(uri, null, null)
                            } catch (_: Exception) {}
                        }
                        null
                    } catch (e: RecoverableSecurityException) {
                        e.userAction.actionIntent.intentSender
                    } catch (_: Exception) {
                        null
                    }
                }
                else -> {
                    contentUris.forEach { uri ->
                        try {
                            context.contentResolver.delete(uri, null, null)
                        } catch (_: Exception) {}
                    }
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun toggleFavorite(mediaId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            mediaRepository.toggleFavorite(mediaId, isFavorite)
            refreshSilent()
        }
    }

    fun toggleFavoriteBatch(items: List<MediaItem>, isFavorite: Boolean) {
        viewModelScope.launch {
            items.forEach { item ->
                mediaRepository.toggleFavorite(item.id, isFavorite)
            }
            refreshSilent()
        }
    }

    fun duplicateMedia(mediaItem: MediaItem, onComplete: ((String?) -> Unit)? = null) {
        viewModelScope.launch {
            val newUri = mediaRepository.duplicateMedia(mediaItem)
            if (newUri != null) {
                refreshSilent()
            }
            onComplete?.invoke(newUri)
        }
    }

    fun addItemsToAlbum(album: Album, items: List<MediaItem>, onComplete: (AddToAlbumResult) -> Unit = {}) {
        viewModelScope.launch {
            val firstCover = album.coverUri ?: items.firstOrNull()?.uri
            val result = albumRepository.addMediaBatchToAlbum(
                albumId = album.id,
                mediaIds = items.map { it.id },
                firstCoverUri = firstCover
            )
            onComplete(result)
        }
    }

    fun createAlbumWithPhotos(
        name: String,
        description: String = "",
        coverUri: String? = null,
        tags: List<String> = emptyList(),
        photos: List<MediaItem>,
        onComplete: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val oldestPhoto = photos.minByOrNull { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
            val resolvedCover = coverUri ?: oldestPhoto?.uri
            val albumId = albumRepository.createAlbum(name, description, resolvedCover, tags)
            if (photos.isNotEmpty()) {
                albumRepository.addMediaBatchToAlbum(
                    albumId = albumId,
                    mediaIds = photos.map { it.id },
                    firstCoverUri = resolvedCover
                )
            }
            onComplete(albumId)
        }
    }

    fun completeWalkthrough() {
        viewModelScope.launch {
            userPreferencesRepository.setHasCompletedWalkthrough(true)
        }
    }
}

