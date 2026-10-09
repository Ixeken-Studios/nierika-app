package com.ixeken.nierika.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Palette
import com.ixeken.nierika.feature.settings.util.getTitleRes
import com.ixeken.nierika.feature.settings.util.getLabelRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.SliderWithTrackIcons
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import com.ixeken.nierika.core.designsystem.theme.ColorSchemeStyle

/**
 * Pantalla de Ajustes de Apariencia (AppearanceScreen).
 * Permite ajustar Theming, Font style (con slider de 5 pasos), y Layout and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()

    var isThemeBsVisible by remember { mutableStateOf(false) }
    var isFontBsVisible by remember { mutableStateOf(false) }
    var fontScaleStep by remember { mutableStateOf(2f) }
    var floatingTabBarEnabled by remember { mutableStateOf(true) }

    val fontScaleLabels = listOf("Very small", "Small", "Default", "Large", "Very large")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButtonWithAnimatedShape(
                    onClick = onBackClick,
                    buttonSize = 44.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = stringResource(R.string.settings_appearance_title),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Theming
                AppearanceSection(title = stringResource(R.string.appearance_section_theming)) {
                    AppearanceSwitchItem(
                        icon = Icons.Filled.DarkMode,
                        title = stringResource(R.string.appearance_dark_theme_title),
                        subtitle = stringResource(R.string.appearance_dark_theme_subtitle),
                        checked = userPreferences.isDarkTheme,
                        onCheckedChange = { viewModel.updateIsDarkTheme(it) }
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AppearanceNavigationItem(
                        icon = Icons.Filled.Palette,
                        title = stringResource(R.string.appearance_theme_selection_title),
                        subtitle = stringResource(R.string.appearance_theme_selection_subtitle),
                        onClick = { isThemeBsVisible = true }
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AppearanceSwitchItem(
                        icon = Icons.Filled.AutoAwesome,
                        title = stringResource(R.string.appearance_dynamic_color_title),
                        subtitle = stringResource(R.string.appearance_dynamic_color_subtitle),
                        checked = userPreferences.useDynamicColor,
                        onCheckedChange = { viewModel.updateUseDynamicColor(it) }
                    )
                }

                // Section 2: Font style
                AppearanceSection(title = stringResource(R.string.appearance_section_font_style)) {
                    AppearanceNavigationItem(
                        icon = Icons.Filled.FontDownload,
                        title = stringResource(R.string.appearance_font_selection_title),
                        subtitle = userPreferences.selectedFontFamily,
                        onClick = { isFontBsVisible = true }
                    )
                    androidx.compose.material3.HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Font size item with 5-step slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TextIncrease,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.appearance_font_size_title),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.appearance_font_size_subtitle),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val fontScaleSteps = listOf(0.85f, 0.92f, 1.0f, 1.08f, 1.16f)
                        val currentStep = fontScaleSteps.indexOfFirst { kotlin.math.abs(it - userPreferences.fontScaleMultiplier) < 0.02f }.let { if (it != -1) it.toFloat() else 2f }

                        SliderWithTrackIcons(
                            value = currentStep,
                            onValueChange = { step ->
                                val targetScale = fontScaleSteps[step.toInt().coerceIn(0, 4)]
                                viewModel.updateFontScaleMultiplier(targetScale)
                            },
                            valueRange = 0f..4f,
                            steps = 3,
                            startIcon = Icons.Filled.TextDecrease,
                            endIcon = Icons.Filled.TextIncrease,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                activeTickColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = fontScaleLabels.getOrElse(currentStep.toInt()) { "Default" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }

                // Section 3: App Identity (Icon and Label)
                val currentAlias = userPreferences.appLauncherAlias
                AppearanceSection(title = stringResource(R.string.settings_app_identity_header)) {
                    AppearanceNavigationItem(
                        icon = Icons.Filled.AddToPhotos,
                        title = stringResource(R.string.app_icon_bs_title),
                        subtitle = "${stringResource(currentAlias.iconStyle.getTitleRes())} • ${stringResource(currentAlias.labelOption.getLabelRes())}",
                        onClick = { viewModel.onShowAppIconBottomSheet(true) }
                    )
                }
            }
        }

        // Bottom Sheets
        ThemeSelectionBottomSheet(
            isVisible = isThemeBsVisible,
            selectedStyle = ColorSchemeStyle.fromId(userPreferences.selectedThemeId),
            isDarkTheme = userPreferences.isDarkTheme,
            onStyleSelect = { style ->
                viewModel.updateSelectedThemeId(style.id)
                isThemeBsVisible = false
            },
            onDismiss = { isThemeBsVisible = false }
        )

        FontSelectionBottomSheet(
            isVisible = isFontBsVisible,
            selectedFontName = userPreferences.selectedFontFamily,
            onFontSelect = { fontName ->
                viewModel.updateSelectedFontFamily(fontName)
                isFontBsVisible = false
            },
            onDismiss = { isFontBsVisible = false }
        )
    }
}

@Composable
private fun AppearanceSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun AppearanceNavigationItem(
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = if (!subtitle.isNullOrEmpty()) {
            {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
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

@Composable
private fun AppearanceSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        },
        trailingContent = {
            SwitchWithThumbIcon(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
    )
}
