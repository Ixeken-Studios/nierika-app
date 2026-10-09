package com.ixeken.nierika.feature.settings

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation
import com.ixeken.nierika.core.domain.repository.UserPreferences
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de administrar el estado de preferencias en SettingsScreen y AppearanceScreen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UserPreferences()
    )

    fun onToggleDeleteConfirmation(enabled: Boolean) {
        _uiState.update { it.copy(isDeleteConfirmationEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setDeleteConfirmationEnabled(enabled)
        }
    }

    fun onToggleAppLock(enabled: Boolean) {
        _uiState.update { it.copy(isAppLockEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setAppLockEnabled(enabled)
        }
    }

    fun onShowPermissionsBottomSheet(show: Boolean) {
        _uiState.update { it.copy(isPermissionsBottomSheetVisible = show) }
    }

    fun onShowPrivacyInfoBottomSheet(show: Boolean) {
        _uiState.update { it.copy(isPrivacyInfoBottomSheetVisible = show) }
    }

    fun updateAlbumButtonsLocation(location: AlbumButtonsLocation) {
        viewModelScope.launch {
            userPreferencesRepository.setAlbumButtonsLocation(location)
        }
    }

    fun updateFloatingTabBarEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setFloatingTabBarEnabled(enabled)
        }
    }

    fun updateFontScaleMultiplier(scale: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setFontScaleMultiplier(scale)
        }
    }

    fun updateSelectedFontFamily(fontFamily: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedFontFamily(fontFamily)
        }
    }

    fun updateSelectedThemeId(themeId: String) {
        viewModelScope.launch {
            val isDynamic = themeId.equals("dynamic", ignoreCase = true)
            userPreferencesRepository.setTheme(
                themeId = if (isDynamic) "dynamic" else themeId,
                useDynamicColor = isDynamic
            )
        }
    }

    fun updateIsDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setIsDarkTheme(enabled)
        }
    }

    fun updateSelectedLanguage(context: Context, languageCode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedLanguage(languageCode)
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (localeManager != null) {
                localeManager.applicationLocales = if (languageCode.isBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(languageCode)
                }
            }
        }
    }

    fun updateUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(
                themeId = if (enabled) "dynamic" else "purple",
                useDynamicColor = enabled
            )
        }
    }

    fun onToggleThumbnailCarousel(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setThumbnailCarouselEnabled(enabled)
        }
    }

    fun onToggleShowAppLogoInTopBar(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowAppLogoInTopBar(show)
        }
    }

    fun onToggleCheckUpdateOnStart(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCheckUpdateOnStart(enabled)
        }
    }

    fun updateSplitHeaderMediaCount(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSplitHeaderMediaCount(enabled)
        }
    }

    fun onShowAppIconBottomSheet(show: Boolean) {
        _uiState.update { it.copy(isAppIconBottomSheetVisible = show) }
    }

    fun onShowWalkthrough(show: Boolean) {
        _uiState.update { it.copy(isWalkthroughVisible = show) }
    }

    fun onSelectLauncherAlias(alias: com.ixeken.nierika.core.domain.model.AppLauncherAlias, context: android.content.Context) {
        viewModelScope.launch {
            userPreferencesRepository.setAppLauncherAlias(alias)
            com.ixeken.nierika.feature.settings.util.LauncherAliasHelper.applyLauncherAlias(context, alias)
        }
    }
}
