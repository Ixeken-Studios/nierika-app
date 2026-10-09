package com.ixeken.nierika.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaWalkthroughSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Pantalla de Ajustes (SettingsScreen) que sigue estrictamente M3 Expressive,
 * los tokens de tema oscuro y la maqueta proporcionada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onRequestPermissions: () -> Unit,
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme
    var isAppearanceVisible by remember { mutableStateOf(false) }
    var isDisplayVisible by remember { mutableStateOf(false) }
    var isPrivacySecurityVisible by remember { mutableStateOf(false) }
    var isAboutVisible by remember { mutableStateOf(false) }
    var isLanguageBsVisible by remember { mutableStateOf(false) }

    BackHandler(onBack = {
        when {
            isAboutVisible -> isAboutVisible = false
            isPrivacySecurityVisible -> isPrivacySecurityVisible = false
            isDisplayVisible -> isDisplayVisible = false
            isAppearanceVisible -> isAppearanceVisible = false
            else -> onBackClick()
        }
    })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                IconButtonWithAnimatedShape(
                    onClick = {
                        when {
                            isAboutVisible -> isAboutVisible = false
                            isPrivacySecurityVisible -> isPrivacySecurityVisible = false
                            isDisplayVisible -> isDisplayVisible = false
                            isAppearanceVisible -> isAppearanceVisible = false
                            else -> onBackClick()
                        }
                    },
                    buttonSize = 44.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = stringResource(R.string.settings_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Preferences
                SettingsSection(title = stringResource(R.string.settings_section_preferences)) {
                    SettingsNavigationItem(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.settings_appearance_title),
                        subtitle = stringResource(R.string.settings_appearance_subtitle),
                        onClick = {
                            isAppearanceVisible = true
                            onNavigateToAppearance()
                        }
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingsNavigationItem(
                        icon = Icons.Filled.DisplaySettings,
                        title = stringResource(R.string.settings_display_title),
                        subtitle = stringResource(R.string.settings_display_subtitle),
                        onClick = {
                            isDisplayVisible = true
                        }
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    SettingsNavigationItem(
                        icon = Icons.Filled.Translate,
                        title = stringResource(R.string.settings_language_title),
                        subtitle = when (userPreferences.selectedLanguage) {
                            "es" -> stringResource(R.string.language_spanish_mx)
                            "en" -> stringResource(R.string.language_english_us)
                            else -> stringResource(R.string.language_system_default)
                        },
                        onClick = {
                            isLanguageBsVisible = true
                        }
                    )
                }

                // Section 2: Privacy and security
                SettingsSection(title = stringResource(R.string.settings_section_privacy_security)) {
                    SettingsNavigationItem(
                        icon = Icons.Filled.Lock,
                        title = stringResource(R.string.settings_section_privacy_security),
                        subtitle = stringResource(R.string.settings_privacy_security_subtitle),
                        onClick = {
                            isPrivacySecurityVisible = true
                        }
                    )
                }

                // Section 3: About
                SettingsSection(title = stringResource(R.string.settings_section_about)) {
                    SettingsNavigationItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(R.string.settings_nierika_about_title),
                        subtitle = stringResource(R.string.settings_nierika_about_subtitle),
                        onClick = {
                            isAboutVisible = true
                            onNavigateToAbout()
                        }
                    )
                }
            }
        }

        // Bottom Sheets
        val context = LocalContext.current
        LanguageSelectionBottomSheet(
            isVisible = isLanguageBsVisible,
            selectedLanguageCode = userPreferences.selectedLanguage,
            onLanguageSelect = { code ->
                viewModel.updateSelectedLanguage(context, code)
                isLanguageBsVisible = false
            },
            onDismiss = { isLanguageBsVisible = false }
        )

        PermissionsBottomSheet(
            isVisible = uiState.isPermissionsBottomSheetVisible,
            onDismiss = { viewModel.onShowPermissionsBottomSheet(false) },
            onRequestPermissions = onRequestPermissions
        )

        PrivacyInfoBottomSheet(
            isVisible = uiState.isPrivacyInfoBottomSheetVisible,
            onDismiss = { viewModel.onShowPrivacyInfoBottomSheet(false) }
        )

        if (uiState.isWalkthroughVisible) {
            NierikaWalkthroughSheet(
                onDismissRequest = { viewModel.onShowWalkthrough(false) },
                onComplete = { viewModel.onShowWalkthrough(false) }
            )
        }

        AnimatedVisibility(
            visible = isAppearanceVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            AppearanceScreen(
                viewModel = viewModel,
                onBackClick = { isAppearanceVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isDisplayVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            DisplayScreen(
                viewModel = viewModel,
                onBackClick = { isDisplayVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isPrivacySecurityVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            PrivacySecurityScreen(
                viewModel = viewModel,
                onBackClick = { isPrivacySecurityVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isAboutVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            AboutScreen(
                onBackClick = { isAboutVisible = false }
            )
        }

        AppIconSelectionBottomSheet(
            isVisible = uiState.isAppIconBottomSheetVisible,
            currentAlias = userPreferences.appLauncherAlias,
            onAliasSelected = { selectedAlias ->
                viewModel.onSelectLauncherAlias(selectedAlias, context)
            },
            onDismiss = { viewModel.onShowAppIconBottomSheet(false) }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = if (!subtitle.isNullOrEmpty()) {
            {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else null,
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}


