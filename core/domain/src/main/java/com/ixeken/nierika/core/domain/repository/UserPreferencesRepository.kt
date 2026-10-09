package com.ixeken.nierika.core.domain.repository

import androidx.compose.runtime.Immutable
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation
import com.ixeken.nierika.core.domain.model.LayoutMode
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Preferencias de usuario inmutables.
 */
@Immutable
data class UserPreferences(
    val layoutMode: LayoutMode = LayoutMode.DAY,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val quickFilter: QuickFilter = QuickFilter.ALL,
    val showDatesInGrid: Boolean = true,
    val showDatesInTimelineGrid: Boolean = true,
    val showDatesInCollectionsGrid: Boolean = true,
    val isDarkTheme: Boolean = true,
    val selectedLanguage: String = "",
    val useDynamicColor: Boolean = true,
    val selectedThemeId: String = "default",
    val fontScaleMultiplier: Float = 1.0f,
    val selectedFontFamily: String = "Google Sans Flex",
    val albumButtonsLocation: AlbumButtonsLocation = AlbumButtonsLocation.BOTTOM,
    val floatingTabBarEnabled: Boolean = true,
    val isDeleteConfirmationEnabled: Boolean = true,
    val isAppLockEnabled: Boolean = false,
    val isThumbnailCarouselEnabled: Boolean = true,
    val isVideoMuted: Boolean = false,
    val showAppLogoInTopBar: Boolean = true,
    val checkUpdateOnStart: Boolean = false,
    val appLauncherAlias: com.ixeken.nierika.core.domain.model.AppLauncherAlias = com.ixeken.nierika.core.domain.model.AppLauncherAlias.DEFAULT,
    val splitHeaderMediaCount: Boolean = false,
    val hasCompletedWalkthrough: Boolean = false
)

/**
 * Contrato de repositorio para la persistencia de preferencias de usuario.
 */
interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun setLayoutMode(mode: LayoutMode)
    suspend fun setSortOrder(order: SortOrder)
    suspend fun setQuickFilter(filter: QuickFilter)
    suspend fun setShowDatesInGrid(show: Boolean)
    suspend fun setShowDatesInTimelineGrid(show: Boolean)
    suspend fun setShowDatesInCollectionsGrid(show: Boolean)
    suspend fun setIsDarkTheme(enabled: Boolean)
    suspend fun setSelectedLanguage(languageCode: String)
    suspend fun setUseDynamicColor(enabled: Boolean)
    suspend fun setSelectedThemeId(themeId: String)
    suspend fun setTheme(themeId: String, useDynamicColor: Boolean)
    suspend fun setFontScaleMultiplier(scale: Float)
    suspend fun setSelectedFontFamily(fontFamily: String)
    suspend fun setAlbumButtonsLocation(location: AlbumButtonsLocation)
    suspend fun setFloatingTabBarEnabled(enabled: Boolean)
    suspend fun setDeleteConfirmationEnabled(enabled: Boolean)
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setThumbnailCarouselEnabled(enabled: Boolean)
    suspend fun setVideoMuted(muted: Boolean)
    suspend fun setShowAppLogoInTopBar(show: Boolean)
    suspend fun setCheckUpdateOnStart(enabled: Boolean)
    suspend fun setAppLauncherAlias(alias: com.ixeken.nierika.core.domain.model.AppLauncherAlias)
    suspend fun setSplitHeaderMediaCount(enabled: Boolean)
    suspend fun setHasCompletedWalkthrough(completed: Boolean)
}
