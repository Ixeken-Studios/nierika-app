package com.ixeken.nierika.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.domain.repository.AlbumRepository
import com.ixeken.nierika.core.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class SearchFilter {
    ALL,
    TAGS,
    ALBUMS,
    PHOTOS,
    VIDEOS,
    DATE
}

private data class SearchFilters(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val dateMillis: Long? = null,
    val isDatePickerVisible: Boolean = false
)

private data class SearchData(
    val tags: List<String> = emptyList(),
    val media: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList()
)

@Immutable
data class SearchUiState(
    val query: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val selectedDateMillis: Long? = null,
    val availableTags: List<String> = emptyList(),
    val matchingAlbums: List<Album> = emptyList(),
    val matchingMedia: List<MediaItem> = emptyList(),
    val isDatePickerVisible: Boolean = false,
    val isSearching: Boolean = false,
    val isVaultTriggerMatch: Boolean = false,
    val isVaultConfigured: Boolean = false
)

/**
 * ViewModel reactivo para la búsqueda a pantalla completa en Nierika.
 * Soporta filtrado combinado por etiquetas (#tags), álbumes, nombres de archivo, tipos de medio y fechas.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val albumRepository: AlbumRepository,
    private val vaultRepository: com.ixeken.nierika.core.domain.repository.VaultRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    private val _isDatePickerVisible = MutableStateFlow(false)

    // Flujo de configuración de filtros
    private val _filtersFlow = combine(
        _query,
        _selectedFilter,
        _selectedDateMillis,
        _isDatePickerVisible
    ) { query, filter, dateMillis, isDatePickerVisible ->
        SearchFilters(query, filter, dateMillis, isDatePickerVisible)
    }

    // Flujo combinado de todas las etiquetas únicas del sistema (fotos + álbumes)
    private val _allUniqueTags = combine(
        mediaRepository.getAllUniqueTags(),
        albumRepository.getAllUniqueTags()
    ) { mediaTags, albumTags ->
        (mediaTags + albumTags).distinct().sorted()
    }

    // Flujo de datos del repositorio
    private val _dataFlow = combine(
        _allUniqueTags,
        mediaRepository.getAllMedia(),
        albumRepository.getAllAlbums()
    ) { tags: List<String>, allMedia: List<MediaItem>, allAlbums: List<Album> ->
        SearchData(tags, allMedia, allAlbums)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        _filtersFlow,
        _dataFlow,
        vaultRepository.isVaultConfigured
    ) { filters: SearchFilters, data: SearchData, isVaultConfigured: Boolean ->

        val trimmed = filters.query.trim()
        val tagSearchTerm = trimmed.removePrefix("#").lowercase()
        val isExplicitTagQuery = trimmed.startsWith("#") || filters.filter == SearchFilter.TAGS

        // Comprobar coincidencia con el disparador de la Bóveda Oculta
        val isVaultTriggerMatch = if (trimmed.isBlank()) {
            false
        } else if (trimmed.equals("vault", ignoreCase = true)) {
            true
        } else if (isVaultConfigured) {
            vaultRepository.verifyVaultPin(trimmed)
        } else {
            false
        }

        // 1. Filtrar Álbumes
        val filteredAlbums = if (filters.filter == SearchFilter.PHOTOS || filters.filter == SearchFilter.VIDEOS || filters.filter == SearchFilter.DATE) {
            emptyList()
        } else if (trimmed.isBlank() && filters.filter != SearchFilter.ALBUMS && filters.filter != SearchFilter.TAGS) {
            emptyList()
        } else {
            data.albums.filter { album: Album ->
                val nameMatch = album.name.contains(trimmed, ignoreCase = true)
                val descMatch = album.description.contains(trimmed, ignoreCase = true)
                val tagMatch = if (tagSearchTerm.isNotBlank()) {
                    album.tags.any { tag: String -> tag.contains(tagSearchTerm, ignoreCase = true) }
                } else {
                    filters.filter == SearchFilter.TAGS && album.tags.isNotEmpty()
                }

                if (isExplicitTagQuery) tagMatch else (nameMatch || descMatch || tagMatch)
            }
        }

        // 2. Filtrar Fotos y Vídeos
        val filteredMedia = if (filters.filter == SearchFilter.ALBUMS) {
            emptyList()
        } else if (trimmed.isBlank() && filters.dateMillis == null && filters.filter == SearchFilter.ALL) {
            emptyList()
        } else {
            data.media.filter { item: MediaItem ->
                // Filtro por tipo de contenido
                val typeMatch = when (filters.filter) {
                    SearchFilter.PHOTOS -> item.mimeType.startsWith("image/")
                    SearchFilter.VIDEOS -> item.mimeType.startsWith("video/")
                    else -> true
                }
                if (!typeMatch) return@filter false

                val mediaTimestamp = if (item.dateTakenMillis > 0) item.dateTakenMillis else item.dateModifiedMillis

                // Filtro por fecha específica seleccionada en DatePicker
                val datePickerMatch = if (filters.dateMillis != null) {
                    isSameDayUtc(mediaTimestamp, filters.dateMillis)
                } else true
                if (!datePickerMatch) return@filter false

                // Si no hay texto de búsqueda pero sí hay filtro de fecha o tipo o tags
                if (trimmed.isBlank()) {
                    return@filter if (filters.filter == SearchFilter.TAGS) item.tags.isNotEmpty() else true
                }

                // Búsqueda por tag
                val tagMatch = if (tagSearchTerm.isNotBlank()) {
                    item.tags.any { tag: String -> tag.contains(tagSearchTerm, ignoreCase = true) }
                } else {
                    filters.filter == SearchFilter.TAGS && item.tags.isNotEmpty()
                }

                if (isExplicitTagQuery) {
                    return@filter tagMatch
                }

                // Búsqueda por nombre de archivo
                val nameMatch = item.displayName.contains(trimmed, ignoreCase = true)

                // Búsqueda por texto de fecha localizada (ej. "2026", "agosto", "15")
                val itemDate = Date(mediaTimestamp)
                val formattedFull = SimpleDateFormat("dd MMMM yyyy MMMM", Locale.getDefault()).format(itemDate)
                val dateTextMatch = formattedFull.contains(trimmed, ignoreCase = true)

                nameMatch || tagMatch || dateTextMatch
            }
        }

        SearchUiState(
            query = filters.query,
            selectedFilter = filters.filter,
            selectedDateMillis = filters.dateMillis,
            availableTags = data.tags,
            matchingAlbums = filteredAlbums,
            matchingMedia = filteredMedia,
            isDatePickerVisible = filters.isDatePickerVisible,
            isSearching = trimmed.isNotBlank() || filters.dateMillis != null || filters.filter != SearchFilter.ALL,
            isVaultTriggerMatch = isVaultTriggerMatch,
            isVaultConfigured = isVaultConfigured
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun setVaultPin(pin: String) {
        viewModelScope.launch {
            vaultRepository.setVaultPin(pin)
        }
    }

    suspend fun verifyVaultPin(pin: String): Boolean {
        return vaultRepository.verifyVaultPin(pin)
    }

    fun onFilterSelect(filter: SearchFilter) {
        if (filter == SearchFilter.DATE) {
            _isDatePickerVisible.value = true
        } else {
            _selectedFilter.value = filter
        }
    }

    fun onTagSelect(tag: String) {
        _query.value = "#$tag"
        _selectedFilter.value = SearchFilter.TAGS
    }

    fun onDateSelected(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
        _isDatePickerVisible.value = false
        if (dateMillis != null) {
            _selectedFilter.value = SearchFilter.DATE
        }
    }

    fun onDismissDatePicker() {
        _isDatePickerVisible.value = false
    }

    fun onClearDateFilter() {
        _selectedDateMillis.value = null
        if (_selectedFilter.value == SearchFilter.DATE) {
            _selectedFilter.value = SearchFilter.ALL
        }
    }

    fun onClearQuery() {
        _query.value = ""
    }


    private fun isSameDayUtc(timestampMillis: Long, utcDateMillis: Long): Boolean {
        val calItem = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        val calPicker = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDateMillis }

        return calItem.get(Calendar.YEAR) == calPicker.get(Calendar.YEAR) &&
                calItem.get(Calendar.MONTH) == calPicker.get(Calendar.MONTH) &&
                calItem.get(Calendar.DAY_OF_MONTH) == calPicker.get(Calendar.DAY_OF_MONTH)
    }
}
