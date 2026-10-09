package com.ixeken.nierika.feature.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.editor.components.AdjustmentToolbar
import com.ixeken.nierika.feature.editor.components.CanvasExpansionToolbar
import com.ixeken.nierika.feature.editor.components.CropToolbar
import com.ixeken.nierika.feature.editor.components.DrawingToolbar
import com.ixeken.nierika.feature.editor.components.EditorBottomNavigation
import com.ixeken.nierika.feature.editor.components.NativeCropCanvas
import com.ixeken.nierika.feature.editor.components.NierikaCropView
import com.ixeken.nierika.feature.editor.components.VideoEditorCanvas
import com.ixeken.nierika.feature.editor.components.VideoSpeedToolbar
import com.ixeken.nierika.feature.editor.components.VideoTimelineTrimmer
import com.ixeken.nierika.feature.editor.components.VideoVolumeToolbar
import com.ixeken.nierika.feature.editor.components.MarkupToolbar
import com.ixeken.nierika.feature.editor.components.TextStickerEditorDialog
import com.ixeken.nierika.feature.editor.model.CropAspectRatio
import com.ixeken.nierika.feature.editor.model.EditorTool
import com.ixeken.nierika.feature.editor.model.MarkupSubTool
import com.ixeken.nierika.feature.editor.model.TextStickerData

/**
 * Pantalla principal del editor multimedia (Fotografía y Video) M3 Expressive.
 * Reutiliza la superficie inmersiva del visor para garantizar cero saltos visuales.
 */
