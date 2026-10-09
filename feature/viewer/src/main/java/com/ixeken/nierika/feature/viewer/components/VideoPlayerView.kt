package com.ixeken.nierika.feature.viewer.components

import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon as AndroidIcon
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

private const val ACTION_PIP_PLAY = "com.ixeken.nierika.ACTION_PIP_PLAY"
private const val ACTION_PIP_PAUSE = "com.ixeken.nierika.ACTION_PIP_PAUSE"
private const val ACTION_PIP_REWIND = "com.ixeken.nierika.ACTION_PIP_REWIND"
private const val ACTION_PIP_FORWARD = "com.ixeken.nierika.ACTION_PIP_FORWARD"

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Composable para la reproducción nativa de videos locales mediante AndroidX Media3 ExoPlayer.
 * Diseño refinado con cápsulas semitransparentes M3 Expressive:
 * - Fila Superior: Botón Cíclico de Velocidad (0.5x, 1.0x, 1.5x, 2.0x) a la izquierda, Trío Central (-5s, Play/Pausa 56dp, +5s) al centro, Botón Mute a la derecha.
 * - Fila Inferior: Tiempo transcurrido (00:33) + Pista unificada de avance + Tiempo total (01:36).
 * - Soporte nativo para Picture-in-Picture (PiP) al realizar el gesto de Home.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerView(
    uriString: String,
    isCurrentPage: Boolean,
    areControlsVisible: Boolean,
    onToggleControls: () -> Unit,
    isVideoMuted: Boolean = false,
    onToggleMute: (Boolean) -> Unit = {},
    hasCarousel: Boolean = true,
    onPositionChanged: (Long) -> Unit = {},
    onZoomStateChanged: (Boolean) -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activity = remember(context) { context.findActivity() }

    val exoPlayer = remember(context, uriString) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(uriString)))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = if (isVideoMuted) 0f else 1f
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember(isVideoMuted) { mutableStateOf(isVideoMuted) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isInPipMode by remember { mutableStateOf(activity?.isInPictureInPictureMode == true) }
    var videoWidth by remember { mutableIntStateOf(exoPlayer.videoSize.width) }
    var videoHeight by remember { mutableIntStateOf(exoPlayer.videoSize.height) }
    var videoContainerBounds by remember { mutableStateOf<android.graphics.Rect?>(null) }

    // Escuchar acciones nativas del sistema Android desde la ventana de Picture-in-Picture (PiP)
    DisposableEffect(context, exoPlayer) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_PIP_PLAY -> exoPlayer.play()
                    ACTION_PIP_PAUSE -> exoPlayer.pause()
                    ACTION_PIP_REWIND -> {
                        val newPos = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                        exoPlayer.seekTo(newPos)
                    }
                    ACTION_PIP_FORWARD -> {
                        val newPos = (exoPlayer.currentPosition + 5000L).coerceAtMost(exoPlayer.duration)
                        exoPlayer.seekTo(newPos)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(ACTION_PIP_PLAY)
            addAction(ACTION_PIP_PAUSE)
            addAction(ACTION_PIP_REWIND)
            addAction(ACTION_PIP_FORWARD)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) { }
        }
    }

    var lastPipParams by remember { mutableStateOf<PictureInPictureParams?>(null) }

    // Configurar Picture-in-Picture nativo de Android para video con sourceRectHint y autoEnterEnabled continuo
    LaunchedEffect(isCurrentPage, isPlaying, videoWidth, videoHeight, videoContainerBounds, activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity != null) {
            val builder = PictureInPictureParams.Builder()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(isCurrentPage)
            }

            val effectiveW = if (videoWidth > 0) videoWidth else exoPlayer.videoSize.width
            val effectiveH = if (videoHeight > 0) videoHeight else exoPlayer.videoSize.height

            if (effectiveW > 0 && effectiveH > 0) {
                val ratio = effectiveW.toFloat() / effectiveH.toFloat()
                // Límites estrictos de Android OS para PiP: entre 1/2.39 (~0.418410) y 2.39/1 (2.390000).
                // 100/239 = 0.41841004f garantiza cumplimiento exacto para prevenir IllegalArgumentException.
                val clampedRatio = when {
                    ratio > 2.39f -> Rational(239, 100)
                    ratio < (100f / 239f) -> Rational(100, 239)
                    else -> Rational(effectiveW, effectiveH)
                }
                builder.setAspectRatio(clampedRatio)
            }

            videoContainerBounds?.let { rect ->
                val displayMetrics = context.resources.displayMetrics
                val screenW = displayMetrics.widthPixels
                val screenH = displayMetrics.heightPixels
                val safeRect = android.graphics.Rect(
                    rect.left.coerceIn(0, screenW),
                    rect.top.coerceIn(0, screenH),
                    rect.right.coerceIn(0, screenW),
                    rect.bottom.coerceIn(0, screenH)
                )
                if (safeRect.width() > 0 && safeRect.height() > 0) {
                    builder.setSourceRectHint(safeRect)
                }
            }

            val actions = mutableListOf<RemoteAction>()

            // Rewind -5s: Se debe usar el paquete del sistema "android" para evitar Resources$NotFoundException en SystemUI
            val rewindIntent = PendingIntent.getBroadcast(
                context, 1, Intent(ACTION_PIP_REWIND).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val rewindIcon = AndroidIcon.createWithResource("android", android.R.drawable.ic_media_rew)
            actions.add(RemoteAction(rewindIcon, "-5s", "-5s", rewindIntent))

            // Play / Pause
            if (isPlaying) {
                val pauseIntent = PendingIntent.getBroadcast(
                    context, 2, Intent(ACTION_PIP_PAUSE).setPackage(context.packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val pauseIcon = AndroidIcon.createWithResource("android", android.R.drawable.ic_media_pause)
                actions.add(RemoteAction(pauseIcon, "Pausa", "Pausa", pauseIntent))
            } else {
                val playIntent = PendingIntent.getBroadcast(
                    context, 3, Intent(ACTION_PIP_PLAY).setPackage(context.packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val playIcon = AndroidIcon.createWithResource("android", android.R.drawable.ic_media_play)
                actions.add(RemoteAction(playIcon, "Play", "Play", playIntent))
            }

            // Forward +5s
            val forwardIntent = PendingIntent.getBroadcast(
                context, 4, Intent(ACTION_PIP_FORWARD).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val forwardIcon = AndroidIcon.createWithResource("android", android.R.drawable.ic_media_ff)
            actions.add(RemoteAction(forwardIcon, "+5s", "+5s", forwardIntent))

            builder.setActions(actions)

            val builtParams = builder.build()
            lastPipParams = builtParams
            try {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    activity.setPictureInPictureParams(builtParams)
                }
            } catch (t: Throwable) { }
        }
    }

    // Activar Picture-in-Picture al cambiar de aplicación en Android 8.0 a 11. En Android 12+ (API 31+),
    // setAutoEnterEnabled(true) ya realiza la transición de forma atómica en el gesto de Home evitando colisiones.
    DisposableEffect(activity, isCurrentPage, lastPipParams) {
        val act = activity
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.O until Build.VERSION_CODES.S && act != null && isCurrentPage) {
            val pipListener: () -> Unit = {
                if (!act.isFinishing && !act.isDestroyed && !act.isInPictureInPictureMode) {
                    try {
                        val params = lastPipParams ?: PictureInPictureParams.Builder().build()
                        act.enterPictureInPictureMode(params)
                    } catch (t: Throwable) { }
                }
            }
            try {
                val field = act::class.java.getDeclaredField("onUserLeaveHintListener")
                field.isAccessible = true
                field.set(act, pipListener)
            } catch (t: Throwable) { }
        }
        onDispose {
            val act = activity
            if (act != null) {
                try {
                    val field = act::class.java.getDeclaredField("onUserLeaveHintListener")
                    field.isAccessible = true
                    field.set(act, null)
                } catch (t: Throwable) { }
            }
        }
    }

    DisposableEffect(activity) {
        val act = activity as? androidx.activity.ComponentActivity
        if (act == null) return@DisposableEffect onDispose {}

        val consumer = androidx.core.util.Consumer<androidx.core.app.PictureInPictureModeChangedInfo> { info ->
            isInPipMode = info.isInPictureInPictureMode
        }
        act.addOnPictureInPictureModeChangedListener(consumer)

        onDispose {
            act.removeOnPictureInPictureModeChangedListener(consumer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !act.isFinishing && !act.isDestroyed) {
                try {
                    act.setPictureInPictureParams(
                        PictureInPictureParams.Builder().setAutoEnterEnabled(false).build()
                    )
                } catch (t: Throwable) { }
            }
        }
    }



    // Controlar reproducción/pausa según la página activa SIN destruir exoPlayer
    LaunchedEffect(isCurrentPage, exoPlayer) {
        if (isCurrentPage) {
            if (exoPlayer.playbackState == Player.STATE_IDLE || exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.prepare()
            }
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.pause()
            exoPlayer.playWhenReady = false
        }
    }

    // Pausar automáticamente la reproducción al pasar a segundo plano si no está en modo Picture-in-Picture
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                val act = activity as? ComponentActivity
                val inPip = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && act?.isInPictureInPictureMode == true
                if (!inPip) {
                    exoPlayer.pause()
                    exoPlayer.playWhenReady = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Liberar exoPlayer ÚNICAMENTE al desmontarse el composable por completo de la memoria
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    videoWidth = videoSize.width
                    videoHeight = videoSize.height
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.pause()
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    LaunchedEffect(isCurrentPage, exoPlayer) {
        while (isActive) {
            if (isCurrentPage) {
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
                onPositionChanged(currentPositionMs)
            }
            delay(200L)
        }
    }

    val motionScheme = MaterialTheme.nierikaMotionScheme
    val coroutineScope = rememberCoroutineScope()

    ZoomableMediaContainer(
        modifier = modifier.fillMaxSize(),
        isCurrentPage = isCurrentPage,
        onToggleControls = onToggleControls,
        onZoomStateChanged = onZoomStateChanged,
        onDismiss = onDismiss,
        onHoldStart = if (!areControlsVisible && isPlaying) { { exoPlayer.pause() } } else null,
        onHoldEnd = if (!areControlsVisible && isPlaying) { { exoPlayer.play() } } else null
    ) { graphicsModifier ->
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    controllerAutoShow = false
                    controllerHideOnTouch = false
                    hideController()
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.useController = false
                playerView.controllerAutoShow = false
                playerView.controllerHideOnTouch = false
                playerView.hideController()
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    if (coordinates.isAttached) {
                        val bounds = coordinates.boundsInWindow()
                        if (bounds.width > 0 && bounds.height > 0) {
                            val w = if (videoWidth > 0) videoWidth else exoPlayer.videoSize.width
                            val h = if (videoHeight > 0) videoHeight else exoPlayer.videoSize.height
                            if (w > 0 && h > 0) {
                                val videoRatio = w.toFloat() / h.toFloat()
                                val viewRatio = bounds.width / bounds.height
                                val fittedRect = if (videoRatio > viewRatio) {
                                    val fittedHeight = bounds.width / videoRatio
                                    val topOffset = bounds.top + (bounds.height - fittedHeight) / 2f
                                    android.graphics.Rect(
                                        bounds.left.toInt(),
                                        topOffset.toInt(),
                                        bounds.right.toInt(),
                                        (topOffset + fittedHeight).toInt()
                                    )
                                } else {
                                    val fittedWidth = bounds.height * videoRatio
                                    val leftOffset = bounds.left + (bounds.width - fittedWidth) / 2f
                                    android.graphics.Rect(
                                        leftOffset.toInt(),
                                        bounds.top.toInt(),
                                        (leftOffset + fittedWidth).toInt(),
                                        bounds.bottom.toInt()
                                    )
                                }
                                videoContainerBounds = fittedRect
                            } else {
                                videoContainerBounds = android.graphics.Rect(
                                    bounds.left.toInt(),
                                    bounds.top.toInt(),
                                    bounds.right.toInt(),
                                    bounds.bottom.toInt()
                                )
                            }
                        }
                    }
                }
                .then(graphicsModifier)
        )

        val showOverlay = areControlsVisible && !isInPipMode && isCurrentPage
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isCompactLandscape = isLandscape && configuration.screenHeightDp < 520
        val screenWidthDp = configuration.screenWidthDp

        val adaptiveVideoHorizontalPadding = remember(screenWidthDp, isLandscape) {
            if (isLandscape) {
                val buttonHorizontalPadding = when {
                    screenWidthDp >= 840 -> 32.dp
                    screenWidthDp >= 600 -> 24.dp
                    else -> 16.dp
                }
                val buttonSize = 40.dp
                // Margen de seguridad para evitar colision visual y tactil con Compartir y Borrar
                buttonHorizontalPadding + buttonSize + 24.dp
            } else {
                when {
                    screenWidthDp >= 840 -> 32.dp
                    screenWidthDp >= 600 -> 24.dp
                    else -> 16.dp
                }
            }
        }

        val animatedBottomPadding by animateDpAsState(
            targetValue = when {
                isLandscape -> 4.dp
                hasCarousel -> when {
                    screenWidthDp >= 600 -> 164.dp
                    else -> 156.dp
                }
                else -> when {
                    screenWidthDp >= 600 -> 92.dp
                    else -> 84.dp
                }
            },
            animationSpec = motionScheme.fastSpatialSpec(),
            label = "videoControlsBottomPadding"
        )

        val speedText = when (playbackSpeed) {
            0.5f -> "0.5x"
            1.5f -> "1.5x"
            2.0f -> "2.0x"
            else -> "1x"
        }

        // Capa de Controles del Reproductor (Material 3 Expressive)
        AnimatedVisibility(
            visible = showOverlay,
            enter = slideInVertically(
                animationSpec = motionScheme.fastSpatialSpec(),
                initialOffsetY = { it * 8 }
            ),
            exit = slideOutVertically(
                animationSpec = motionScheme.fastSpatialSpec(),
                targetOffsetY = { it * 8 }
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    bottom = animatedBottomPadding.coerceAtLeast(0.dp),
                    start = adaptiveVideoHorizontalPadding.coerceAtLeast(0.dp),
                    end = adaptiveVideoHorizontalPadding.coerceAtLeast(0.dp)
                )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Cápsula Unificada M3 Expressive de Controles de Video.
                // En modo apaisado se fija su altura vertical en 40dp para alinearse
                // exactamente con los botones circulares de Compartir y Borrar (40dp).
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = if (isLandscape) 500.dp else 640.dp)
                        .then(if (isLandscape) Modifier.height(40.dp) else Modifier)
                ) {
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides if (isLandscape) Dp.Unspecified else 48.dp
                    ) {
                        Row(
                            modifier = if (isLandscape) {
                                Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp)
                            } else {
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // 1. Botón Play / Pausa (Mismo tamaño simétrico, limpio sin contenedor extra)
                            val playInteractionSource = remember { MutableInteractionSource() }
                            val isPlayPressed by playInteractionSource.collectIsPressedAsState()
                            val playCornerPercent by animateIntAsState(
                                targetValue = if (isPlayPressed) 26 else 50,
                                animationSpec = motionScheme.fastSpatialSpec(),
                                label = "playCorner"
                            )
                            Box(
                                modifier = Modifier
                                    .then(if (isLandscape) Modifier else Modifier.minimumInteractiveComponentSize())
                                    .size(if (isLandscape) 28.dp else 34.dp)
                                    .clip(RoundedCornerShape(playCornerPercent))
                                    .clickable(
                                        interactionSource = playInteractionSource,
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = stringResource(if (isPlaying) R.string.video_cd_pause else R.string.video_cd_play),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(if (isLandscape) 18.dp else 22.dp)
                                )
                            }

                            // 2. Scrubber interactivo con marcas de tiempo (Espacio horizontal maximizado)
                            ExpressiveVideoProgressBar(
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onSeek = { seekPosMs ->
                                    exoPlayer.seekTo(seekPosMs)
                                    currentPositionMs = seekPosMs
                                    onPositionChanged(seekPosMs)
                                },
                                isCompact = isLandscape,
                                modifier = Modifier.weight(1f)
                            )

                            // 3. Botón de Velocidad Cíclica (A la izquierda del Mute)
                            val speedInteractionSource = remember { MutableInteractionSource() }
                            val isSpeedPressed by speedInteractionSource.collectIsPressedAsState()
                            val speedCornerPercent by animateIntAsState(
                                targetValue = if (isSpeedPressed) 26 else 50,
                                animationSpec = motionScheme.fastSpatialSpec(),
                                label = "speedCorner"
                            )
                            Box(
                                modifier = Modifier
                                    .then(if (isLandscape) Modifier else Modifier.minimumInteractiveComponentSize())
                                    .size(if (isLandscape) 28.dp else 32.dp)
                                    .clip(RoundedCornerShape(speedCornerPercent))
                                    .clickable(
                                        interactionSource = speedInteractionSource,
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.5f
                                            1.5f -> 2.0f
                                            2.0f -> 0.5f
                                            else -> 1.0f
                                        }
                                        exoPlayer.setPlaybackSpeed(playbackSpeed)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = speedText,
                                    style = TextStyle(
                                        fontSize = if (isLandscape) 10.sp else 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            // 4. Botón Mute / Unmute (Extremo Derecho)
                            val muteInteractionSource = remember { MutableInteractionSource() }
                            val isMutePressed by muteInteractionSource.collectIsPressedAsState()
                            val muteCornerPercent by animateIntAsState(
                                targetValue = if (isMutePressed) 26 else 50,
                                animationSpec = motionScheme.fastSpatialSpec(),
                                label = "muteCorner"
                            )
                            Box(
                                modifier = Modifier
                                    .then(if (isLandscape) Modifier else Modifier.minimumInteractiveComponentSize())
                                    .size(if (isLandscape) 28.dp else 32.dp)
                                    .clip(RoundedCornerShape(muteCornerPercent))
                                    .clickable(
                                        interactionSource = muteInteractionSource,
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val nextMuted = !isMuted
                                        isMuted = nextMuted
                                        exoPlayer.volume = if (nextMuted) 0f else 1f
                                        onToggleMute(nextMuted)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(if (isMuted) R.string.video_cd_unmute else R.string.video_cd_mute),
                                    tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Scrubber interactivo de video Material 3 Expressive con barra estática recta y drag handle vertical.
 * Diseñado conforme a las directrices de Material 3 Expressive:
 * - Pista activa e inactiva: Barras horizontales rectas con bordes redondeados (StrokeCap.Round) y grosor constante.
 * - Drag handle: Barra vertical redondeada (píldora) con respuesta táctil fluida (fastSpatialSpec) y halo de contraste.
 * - Cero asignaciones en memoria durante la fase de dibujo para garantizar 120 fps constantes durante el scrubbing.
 */
@Composable
private fun ExpressiveVideoProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val progressFraction = if (durationMs > 0L) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var isScrubbing by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val activeFraction = if (isScrubbing) dragFraction else progressFraction
    val motionScheme = MaterialTheme.nierikaMotionScheme

    // Dinámica expresiva del drag handle vertical al interactuar
    val thumbWidthDp by animateDpAsState(
        targetValue = if (isScrubbing) 6.dp else 4.5.dp,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "videoThumbWidth"
    )
    val thumbHeightDp by animateDpAsState(
        targetValue = if (isScrubbing) {
            if (isCompact) 20.dp else 24.dp
        } else {
            if (isCompact) 16.dp else 19.dp
        },
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "videoThumbHeight"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Marca de tiempo transcurrido (00:33)
        Text(
            text = formatDuration(if (isScrubbing) (dragFraction * durationMs).toLong() else currentPositionMs),
            style = TextStyle(
                fontSize = if (isCompact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val trackInactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        val shadowColor = Color.Black.copy(alpha = 0.4f)

        // Pista de progreso interactiva M3 Expressive
        Box(
            modifier = Modifier
                .weight(1f)
                .height(if (isCompact) 28.dp else 34.dp)
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isScrubbing = true
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            dragFraction = (offset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isScrubbing = false
                            if (durationMs > 0L) {
                                onSeek((dragFraction * durationMs).toLong())
                            }
                        },
                        onDragCancel = {
                            isScrubbing = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        }
                    )
                }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (durationMs > 0L) {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            val fraction = (offset.x / size.width.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                            onSeek((fraction * durationMs).toLong())
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp)
            ) {
                val width = size.width
                val height = size.height
                if (width <= 0f || height <= 0f) return@Canvas

                val centerY = height / 2f
                val activeWidth = (width * activeFraction).coerceIn(0f, width)
                val trackStrokeWidthPx = 4.dp.toPx()
                val thumbWidthPx = thumbWidthDp.toPx()
                val thumbHeightPx = thumbHeightDp.toPx()

                // 1. Pista inactiva recta
                if (activeWidth < width) {
                    drawLine(
                        color = trackInactiveColor,
                        start = Offset(activeWidth, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = trackStrokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // 2. Pista activa recta con color primario
                if (activeWidth > 0f) {
                    drawLine(
                        color = primaryColor,
                        start = Offset(0f, centerY),
                        end = Offset(activeWidth, centerY),
                        strokeWidth = trackStrokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }

                // 3. Drag Handle vertical M3 Expressive (píldora vertical con bordes redondeados)
                val minThumbX = (thumbWidthPx / 2f).coerceAtMost(width)
                val maxThumbX = (width - thumbWidthPx / 2f).coerceAtLeast(minThumbX)
                val clampedThumbX = if (maxThumbX >= minThumbX) {
                    activeWidth.coerceIn(minThumbX, maxThumbX)
                } else {
                    minThumbX
                }
                val halfThumbLength = (thumbHeightPx - thumbWidthPx).coerceAtLeast(0f) / 2f

                // Halo de contraste para máxima legibilidad sobre video
                drawLine(
                    color = shadowColor,
                    start = Offset(clampedThumbX, centerY - halfThumbLength),
                    end = Offset(clampedThumbX, centerY + halfThumbLength),
                    strokeWidth = thumbWidthPx + 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Drag handle vertical oficial
                drawLine(
                    color = primaryColor,
                    start = Offset(clampedThumbX, centerY - halfThumbLength),
                    end = Offset(clampedThumbX, centerY + halfThumbLength),
                    strokeWidth = thumbWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        // Marca de tiempo total (01:36)
        Text(
            text = formatDuration(durationMs),
            style = TextStyle(
                fontSize = if (isCompact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Formatea milisegundos a representación mm:ss o hh:mm:ss.
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}


