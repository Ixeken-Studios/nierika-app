package com.ixeken.nierika.core.data.repository

import com.ixeken.nierika.core.database.datastore.UserPreferencesDataStore
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation
import com.ixeken.nierika.core.domain.model.LayoutMode
import com.ixeken.nierika.core.domain.model.QuickFilter
import com.ixeken.nierika.core.domain.model.SortOrder
import com.ixeken.nierika.core.domain.repository.UserPreferences
import com.ixeken.nierika.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de preferencias respaldada por DataStore.
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.preferencesFlow.map { map ->
        UserPreferences(
            layoutMode = runCatching { LayoutMode.valueOf(map["layout_mode"] as? String ?: "DAY") }.getOrDefault(LayoutMode.DAY),
            sortOrder = runCatching { SortOrder.valueOf(map["sort_order"] as? String ?: "DESCENDING") }.getOrDefault(SortOrder.DESCENDING),
            quickFilter = runCatching { QuickFilter.valueOf(map["quick_filter"] as? String ?: "ALL") }.getOrDefault(QuickFilter.ALL),
            showDatesInGrid = map["show_dates_in_grid"] as? Boolean ?: true,
            showDatesInTimelineGrid = map["show_dates_in_timeline_grid"] as? Boolean ?: true,
            showDatesInCollectionsGrid = map["show_dates_in_collections_grid"] as? Boolean ?: true,
            isDarkTheme = map["is_dark_theme"] as? Boolean ?: true,
            selectedLanguage = map["selected_language"] as? String ?: "",
            useDynamicColor = map["use_dynamic_color"] as? Boolean ?: true,
            selectedThemeId = map["selected_theme_id"] as? String ?: "default",
            fontScaleMultiplier = map["font_scale_multiplier"] as? Float ?: 1.0f,
            selectedFontFamily = map["selected_font_family"] as? String ?: "Google Sans Flex",
            albumButtonsLocation = runCatching { AlbumButtonsLocation.valueOf(map["album_buttons_location"] as? String ?: "BOTTOM") }.getOrDefault(AlbumButtonsLocation.BOTTOM),
            floatingTabBarEnabled = map["floating_tab_bar_enabled"] as? Boolean ?: true,
            isDeleteConfirmationEnabled = map["is_delete_confirmation_enabled"] as? Boolean ?: true,
            isAppLockEnabled = map["is_app_lock_enabled"] as? Boolean ?: false,
            isThumbnailCarouselEnabled = map["is_thumbnail_carousel_enabled"] as? Boolean ?: true,
            isVideoMuted = map["is_video_muted"] as? Boolean ?: false,
            showAppLogoInTopBar = map["show_app_logo_in_top_bar"] as? Boolean ?: true,
            checkUpdateOnStart = map["check_update_on_start"] as? Boolean ?: false,
            appLauncherAlias = com.ixeken.nierika.core.domain.model.AppLauncherAlias.fromId(map["app_launcher_alias"] as? String ?: "default"),
            splitHeaderMediaCount = map["split_header_media_count"] as? Boolean ?: false,
            hasCompletedWalkthrough = map["has_completed_walkthrough"] as? Boolean ?: false
        )
    }

    override suspend fun setLayoutMode(mode: LayoutMode) {
        dataStore.updateLayoutMode(mode.name)
    }

    override suspend fun setSortOrder(order: SortOrder) {
        dataStore.updateSortOrder(order.name)
    }

    override suspend fun setQuickFilter(filter: QuickFilter) {
        dataStore.updateQuickFilter(filter.name)
    }

    override suspend fun setShowDatesInGrid(show: Boolean) {
        dataStore.updateShowDatesInGrid(show)
    }

    override suspend fun setShowDatesInTimelineGrid(show: Boolean) {
        dataStore.updateShowDatesInTimelineGrid(show)
    }

    override suspend fun setShowDatesInCollectionsGrid(show: Boolean) {
        dataStore.updateShowDatesInCollectionsGrid(show)
    }

    override suspend fun setIsDarkTheme(enabled: Boolean) {
        dataStore.updateIsDarkTheme(enabled)
    }

    override suspend fun setSelectedLanguage(languageCode: String) {
        dataStore.updateSelectedLanguage(languageCode)
    }

    override suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.updateUseDynamicColor(enabled)
    }

    override suspend fun setSelectedThemeId(themeId: String) {
        dataStore.updateSelectedThemeId(themeId)
    }

    override suspend fun setTheme(themeId: String, useDynamicColor: Boolean) {
        dataStore.updateTheme(themeId = themeId, useDynamicColor = useDynamicColor)
    }

    override suspend fun setFontScaleMultiplier(scale: Float) {
        dataStore.updateFontScaleMultiplier(scale)
    }

    override suspend fun setSelectedFontFamily(fontFamily: String) {
        dataStore.updateSelectedFontFamily(fontFamily)
    }

    override suspend fun setAlbumButtonsLocation(location: AlbumButtonsLocation) {
        dataStore.updateAlbumButtonsLocation(location.name)
    }

    override suspend fun setFloatingTabBarEnabled(enabled: Boolean) {
        dataStore.updateFloatingTabBarEnabled(enabled)
    }

    override suspend fun setDeleteConfirmationEnabled(enabled: Boolean) {
        dataStore.updateDeleteConfirmationEnabled(enabled)
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.updateAppLockEnabled(enabled)
    }

    override suspend fun setThumbnailCarouselEnabled(enabled: Boolean) {
        dataStore.updateThumbnailCarouselEnabled(enabled)
    }

    override suspend fun setVideoMuted(muted: Boolean) {
        dataStore.updateVideoMuted(muted)
    }

    override suspend fun setShowAppLogoInTopBar(show: Boolean) {
        dataStore.updateShowAppLogoInTopBar(show)
    }

    override suspend fun setCheckUpdateOnStart(enabled: Boolean) {
        dataStore.updateCheckUpdateOnStart(enabled)
    }

    override suspend fun setAppLauncherAlias(alias: com.ixeken.nierika.core.domain.model.AppLauncherAlias) {
        dataStore.updateAppLauncherAlias(alias.id)
    }

    override suspend fun setSplitHeaderMediaCount(enabled: Boolean) {
        dataStore.updateSplitHeaderMediaCount(enabled)
    }

    override suspend fun setHasCompletedWalkthrough(completed: Boolean) {
        dataStore.updateHasCompletedWalkthrough(completed)
    }
}
