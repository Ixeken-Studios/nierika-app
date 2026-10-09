package com.ixeken.nierika.feature.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nierika.core.domain.repository.MediaRepository
import com.ixeken.nierika.core.domain.repository.UserPreferences
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para operaciones de soporte y preferencias del Visor de Medios.
 * Centraliza la lectura reactiva de preferencias de usuario como carrusel de miniaturas, silencio de video y confirmación de eliminación.
 */
@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    fun setVideoMuted(muted: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setVideoMuted(muted)
        }
    }

    fun getTagsForMedia(mediaId: Long): Flow<List<String>> {
        return mediaRepository.getTagsForMedia(mediaId)
    }

    fun addTag(mediaId: Long, tag: String) {
        viewModelScope.launch {
            mediaRepository.addTagToMedia(mediaId, tag)
        }
    }

    fun removeTag(mediaId: Long, tag: String) {
        viewModelScope.launch {
            mediaRepository.removeTagFromMedia(mediaId, tag)
        }
    }
}
