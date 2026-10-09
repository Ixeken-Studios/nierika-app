package com.ixeken.nierika.feature.collections

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
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.repository.AlbumRepository
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.domain.repository.UserPreferences
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel Hilt para la pestaña Collections.
 * Administra el estado de álbumes creados por el usuario, ubicación de botones y visibilidad del BottomSheet "Create new".
 */
@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    val mediaPagingData: Flow<PagingData<MediaItem>> = mediaRepository.getMediaPaged().cachedIn(viewModelScope)

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    private val _refreshTrigger = MutableStateFlow(0L)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val trashMedia: StateFlow<List<MediaItem>> = _refreshTrigger.flatMapLatest {
        mediaRepository.getTrashMedia()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _pendingTrashIds = MutableStateFlow<Set<Long>>(emptySet())
    val pendingTrashIds: StateFlow<Set<Long>> = _pendingTrashIds.asStateFlow()

    fun refreshSilent() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    private var exportJob: Job? = null
    private val _albumExportState = MutableStateFlow(AlbumExportState())
    val albumExportState: StateFlow<AlbumExportState> = _albumExportState.asStateFlow()

    private var importJob: Job? = null
    private val _albumImportState = MutableStateFlow(AlbumImportState())
    val albumImportState: StateFlow<AlbumImportState> = _albumImportState.asStateFlow()

    private val _zipPreviewInfo = MutableStateFlow<ZipPreviewInfo?>(null)
    val zipPreviewInfo: StateFlow<ZipPreviewInfo?> = _zipPreviewInfo.asStateFlow()

    init {
        loadAlbums()
        observeUserPreferences()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            albumRepository.getAllAlbums().collectLatest { albumList ->
                _uiState.update { state ->
                    state.copy(
                        albums = albumList,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collectLatest { prefs ->
                _uiState.update { state ->
                    state.copy(albumButtonsLocation = prefs.albumButtonsLocation)
                }
            }
        }
    }

    fun onCreateAlbumWithPhotos(
        name: String,
        description: String = "",
        coverUri: String? = null,
        tags: List<String> = emptyList(),
        photos: List<MediaItem> = emptyList(),
        onSuccess: (Long) -> Unit = {}
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
            } else if (!coverUri.isNullOrEmpty()) {
                albumRepository.updateAlbumCover(albumId, coverUri)
            }
            onSuccess(albumId)
        }
    }

    fun onAddMediaBatchToAlbum(
        album: com.ixeken.nierika.core.domain.model.Album,
        items: List<MediaItem>,
        onResult: ((com.ixeken.nierika.core.domain.model.AddToAlbumResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val oldestItem = items.minByOrNull { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
            val firstCover = album.coverUri ?: oldestItem?.uri
            val result = albumRepository.addMediaBatchToAlbum(
                albumId = album.id,
                mediaIds = items.map { it.id },
                firstCoverUri = firstCover
            )
            onResult?.invoke(result)
        }
    }

    fun getAlbumById(albumId: Long): Flow<com.ixeken.nierika.core.domain.model.Album?> {
        return albumRepository.getAlbumById(albumId)
    }

    fun getMediaForAlbum(albumId: Long): Flow<List<MediaItem>> {
        return albumRepository.getMediaForAlbum(albumId)
    }

    fun onUpdateAlbum(
        albumId: Long,
        name: String,
        description: String = "",
        coverUri: String? = null,
        tags: List<String> = emptyList(),
        photos: List<MediaItem> = emptyList(),
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val oldestPhoto = photos.minByOrNull { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
            val resolvedCover = coverUri ?: oldestPhoto?.uri
            albumRepository.updateAlbum(albumId, name, description, resolvedCover, tags)
            albumRepository.syncAlbumMedia(albumId, photos.map { it.id }, resolvedCover)
            _uiState.update { state ->
                val updatedList = state.albums.map { existing ->
                    if (existing.id == albumId) {
                        existing.copy(
                            name = name,
                            description = description,
                            coverUri = resolvedCover,
                            itemCount = photos.size,
                            tags = tags
                        )
                    } else existing
                }
                state.copy(albums = updatedList)
            }
            onSuccess()
        }
    }

    fun onDeleteAlbum(albumId: Long) {
        viewModelScope.launch {
            albumRepository.deleteAlbum(albumId)
        }
    }

    fun getMediaForCategory(category: com.ixeken.nierika.core.domain.model.QuickFilter): Flow<List<MediaItem>> {
        return mediaRepository.getMediaForCategory(category)
    }

    fun onEmptyTrash() {
        viewModelScope.launch {
            mediaRepository.emptyTrash()
            refreshSilent()
        }
    }

    fun onToggleFavorite(mediaId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            mediaRepository.toggleFavorite(mediaId, isFavorite)
            refreshSilent()
        }
    }

    fun onRestoreBatch(items: List<MediaItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                mediaRepository.restoreFromTrash(item.id)
            }
            refreshSilent()
        }
    }

    fun deletePermanentlyFromDb(mediaIds: List<Long>) {
        viewModelScope.launch {
            mediaIds.forEach { id ->
                mediaRepository.deletePermanently(id)
            }
            refreshSilent()
        }
    }

    fun onRemoveMediaBatchFromAlbum(album: com.ixeken.nierika.core.domain.model.Album, items: List<MediaItem>) {
        viewModelScope.launch {
            items.forEach { item ->
                albumRepository.removeMediaFromAlbum(album.id, item.id)
            }
            refreshSilent()
        }
    }

    fun onShowDatesInGridChange(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowDatesInCollectionsGrid(show)
        }
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

    fun deletePermanently(items: List<MediaItem>) {
        val ids = items.map { it.id }.toSet()
        _pendingTrashIds.update { it + ids }
        viewModelScope.launch {
            mediaRepository.deletePermanentlyBatch(
                mediaIds = items.map { it.id },
                uris = items.map { it.uri }
            )
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

    /**
     * Exporta todas las fotos y vídeos de un álbum comprimiéndolos en un archivo .ZIP estructurado
     * con streaming directo al [destinationUri] seleccionado por el usuario mediante Storage Access Framework.
     */
    fun exportAlbumToZip(context: Context, album: Album, destinationUri: Uri) {
        exportJob?.cancel()
        exportJob = viewModelScope.launch(Dispatchers.IO) {
            _albumExportState.value = AlbumExportState(
                isExporting = true,
                albumName = album.name,
                currentItem = 0,
                totalItems = 0,
                progress = 0f,
                isCompleted = false,
                error = null
            )

            try {
                val mediaItems = albumRepository.getMediaForAlbum(album.id).first()
                if (mediaItems.isEmpty()) {
                    _albumExportState.value = _albumExportState.value.copy(
                        isExporting = false,
                        error = "Empty album"
                    )
                    return@launch
                }

                val exportItems = mediaItems.map { item ->
                    ExportableMedia(
                        id = item.id,
                        displayName = item.displayName,
                        mimeType = item.mimeType,
                        uri = Uri.parse(item.uri)
                    )
                }

                AlbumZipArchiveManager.exportToZip(
                    context = context,
                    destinationUri = destinationUri,
                    folderName = album.name,
                    items = exportItems
                ) { current, total, prog ->
                    _albumExportState.value = _albumExportState.value.copy(
                        currentItem = current,
                        totalItems = total,
                        progress = prog
                    )
                }

                _albumExportState.value = _albumExportState.value.copy(
                    isExporting = false,
                    isCompleted = true,
                    progress = 1.0f
                )
            } catch (e: CancellationException) {
                _albumExportState.value = AlbumExportState()
                try {
                    context.contentResolver.delete(destinationUri, null, null)
                } catch (_: Exception) {}
            } catch (e: Exception) {
                _albumExportState.value = _albumExportState.value.copy(
                    isExporting = false,
                    error = e.localizedMessage ?: "Export error"
                )
            }
        }
    }

    /**
     * Cancela la exportación en curso y elimina el archivo parcial.
     */
    fun cancelAlbumExport() {
        exportJob?.cancel()
        _albumExportState.value = AlbumExportState()
    }

    /**
     * Restablece el estado de exportación una vez consumido el resultado.
     */
    fun resetAlbumExportState() {
        _albumExportState.value = AlbumExportState()
    }

    /**
     * Establece o limpia la información de previsualización del archivo .ZIP.
     */
    fun setZipPreviewInfo(info: ZipPreviewInfo?) {
        _zipPreviewInfo.value = info
    }

    /**
     * Escanea rápidamente el archivo .ZIP en segundo plano para inferir el nombre del álbum
     * y contar el total de fotos y vídeos válidos.
     */
    fun inspectZipArchive(context: Context, zipUri: Uri, onResult: (ZipPreviewInfo?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val previewInfo = AlbumZipArchiveManager.inspectZipArchive(context, zipUri)
            withContext(Dispatchers.Main) {
                _zipPreviewInfo.value = previewInfo
                onResult(previewInfo)
            }
        }
    }

    /**
     * Importa fotos y vídeos desde un archivo .ZIP, escribiéndolos en MediaStore (Pictures/Nierika/[albumName])
     * y creando el álbum correspondiente en la base de datos Room.
     */
    fun importAlbumFromZip(
        context: Context,
        zipUri: Uri,
        albumName: String,
        onSuccess: (Long) -> Unit = {}
    ) {
        importJob?.cancel()
        importJob = viewModelScope.launch(Dispatchers.IO) {
            _albumImportState.value = AlbumImportState(
                isImporting = true,
                albumName = albumName,
                currentItem = 0,
                totalItems = 0,
                progress = 0f,
                isCompleted = false,
                error = null
            )

            try {
                val validCount = _zipPreviewInfo.value?.mediaCount ?: 0
                val (insertedMediaIds, firstCoverUri) = AlbumZipArchiveManager.importZipToMediaStore(
                    context = context,
                    zipUri = zipUri,
                    albumName = albumName,
                    validCount = validCount
                ) { current, total, prog ->
                    _albumImportState.value = _albumImportState.value.copy(
                        currentItem = current,
                        totalItems = total,
                        progress = prog
                    )
                }

                if (insertedMediaIds.isEmpty()) {
                    _albumImportState.value = _albumImportState.value.copy(
                        isImporting = false,
                        error = "No media files were imported"
                    )
                    return@launch
                }

                // Crear el álbum en Room
                val createdAlbumId = albumRepository.createAlbum(
                    name = albumName,
                    description = "",
                    coverUri = firstCoverUri,
                    tags = emptyList()
                )

                // Asociar las fotos/vídeos al nuevo álbum
                albumRepository.addMediaBatchToAlbum(
                    albumId = createdAlbumId,
                    mediaIds = insertedMediaIds,
                    firstCoverUri = firstCoverUri
                )

                _albumImportState.value = _albumImportState.value.copy(
                    isImporting = false,
                    isCompleted = true,
                    importedAlbumId = createdAlbumId,
                    progress = 1.0f
                )

                withContext(Dispatchers.Main) {
                    onSuccess(createdAlbumId)
                }
            } catch (e: CancellationException) {
                _albumImportState.value = AlbumImportState()
            } catch (e: Exception) {
                _albumImportState.value = _albumImportState.value.copy(
                    isImporting = false,
                    error = e.localizedMessage ?: "Import error"
                )
            }
        }
    }

    /**
     * Cancela la importación en curso.
     */
    fun cancelAlbumImport() {
        importJob?.cancel()
        _albumImportState.value = AlbumImportState()
    }

    /**
     * Restablece el estado de importación una vez consumido el resultado.
     */
    fun resetAlbumImportState() {
        _albumImportState.value = AlbumImportState()
    }
}