@Composable
fun EditorScreen(
    mediaUri: String,
    isVideo: Boolean = false,
    onDismiss: () -> Unit,
    onSaveSuccess: (String) -> Unit = {},
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current

    // Sincronización inmediata al abrir el editor y limpieza de estado al salir
    DisposableEffect(mediaUri, isVideo) {
        viewModel.setMediaUri(mediaUri, isVideo)
        onDispose {
            viewModel.resetAllEditorState()
        }
    }

    var showDiscardConfirmationDialog by remember { mutableStateOf(false) }
    var cropImageViewRef by remember { mutableStateOf<com.canhub.cropper.CropImageView?>(null) }
    var isVideoPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableStateOf(0L) }
    var seekPositionMs by remember { mutableStateOf<Long?>(null) }
    var isTextEditorDialogVisible by remember { mutableStateOf(false) }
    var editingTextSticker by remember { mutableStateOf<TextStickerData?>(null) }
    var pendingVideoCropBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isCroppingPhoto by remember { mutableStateOf(false) }

    val handleExit = {
        if (viewModel.hasUnsavedChanges) {
            showDiscardConfirmationDialog = true
        } else {
            onDismiss()
        }
    }

    // Registro explícito del gesto de retroceso nativo
    BackHandler {
        handleExit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Barra superior estandarizada con acciones de cancelar (X) y guardar (Save) en contenedores estilizados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = handleExit,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (viewModel.hasUnsavedChanges) {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            pendingVideoCropBounds = null
                            currentPositionMs = 0L
                            seekPositionMs = 0L
                            viewModel.resetAllEditorState()
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(R.string.action_reset),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.action_reset),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                val context = LocalContext.current

                androidx.compose.material3.Button(
                    onClick = {
                        if (uiState.isVideo) {
                            isVideoPlaying = false
                            viewModel.exportVideo(
                                context = context,
                                onSaveSuccess = onSaveSuccess,
                                onSaveError = { errorMsg ->
                                    android.widget.Toast.makeText(
                                        context,
                                        "${context.getString(R.string.video_export_error)}: $errorMsg",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } else {
                            if (uiState.activeTool == EditorTool.CROP) {
                                isCroppingPhoto = true
                                coroutineScope.launch(Dispatchers.Default) {
                                    val cropped = try {
                                        cropImageViewRef?.getCroppedImage()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        null
                                    }
                                    withContext(Dispatchers.Main) {
                                        isCroppingPhoto = false
                                        viewModel.saveEditedImage(context, baseBitmap = cropped, onSaveSuccess = onSaveSuccess)
                                    }
                                }
                            } else {
                                viewModel.saveEditedImage(context, baseBitmap = uiState.croppedBitmap, onSaveSuccess = onSaveSuccess)
                            }
                        }
                    },
                    enabled = !uiState.isLoading && !uiState.isExportingVideo && !isCroppingPhoto,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(42.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 6.dp)
                ) {
                    if (uiState.isLoading || uiState.isExportingVideo) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.action_save),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Área central inmersiva con delimitación estricta (clipToBounds) para que el zoom nunca tape los botones
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isVideo) {
                    VideoEditorCanvas(
                        videoUri = uiState.mediaUri,
                        isPlaying = isVideoPlaying,
                        rotationDegrees = uiState.rotationDegrees,
                        isFlippedHorizontally = uiState.isFlippedHorizontally,
                        isFlippedVertically = uiState.isFlippedVertically,
                        aspectRatio = uiState.selectedAspectRatio,
                        colorMatrix = uiState.colorMatrix,
                        isCropMode = (uiState.activeTool == EditorTool.CROP),
                        trimStartMs = uiState.trimStartMs,
                        trimEndMs = uiState.trimEndMs,
                        volume = uiState.videoVolume,
                        isMuted = uiState.isVideoMuted,
                        speed = uiState.videoSpeed,
                        seekPositionMs = seekPositionMs,
                        isDrawingMode = (uiState.activeTool == EditorTool.MARKUP && uiState.markupSubTool == MarkupSubTool.DRAW),
                        drawingPaths = uiState.drawingPaths,
                        brushColor = uiState.brushColor,
                        strokeWidth = uiState.strokeWidth,
                        brushMode = uiState.brushMode,
                        isEraserActive = uiState.isEraserActive,
                        onAddDrawingPath = viewModel::addDrawingPath,
                        isTextMode = (uiState.activeTool == EditorTool.MARKUP && uiState.markupSubTool == MarkupSubTool.TEXT),
                        textStickers = uiState.textStickers,
                        onUpdateTextSticker = viewModel::addOrUpdateTextSticker,
                        onEditTextSticker = {
                            editingTextSticker = it
                            isTextEditorDialogVisible = true
                        },
                        onDeleteTextSticker = viewModel::removeTextSticker,
                        onDurationDiscovered = viewModel::setVideoDuration,
                        onPositionDiscovered = { pos -> currentPositionMs = pos },
                        videoCropBounds = uiState.videoCropBounds,
                        onCropBoundsChanged = { normRect -> pendingVideoCropBounds = normRect },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.activeTool == EditorTool.CROP) {
                    NierikaCropView(
                        mediaUri = mediaUri,
                        croppedBitmap = uiState.croppedBitmap,
                        aspectRatio = uiState.selectedAspectRatio,
                        customAspectRatioPair = uiState.customAspectRatioPair,
                        rotationDegrees = uiState.rotationDegrees,
                        isFlippedHorizontally = uiState.isFlippedHorizontally,
                        isFlippedVertically = uiState.isFlippedVertically,
                        onCropViewReady = { cropImageViewRef = it },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    NativeCropCanvas(
                        mediaUri = mediaUri,
                        croppedBitmap = uiState.croppedBitmap,
                        rotationDegrees = if (uiState.croppedBitmap != null) 0f else uiState.rotationDegrees,
                        isFlippedHorizontally = if (uiState.croppedBitmap != null) false else uiState.isFlippedHorizontally,
                        isFlippedVertically = if (uiState.croppedBitmap != null) false else uiState.isFlippedVertically,
                        aspectRatio = if (uiState.croppedBitmap != null) CropAspectRatio.FREE else uiState.selectedAspectRatio,
                        colorMatrix = uiState.colorMatrix,
                        isCropMode = false,
                        isCanvasExpansionMode = (uiState.activeTool == EditorTool.CANVAS),
                        isCanvasEnabled = uiState.isCanvasEnabled,
                        canvasPaddingX = uiState.canvasPaddingX,
                        canvasPaddingY = uiState.canvasPaddingY,
                        canvasBgMode = uiState.canvasBgMode,
                        canvasBlurRadius = uiState.canvasBlurRadius,
                        canvasSolidColor = uiState.canvasSolidColor,
                        isDrawingMode = (uiState.activeTool == EditorTool.MARKUP && uiState.markupSubTool == MarkupSubTool.DRAW),
                        drawingPaths = uiState.drawingPaths,
                        brushColor = uiState.brushColor,
                        strokeWidth = uiState.strokeWidth,
                        brushMode = uiState.brushMode,
                        isEraserActive = uiState.isEraserActive,
                        onAddDrawingPath = viewModel::addDrawingPath,
                        isTextMode = (uiState.activeTool == EditorTool.MARKUP && uiState.markupSubTool == MarkupSubTool.TEXT),
                        textStickers = uiState.textStickers,
                        onUpdateTextSticker = viewModel::addOrUpdateTextSticker,
                        onEditTextSticker = {
                            editingTextSticker = it
                            isTextEditorDialogVisible = true
                        },
                        onDeleteTextSticker = viewModel::removeTextSticker,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Panel de herramientas dinámico con animación M3 Expressive
            AnimatedContent(
                targetState = uiState.activeTool,
                transitionSpec = {
                    fadeIn(animationSpec = motionScheme.fastEffectsSpec()) togetherWith
                            fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                },
                label = "toolPanelAnimation"
            ) { tool ->
                when (tool) {
                    EditorTool.TRIM -> {
                        VideoTimelineTrimmer(
                            videoUri = uiState.mediaUri,
                            durationMs = uiState.videoDurationMs,
                            trimStartMs = uiState.trimStartMs,
                            trimEndMs = uiState.trimEndMs,
                            currentPositionMsProvider = { currentPositionMs },
                            isPlaying = isVideoPlaying,
                            onTogglePlayPause = { isVideoPlaying = !isVideoPlaying },
                            onTrimRangeChange = viewModel::setTrimRange,
                            onSeekTo = { pos -> seekPositionMs = pos }
                        )
                    }

                    EditorTool.VOLUME -> {
                        VideoVolumeToolbar(
                            volume = uiState.videoVolume,
                            isMuted = uiState.isVideoMuted,
                            onVolumeChange = viewModel::setVideoVolume,
                            onMuteToggle = viewModel::setVideoMuted,
                            onDone = { viewModel.setActiveTool(EditorTool.TRIM) }
                        )
                    }

                    EditorTool.SPEED -> {
                        VideoSpeedToolbar(
                            speed = uiState.videoSpeed,
                            onSpeedChange = viewModel::setVideoSpeed,
                            onDone = { viewModel.setActiveTool(EditorTool.TRIM) }
                        )
                    }

                    EditorTool.CROP -> {
                        CropToolbar(
                            selectedAspectRatio = uiState.selectedAspectRatio,
                            customAspectRatioPair = uiState.customAspectRatioPair,
                            onSelectAspectRatio = viewModel::setAspectRatio,
                            onSelectCustomAspectRatio = viewModel::setCustomAspectRatio,
                            onRotateClick = viewModel::rotate90Degrees,
                            onFlipHorizontalClick = viewModel::toggleFlipHorizontal,
                            onFlipVerticalClick = viewModel::toggleFlipVertical,
                            onResetClick = {
                                pendingVideoCropBounds = null
                                viewModel.resetTransformations()
                            },
                            onConfirmCropClick = {
                                if (uiState.isVideo) {
                                    viewModel.setVideoCropBounds(pendingVideoCropBounds)
                                    viewModel.setActiveTool(EditorTool.TRIM)
                                } else {
                                    isCroppingPhoto = true
                                    coroutineScope.launch(Dispatchers.Default) {
                                        val cropped = try {
                                            cropImageViewRef?.getCroppedImage()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            null
                                        }
                                        withContext(Dispatchers.Main) {
                                            viewModel.applyCroppedBitmap(cropped)
                                            isCroppingPhoto = false
                                        }
                                    }
                                }
                            }
                        )
                    }

                    EditorTool.ADJUST -> {
                        AdjustmentToolbar(
                            brightness = uiState.brightness,
                            contrast = uiState.contrast,
                            saturation = uiState.saturation,
                            highlights = uiState.highlights,
                            shadows = uiState.shadows,
                            temperature = uiState.temperature,
                            onBrightnessChange = viewModel::setBrightness,
                            onContrastChange = viewModel::setContrast,
                            onSaturationChange = viewModel::setSaturation,
                            onHighlightsChange = viewModel::setHighlights,
                            onShadowsChange = viewModel::setShadows,
                            onTemperatureChange = viewModel::setTemperature,
                            onResetAdjustments = viewModel::resetAdjustments
                        )
                    }

                    EditorTool.CANVAS -> {
                        CanvasExpansionToolbar(
                            paddingX = uiState.canvasPaddingX,
                            paddingY = uiState.canvasPaddingY,
                            bgMode = uiState.canvasBgMode,
                            blurRadius = uiState.canvasBlurRadius,
                            selectedSolidColor = uiState.canvasSolidColor,
                            onPaddingXChange = viewModel::setCanvasPaddingX,
                            onPaddingYChange = viewModel::setCanvasPaddingY,
                            onSelectBgMode = viewModel::setCanvasBgMode,
                            onBlurRadiusChange = viewModel::setCanvasBlurRadius,
                            onSolidColorChange = viewModel::setCanvasSolidColor,
                            onResetCanvas = viewModel::resetCanvas
                        )
                    }

                    EditorTool.MARKUP -> {
                        MarkupToolbar(
                            selectedSubTool = uiState.markupSubTool,
                            onSelectSubTool = viewModel::setMarkupSubTool,
                            isVideo = uiState.isVideo,
                            brushMode = uiState.brushMode,
                            isEraserActive = uiState.isEraserActive,
                            strokeWidth = uiState.strokeWidth,
                            brushColor = uiState.brushColor,
                            canUndo = uiState.canUndo,
                            canRedo = uiState.canRedo,
                            hasStrokes = uiState.drawingPaths.isNotEmpty(),
                            onSelectBrushMode = viewModel::setBrushMode,
                            onToggleEraser = viewModel::setEraserActive,
                            onStrokeWidthChange = viewModel::setStrokeWidth,
                            onSelectColor = viewModel::setBrushColor,
                            onUndoClick = viewModel::undoDrawing,
                            onRedoClick = viewModel::redoDrawing,
                            onClearClick = viewModel::clearDrawing,
                            textStickers = uiState.textStickers,
                            onAddNewTextClick = {
                                editingTextSticker = null
                                isTextEditorDialogVisible = true
                            },
                            onDoneClick = {
                                val defaultTool = if (uiState.isVideo) EditorTool.TRIM else EditorTool.CROP
                                viewModel.setActiveTool(defaultTool)
                            }
                        )
                    }
                }
            }

            // Barra inferior de navegación entre herramientas de edición
            EditorBottomNavigation(
                activeTool = uiState.activeTool,
                onSelectTool = { nextTool ->
                    if (!uiState.isVideo && uiState.activeTool == EditorTool.CROP && nextTool != EditorTool.CROP) {
                        isCroppingPhoto = true
                        coroutineScope.launch(Dispatchers.Default) {
                            val cropped = try {
                                cropImageViewRef?.getCroppedImage()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                            withContext(Dispatchers.Main) {
                                if (cropped != null) {
                                    viewModel.setCroppedBitmap(cropped)
                                }
                                viewModel.setActiveTool(nextTool)
                                isCroppingPhoto = false
                            }
                        }
                    } else {
                        viewModel.setActiveTool(nextTool)
                    }
                },
                isVideo = uiState.isVideo
            )
        }

        if (isTextEditorDialogVisible) {
            TextStickerEditorDialog(
                initialSticker = editingTextSticker,
                onDismiss = {
                    isTextEditorDialogVisible = false
                    editingTextSticker = null
                },
                onConfirm = { sticker ->
                    viewModel.addOrUpdateTextSticker(sticker)
                    isTextEditorDialogVisible = false
                    editingTextSticker = null
                }
            )
        }

        if (uiState.isExportingVideo) {
            com.ixeken.nierika.feature.editor.components.VideoExportProgressDialog(
                progress = uiState.exportProgress,
                onCancel = viewModel::cancelVideoExport
            )
        }

        if (showDiscardConfirmationDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDiscardConfirmationDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.editor_discard_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.editor_discard_dialog_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showDiscardConfirmationDialog = false
                            viewModel.resetAllEditorState()
                            onDismiss()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.editor_discard_action_discard),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showDiscardConfirmationDialog = false }
                    ) {
                        Text(
                            text = stringResource(R.string.editor_discard_action_keep_editing),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
        if (isCroppingPhoto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
