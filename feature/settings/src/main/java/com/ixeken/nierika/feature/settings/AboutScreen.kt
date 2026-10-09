package com.ixeken.nierika.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.SwitchWithThumbIcon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ixeken.nierika.feature.settings.util.GitHubUpdateChecker
import com.ixeken.nierika.feature.settings.util.UpdateResult
import kotlinx.coroutines.launch

private const val SOURCE_CODE_URL = "https://github.com/Ixeken-Studios/nierika-app"
private const val IXEKEN_STUDIOS_URL = "https://github.com/Ixeken-Studios"

private enum class UpdateConsentAction {
    MANUAL_CHECK,
    ENABLE_STARTUP_CHECK
}

/**
 * Pantalla de Información de la Aplicación (Nierika AboutScreen).
 * Sigue estrictamente M3 Expressive, los tokens de tema oscuro y la maqueta oficial.
 */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val userPreferences by viewModel.userPreferences.collectAsState()

    var showReleaseNotesBottomSheet by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var pendingConsentAction by remember { mutableStateOf<UpdateConsentAction?>(null) }
    var latestNewVersion by remember { mutableStateOf<UpdateResult.NewVersion?>(null) }
    var upToDateResult by remember { mutableStateOf<UpdateResult.UpToDate?>(null) }
    var futureVersionResult by remember { mutableStateOf<UpdateResult.FutureVersion?>(null) }
    var showRepoNotFoundDialog by remember { mutableStateOf(false) }
    var showNetworkErrorDialog by remember { mutableStateOf(false) }
    var isCheckingForUpdates by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)

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
            // Header Top Bar con Logo Mascota y Versión
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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

                    Column {
                        Text(
                            text = stringResource(R.string.settings_nierika_about_title),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 14.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.about_version),
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Top Right Nierika App Logo Badge
                Image(
                    painter = painterResource(R.drawable.nierika_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
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
                // Section 1: Information
                AboutSection(title = stringResource(R.string.about_section_information)) {
                    AboutNavigationItem(
                        icon = Icons.Filled.Code,
                        title = stringResource(R.string.about_view_source_code_title),
                        subtitle = null,
                        onClick = {
                            try {
                                uriHandler.openUri(SOURCE_CODE_URL)
                            } catch (_: Exception) {
                                // Fail-safe silencioso si no se encuentra un handler/navegador disponible
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AboutNavigationItem(
                        icon = Icons.AutoMirrored.Filled.Article,
                        title = stringResource(R.string.about_release_notes_title),
                        subtitle = null,
                        onClick = { showReleaseNotesBottomSheet = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AboutNavigationItem(
                        painter = painterResource(R.drawable.ixeken_logo),
                        title = stringResource(R.string.about_by_ixeken_studios_title),
                        subtitle = stringResource(R.string.about_by_ixeken_studios_subtitle),
                        onClick = {
                            try {
                                uriHandler.openUri(IXEKEN_STUDIOS_URL)
                            } catch (_: Exception) {
                                // Fail-safe silencioso si no se encuentra un handler/navegador disponible
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AboutNavigationItem(
                        icon = Icons.Filled.Explore,
                        title = stringResource(R.string.settings_about_walkthrough_title),
                        subtitle = stringResource(R.string.settings_about_walkthrough_subtitle),
                        onClick = { viewModel.onShowWalkthrough(true) }
                    )
                }

                // Section 2: App updating
                AboutSection(title = stringResource(R.string.about_section_app_updating)) {
                    AboutNavigationItem(
                        icon = Icons.Filled.SystemUpdate,
                        title = stringResource(R.string.about_check_for_updates_title),
                        subtitle = stringResource(R.string.about_check_for_updates_subtitle),
                        onClick = {
                            pendingConsentAction = UpdateConsentAction.MANUAL_CHECK
                            showConsentDialog = true
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    AboutSwitchItem(
                        icon = Icons.Filled.RocketLaunch,
                        title = stringResource(R.string.about_check_update_on_start_title),
                        subtitle = stringResource(R.string.about_check_update_on_start_subtitle),
                        checked = userPreferences.checkUpdateOnStart,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                pendingConsentAction = UpdateConsentAction.ENABLE_STARTUP_CHECK
                                showConsentDialog = true
                            } else {
                                viewModel.onToggleCheckUpdateOnStart(false)
                            }
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.about_stands_with_palestine),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 16.dp)
                )
            }
        }

        ReleaseNotesBottomSheet(
            isVisible = showReleaseNotesBottomSheet,
            onDismiss = { showReleaseNotesBottomSheet = false }
        )

        InternetConsentDialog(
            isVisible = showConsentDialog,
            onConfirm = {
                val action = pendingConsentAction
                showConsentDialog = false
                pendingConsentAction = null
                when (action) {
                    UpdateConsentAction.ENABLE_STARTUP_CHECK -> {
                        viewModel.onToggleCheckUpdateOnStart(true)
                    }
                    UpdateConsentAction.MANUAL_CHECK -> {
                        isCheckingForUpdates = true
                        coroutineScope.launch {
                            val result = GitHubUpdateChecker.checkForUpdates(context)
                            isCheckingForUpdates = false
                            when (result) {
                                is UpdateResult.NewVersion -> {
                                    latestNewVersion = result
                                }
                                is UpdateResult.UpToDate -> {
                                    upToDateResult = result
                                }
                                is UpdateResult.FutureVersion -> {
                                    futureVersionResult = result
                                }
                                is UpdateResult.RepoNotFoundOrPrivate -> {
                                    showRepoNotFoundDialog = true
                                }
                                is UpdateResult.NoInternet -> {
                                    showNetworkErrorDialog = true
                                }
                            }
                        }
                    }
                    null -> Unit
                }
            },
            onDismiss = {
                showConsentDialog = false
                pendingConsentAction = null
            }
        )

        UpdateCheckingDialog(
            isVisible = isCheckingForUpdates
        )

        NewVersionAvailableDialog(
            newVersion = latestNewVersion,
            onViewRelease = { releaseUrl ->
                latestNewVersion = null
                try {
                    uriHandler.openUri(releaseUrl)
                } catch (_: Exception) {
                    // Fail-safe silencioso si no se encuentra un navegador disponible
                }
            },
            onDismiss = { latestNewVersion = null }
        )

        UpToDateDialog(
            upToDate = upToDateResult,
            onDismiss = { upToDateResult = null }
        )

        FutureVersionDialog(
            futureVersion = futureVersionResult,
            onDismiss = { futureVersionResult = null }
        )

        RepoNotFoundOrPrivateDialog(
            isVisible = showRepoNotFoundDialog,
            onDismiss = { showRepoNotFoundDialog = false }
        )

        UpdateErrorDialog(
            isVisible = showNetworkErrorDialog,
            onDismiss = { showNetworkErrorDialog = false }
        )
    }
}

@Composable
private fun AboutSection(
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
private fun AboutNavigationItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
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
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
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
private fun AboutSwitchItem(
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
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
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
