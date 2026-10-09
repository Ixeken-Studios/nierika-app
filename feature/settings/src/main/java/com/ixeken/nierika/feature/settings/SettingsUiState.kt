package com.ixeken.nierika.feature.settings

import androidx.compose.runtime.Immutable

/**
 * Estado inmutable de la pantalla de ajustes de la aplicación Nierika.
 */
@Immutable
data class SettingsUiState(
    val isDeleteConfirmationEnabled: Boolean = true,
    val isAppLockEnabled: Boolean = false,
    val isPermissionsBottomSheetVisible: Boolean = false,
    val isPrivacyInfoBottomSheetVisible: Boolean = false,
    val isAppIconBottomSheetVisible: Boolean = false,
    val isWalkthroughVisible: Boolean = false
)
