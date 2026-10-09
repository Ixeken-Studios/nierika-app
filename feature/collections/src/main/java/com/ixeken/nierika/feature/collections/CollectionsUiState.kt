package com.ixeken.nierika.feature.collections

import androidx.compose.runtime.Immutable
import com.ixeken.nierika.core.domain.model.Album
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation

/**
 * Estado UI inmutable para la pantalla de Colecciones (Collections).
 */
@Immutable
data class CollectionsUiState(
    val albums: List<Album> = emptyList(),
    val favoritesCount: Int = 0,
    val screenshotsCount: Int = 0,
    val videosCount: Int = 0,
    val cameraCount: Int = 0,
    val trashCount: Int = 0,
    val albumButtonsLocation: AlbumButtonsLocation = AlbumButtonsLocation.BOTTOM,
    val isLoading: Boolean = false
)
