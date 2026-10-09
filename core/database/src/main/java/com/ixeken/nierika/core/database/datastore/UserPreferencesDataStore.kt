package com.ixeken.nierika.core.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nierika_user_preferences")

/**
 * Gestor de preferencias persistentes del usuario mediante Jetpack DataStore.
 */
class UserPreferencesDataStore(private val context: Context) {

    private object PreferenceKeys {
        val LAYOUT_MODE = stringPreferencesKey("layout_mode")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val QUICK_FILTER = stringPreferencesKey("quick_filter")
        val SHOW_DATES_IN_GRID = booleanPreferencesKey("show_dates_in_grid")
        val SHOW_DATES_IN_TIMELINE_GRID = booleanPreferencesKey("show_dates_in_timeline_grid")
        val SHOW_DATES_IN_COLLECTIONS_GRID = booleanPreferencesKey("show_dates_in_collections_grid")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val FONT_SCALE_MULTIPLIER = floatPreferencesKey("font_scale_multiplier")
        val SELECTED_FONT_FAMILY = stringPreferencesKey("selected_font_family")
        val ALBUM_BUTTONS_LOCATION = stringPreferencesKey("album_buttons_location")
        val FLOATING_TAB_BAR_ENABLED = booleanPreferencesKey("floating_tab_bar_enabled")
        val IS_DELETE_CONFIRMATION_ENABLED = booleanPreferencesKey("is_delete_confirmation_enabled")
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val IS_THUMBNAIL_CAROUSEL_ENABLED = booleanPreferencesKey("is_thumbnail_carousel_enabled")
        val IS_VIDEO_MUTED = booleanPreferencesKey("is_video_muted")
        val SHOW_APP_LOGO_IN_TOP_BAR = booleanPreferencesKey("show_app_logo_in_top_bar")
        val CHECK_UPDATE_ON_START = booleanPreferencesKey("check_update_on_start")
        val APP_LAUNCHER_ALIAS = stringPreferencesKey("app_launcher_alias")
        val VAULT_PIN_HASH = stringPreferencesKey("vault_pin_hash")
        val SPLIT_HEADER_MEDIA_COUNT = booleanPreferencesKey("split_header_media_count")
        val HAS_COMPLETED_WALKTHROUGH = booleanPreferencesKey("has_completed_walkthrough")
    }

    val isVaultConfigured: Flow<Boolean> = context.dataStore.data.map { prefs ->
        !prefs[PreferenceKeys.VAULT_PIN_HASH].isNullOrBlank()
    }

