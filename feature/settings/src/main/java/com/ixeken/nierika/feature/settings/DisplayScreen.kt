package com.ixeken.nierika.feature.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import com.ixeken.nierika.core.domain.model.AlbumButtonsLocation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R

/**
 * Pantalla de Ajustes de Pantalla (DisplayScreen) alineada a Material 3 Expressive.
 * Contiene la sección Photo view y configuraciones de presentación visual (Layout and navigation).
 */
@Composable
fun DisplayScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    val userPreferences by viewModel.userPreferences.collectAsState()

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
                    text = stringResource(R.string.settings_display_title),
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
                // Section 1: Photo view
                DisplaySection(title = stringResource(R.string.settings_photo_view_header)) {
                    DisplaySwitchItem(
                        icon = Icons.Filled.ViewCarousel,
                        title = stringResource(R.string.settings_thumbnail_carousel_title),
                        subtitle = stringResource(R.string.settings_thumbnail_carousel_subtitle),
                        checked = userPreferences.isThumbnailCarouselEnabled,
                        onCheckedChange = viewModel::onToggleThumbnailCarousel
                    )
                }

                // Section 2: Layout and navigation
                DisplaySection(title = stringResource(R.string.appearance_section_layout_nav)) {
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
                                imageVector = Icons.Filled.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.appearance_album_buttons_location_title),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.appearance_album_buttons_location_subtitle),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val topLabel = stringResource(R.string.location_top)
                        val bottomLabel = stringResource(R.string.location_bottom)

                        NierikaConnectedButtonGroup(
                            items = listOf(AlbumButtonsLocation.TOP, AlbumButtonsLocation.BOTTOM),
                            selectedItem = userPreferences.albumButtonsLocation,
                            onItemSelected = { viewModel.updateAlbumButtonsLocation(it) },
                            labelProvider = { loc -> if (loc == AlbumButtonsLocation.TOP) topLabel else bottomLabel },
                            iconProvider = { loc -> if (loc == AlbumButtonsLocation.TOP) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown }
                        )
                    }

                    DisplaySwitchItem(
                        icon = Icons.Filled.Dock,
                        title = stringResource(R.string.appearance_floating_tab_bar_title),
                        subtitle = stringResource(R.string.appearance_floating_tab_bar_subtitle),
                        checked = userPreferences.floatingTabBarEnabled,
                        onCheckedChange = { viewModel.updateFloatingTabBarEnabled(it) }
                    )

                    DisplaySwitchItem(
                        icon = Icons.Filled.Badge,
                        title = stringResource(R.string.settings_show_app_logo_title),
                        subtitle = stringResource(R.string.settings_show_app_logo_subtitle),
                        checked = userPreferences.showAppLogoInTopBar,
                        onCheckedChange = { viewModel.onToggleShowAppLogoInTopBar(it) }
                    )

                    DisplaySwitchItem(
                        icon = Icons.Filled.PermMedia,
                        title = stringResource(R.string.settings_split_media_count_title),
                        subtitle = stringResource(R.string.settings_split_media_count_subtitle),
                        checked = userPreferences.splitHeaderMediaCount,
                        onCheckedChange = { viewModel.updateSplitHeaderMediaCount(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DisplaySection(
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
        ) {
            content()
        }
    }
}

@Composable
private fun DisplaySwitchItem(
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
