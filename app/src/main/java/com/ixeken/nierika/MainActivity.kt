package com.ixeken.nierika

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ixeken.nierika.core.domain.model.MediaItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.MaterialTheme
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.gif.AnimatedImageDecoder
import coil3.video.VideoFrameDecoder
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.AppLockOverlay
import com.ixeken.nierika.core.designsystem.theme.NierikaTheme
import com.ixeken.nierika.core.designsystem.theme.getAppFontFamilyByName
import com.ixeken.nierika.core.designsystem.util.BiometricHelper
import com.ixeken.nierika.core.designsystem.theme.ColorSchemeStyle
import com.ixeken.nierika.feature.timeline.TimelineScreen
import com.ixeken.nierika.feature.timeline.TimelineViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val timelineViewModel: TimelineViewModel by viewModels()
    var onUserLeaveHintListener: (() -> Unit)? = null

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        onUserLeaveHintListener?.invoke()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val isAlreadyGranted = requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }
        timelineViewModel.onPermissionResult(isAlreadyGranted)

        val intentAction = intent?.action
        val isPickerMode = intentAction == Intent.ACTION_PICK || intentAction == Intent.ACTION_GET_CONTENT
        val allowMultiplePick = intent?.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) == true
        val isViewMode = intentAction == Intent.ACTION_VIEW ||
                intentAction == "com.android.camera.action.REVIEW" ||
                intentAction == "android.provider.action.REVIEW"
        val viewUri = if (isViewMode) intent?.data else null

        val handleMediaPicked: (List<MediaItem>) -> Unit = { pickedItems ->
            if (pickedItems.isNotEmpty()) {
                val resultIntent = Intent().apply {
                    val firstUri = Uri.parse(pickedItems.first().uri)
                    data = firstUri
                    setDataAndType(firstUri, pickedItems.first().mimeType)
                    if (pickedItems.size > 1) {
                        val clipData = ClipData.newUri(contentResolver, "media", firstUri)
                        pickedItems.drop(1).forEach { item ->
                            clipData.addItem(ClipData.Item(Uri.parse(item.uri)))
                        }
                        this.clipData = clipData
                    }
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

        setContent {
            setSingletonImageLoaderFactory { context ->
                ImageLoader.Builder(context)
                    .memoryCache {
                        MemoryCache.Builder()
                            .maxSizePercent(context, 0.35)
                            .build()
                    }
                    .diskCache {
                        DiskCache.Builder()
                            .directory(context.cacheDir.resolve("image_cache"))
                            .maxSizeBytes(500 * 1024 * 1024L)
                            .build()
                    }
                    .components {
                        add(com.ixeken.nierika.core.mediastore.MediaStoreThumbnailFetcher.Factory(context))
                        add(com.ixeken.nierika.core.mediastore.MediaStoreThumbnailFetcher.AndroidUriFactory(context))
                        add(AnimatedImageDecoder.Factory())
                        add(VideoFrameDecoder.Factory())
                    }
                    .build()
            }

            val userPreferences by timelineViewModel.userPreferences.collectAsState()
            val fontFamily = remember(userPreferences.selectedFontFamily) {
                getAppFontFamilyByName(userPreferences.selectedFontFamily)
            }

            var isUnlocked by remember { mutableStateOf(!userPreferences.isAppLockEnabled) }
            var isAuthenticatingBiometric by remember { mutableStateOf(false) }

            // Configurar FLAG_SECURE para evitar vista previa (recents thumbnail) si la app esta bloqueada
            LaunchedEffect(userPreferences.isAppLockEnabled) {
                if (userPreferences.isAppLockEnabled) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    isUnlocked = true
                }
            }

            // Resetear estado de desbloqueo al enviar la app a segundo plano (ON_STOP)
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && userPreferences.isAppLockEnabled && !isAuthenticatingBiometric) {
                        isUnlocked = false
                    }
                    if (event == Lifecycle.Event.ON_START) {
                        timelineViewModel.refreshSilent()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val promptTitle = stringResource(R.string.app_lock_prompt_title)
            val promptSubtitle = stringResource(R.string.app_lock_subtitle)

            val requestUnlock: () -> Unit = {
                if (!isAuthenticatingBiometric) {
                    if (BiometricHelper.canAuthenticate(this@MainActivity)) {
                        isAuthenticatingBiometric = true
                        BiometricHelper.showBiometricPrompt(
                            activity = this@MainActivity,
                            title = promptTitle,
                            subtitle = promptSubtitle,
                            onSuccess = {
                                isAuthenticatingBiometric = false
                                isUnlocked = true
                            },
                            onError = {
                                isAuthenticatingBiometric = false
                            }
                        )
                    } else {
                        isUnlocked = true
                    }
                }
            }

            NierikaTheme(
                darkTheme = userPreferences.isDarkTheme,
                dynamicColor = userPreferences.useDynamicColor,
                themeStyle = ColorSchemeStyle.fromId(userPreferences.selectedThemeId),
                fontFamily = fontFamily,
                fontScaleMultiplier = userPreferences.fontScaleMultiplier
            ) {
                val uiState by timelineViewModel.uiState.collectAsState()
                val pagingItems = timelineViewModel.mediaPagingData.collectAsLazyPagingItems()

                val permissionsArray = remember { requiredPermissions }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val isGranted = permissions.values.all { it }
                    timelineViewModel.onPermissionResult(isGranted)
                }

                TimelineScreen(
                    pagingItems = pagingItems,
                    uiState = uiState,
                    onRequestPermission = {
                        permissionLauncher.launch(permissionsArray)
                    },
                    onSortOrderChange = timelineViewModel::onSortOrderSelected,
                    onFilterChange = timelineViewModel::onFilterSelected,
                    onLayoutModeChange = timelineViewModel::onLayoutModeSelected,
                    onShowDatesInGridChange = timelineViewModel::onToggleShowDatesInGrid,
                    onToggleSideSheet = timelineViewModel::onToggleSideSheet,
                    isPickerMode = isPickerMode,
                    allowMultiplePick = allowMultiplePick,
                    onPickMedia = if (isPickerMode) handleMediaPicked else null,
                    initialViewUri = viewUri,
                    onDismissExternalView = {
                        if (isViewMode) {
                            finish()
                        }
                    }
                )

                val motionScheme = MaterialTheme.nierikaMotionScheme
                AnimatedVisibility(
                    visible = userPreferences.isAppLockEnabled && !isUnlocked,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = motionScheme.defaultSpatialSpec()
                    )
                ) {
                    AppLockOverlay(
                        onRequestUnlock = requestUnlock
                    )
                }

                // Verificacion silenciosa de actualizaciones al inicio si el usuario lo activo
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                var startupNewVersion by remember { mutableStateOf<com.ixeken.nierika.feature.settings.util.UpdateResult.NewVersion?>(null) }

                LaunchedEffect(userPreferences.checkUpdateOnStart) {
                    if (userPreferences.checkUpdateOnStart) {
                        val result = com.ixeken.nierika.feature.settings.util.GitHubUpdateChecker.checkForUpdates(this@MainActivity)
                        if (result is com.ixeken.nierika.feature.settings.util.UpdateResult.NewVersion) {
                            startupNewVersion = result
                        }
                    }
                }

                com.ixeken.nierika.feature.settings.NewVersionAvailableDialog(
                    newVersion = startupNewVersion,
                    onViewRelease = { releaseUrl ->
                        startupNewVersion = null
                        try {
                            uriHandler.openUri(releaseUrl)
                        } catch (_: Exception) {
                            // Fail-safe silencioso si no se encuentra un navegador disponible
                        }
                    },
                    onDismiss = { startupNewVersion = null }
                )
            }
        }
    }
}