    val vaultPinHash: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.VAULT_PIN_HASH] ?: ""
    }

    val preferencesFlow: Flow<Map<String, Any>> = context.dataStore.data.map { prefs ->
        val timelineShowDates = prefs[PreferenceKeys.SHOW_DATES_IN_TIMELINE_GRID]
            ?: prefs[PreferenceKeys.SHOW_DATES_IN_GRID]
            ?: true
        val collectionsShowDates = prefs[PreferenceKeys.SHOW_DATES_IN_COLLECTIONS_GRID]
            ?: true

        mapOf(
            "layout_mode" to (prefs[PreferenceKeys.LAYOUT_MODE] ?: "DAY"),
            "sort_order" to (prefs[PreferenceKeys.SORT_ORDER] ?: "DESCENDING"),
            "quick_filter" to (prefs[PreferenceKeys.QUICK_FILTER] ?: "ALL"),
            "show_dates_in_grid" to timelineShowDates,
            "show_dates_in_timeline_grid" to timelineShowDates,
            "show_dates_in_collections_grid" to collectionsShowDates,
            "is_dark_theme" to (prefs[PreferenceKeys.IS_DARK_THEME] ?: true),
            "selected_language" to (prefs[PreferenceKeys.SELECTED_LANGUAGE] ?: ""),
            "use_dynamic_color" to (prefs[PreferenceKeys.USE_DYNAMIC_COLOR] ?: true),
            "selected_theme_id" to (prefs[PreferenceKeys.SELECTED_THEME_ID] ?: "default"),
            "font_scale_multiplier" to (prefs[PreferenceKeys.FONT_SCALE_MULTIPLIER] ?: 1.0f),
            "selected_font_family" to (prefs[PreferenceKeys.SELECTED_FONT_FAMILY] ?: "Google Sans Flex"),
            "album_buttons_location" to (prefs[PreferenceKeys.ALBUM_BUTTONS_LOCATION] ?: "BOTTOM"),
            "floating_tab_bar_enabled" to (prefs[PreferenceKeys.FLOATING_TAB_BAR_ENABLED] ?: true),
            "is_delete_confirmation_enabled" to (prefs[PreferenceKeys.IS_DELETE_CONFIRMATION_ENABLED] ?: true),
            "is_app_lock_enabled" to (prefs[PreferenceKeys.IS_APP_LOCK_ENABLED] ?: false),
            "is_thumbnail_carousel_enabled" to (prefs[PreferenceKeys.IS_THUMBNAIL_CAROUSEL_ENABLED] ?: true),
            "is_video_muted" to (prefs[PreferenceKeys.IS_VIDEO_MUTED] ?: false),
            "show_app_logo_in_top_bar" to (prefs[PreferenceKeys.SHOW_APP_LOGO_IN_TOP_BAR] ?: true),
            "check_update_on_start" to (prefs[PreferenceKeys.CHECK_UPDATE_ON_START] ?: false),
            "app_launcher_alias" to (prefs[PreferenceKeys.APP_LAUNCHER_ALIAS] ?: "default"),
            "split_header_media_count" to (prefs[PreferenceKeys.SPLIT_HEADER_MEDIA_COUNT] ?: false),
            "has_completed_walkthrough" to (prefs[PreferenceKeys.HAS_COMPLETED_WALKTHROUGH] ?: false)
        )
    }

    suspend fun updateLayoutMode(mode: String) {
        context.dataStore.edit { it[PreferenceKeys.LAYOUT_MODE] = mode }
    }

    suspend fun updateSortOrder(order: String) {
        context.dataStore.edit { it[PreferenceKeys.SORT_ORDER] = order }
    }

    suspend fun updateQuickFilter(filter: String) {
        context.dataStore.edit { it[PreferenceKeys.QUICK_FILTER] = filter }
    }

    suspend fun updateShowDatesInGrid(show: Boolean) {
        context.dataStore.edit {
            it[PreferenceKeys.SHOW_DATES_IN_GRID] = show
            it[PreferenceKeys.SHOW_DATES_IN_TIMELINE_GRID] = show
        }
    }

    suspend fun updateShowDatesInTimelineGrid(show: Boolean) {
        context.dataStore.edit {
            it[PreferenceKeys.SHOW_DATES_IN_GRID] = show
            it[PreferenceKeys.SHOW_DATES_IN_TIMELINE_GRID] = show
        }
    }

    suspend fun updateShowDatesInCollectionsGrid(show: Boolean) {
        context.dataStore.edit {
            it[PreferenceKeys.SHOW_DATES_IN_COLLECTIONS_GRID] = show
        }
    }

    suspend fun updateIsDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_DARK_THEME] = enabled }
    }

    suspend fun updateSelectedLanguage(language: String) {
        context.dataStore.edit { it[PreferenceKeys.SELECTED_LANGUAGE] = language }
    }

    suspend fun updateUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateSelectedThemeId(themeId: String) {
        context.dataStore.edit { it[PreferenceKeys.SELECTED_THEME_ID] = themeId }
    }

    suspend fun updateTheme(themeId: String, useDynamicColor: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SELECTED_THEME_ID] = themeId
            prefs[PreferenceKeys.USE_DYNAMIC_COLOR] = useDynamicColor
        }
    }

    suspend fun updateFontScaleMultiplier(scale: Float) {
        context.dataStore.edit { it[PreferenceKeys.FONT_SCALE_MULTIPLIER] = scale }
    }

    suspend fun updateSelectedFontFamily(fontFamily: String) {
        context.dataStore.edit { it[PreferenceKeys.SELECTED_FONT_FAMILY] = fontFamily }
    }

    suspend fun updateAlbumButtonsLocation(location: String) {
        context.dataStore.edit { it[PreferenceKeys.ALBUM_BUTTONS_LOCATION] = location }
    }

    suspend fun updateFloatingTabBarEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.FLOATING_TAB_BAR_ENABLED] = enabled }
    }

    suspend fun updateDeleteConfirmationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_DELETE_CONFIRMATION_ENABLED] = enabled }
    }

    suspend fun updateAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_APP_LOCK_ENABLED] = enabled }
    }

    suspend fun updateShowAppLogoInTopBar(show: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SHOW_APP_LOGO_IN_TOP_BAR] = show }
    }

    suspend fun updateThumbnailCarouselEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_THUMBNAIL_CAROUSEL_ENABLED] = enabled }
    }

    suspend fun updateVideoMuted(muted: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.IS_VIDEO_MUTED] = muted }
    }

    suspend fun updateAppLauncherAlias(alias: String) {
        context.dataStore.edit { it[PreferenceKeys.APP_LAUNCHER_ALIAS] = alias }
    }

    suspend fun updateCheckUpdateOnStart(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.CHECK_UPDATE_ON_START] = enabled }
    }

    suspend fun updateVaultPinHash(hash: String) {
        context.dataStore.edit { it[PreferenceKeys.VAULT_PIN_HASH] = hash }
    }

    suspend fun updateSplitHeaderMediaCount(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.SPLIT_HEADER_MEDIA_COUNT] = enabled }
    }

    suspend fun updateHasCompletedWalkthrough(completed: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.HAS_COMPLETED_WALKTHROUGH] = completed }
    }
}
