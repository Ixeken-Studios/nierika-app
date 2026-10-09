package com.ixeken.nierika.feature.editor

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ixeken.nierika.feature.editor.model.CanvasBackgroundMode
import com.ixeken.nierika.feature.editor.model.CropAspectRatio
import com.ixeken.nierika.feature.editor.model.DrawPathData
import com.ixeken.nierika.feature.editor.model.EditorTool
import com.ixeken.nierika.feature.editor.model.MarkupSubTool
import com.ixeken.nierika.feature.editor.util.ColorMatrixUtils
import com.ixeken.nierika.feature.editor.util.EditorImageExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import javax.inject.Inject

/**
 * Estado inmutable de la interfaz del editor de fotografías M3 Expressive.
 */
@Immutable
data class EditorUiState(
    val mediaUri: String = "",
    val isVideo: Boolean = false,
    val isLoading: Boolean = false,
    val activeTool: EditorTool = EditorTool.CROP,
    val markupSubTool: MarkupSubTool = MarkupSubTool.DRAW,
    val rotationDegrees: Float = 0f,
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
    val selectedAspectRatio: CropAspectRatio = CropAspectRatio.FREE,
    val customAspectRatioPair: Pair<Float, Float>? = null,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val temperature: Float = 0f,
    val isCanvasEnabled: Boolean = false,
    val canvasPaddingX: Float = 0f,
    val canvasPaddingY: Float = 0f,
    val canvasBgMode: CanvasBackgroundMode = CanvasBackgroundMode.BLUR,
    val canvasBlurRadius: Float = 25f,
    val canvasSolidColor: Color = Color.Black,
    val croppedBitmap: Bitmap? = null,
    val drawingPaths: List<DrawPathData> = emptyList(),
    val undoStack: List<List<DrawPathData>> = emptyList(),
    val redoStack: List<List<DrawPathData>> = emptyList(),
    val brushColor: Color = Color.Red,
    val strokeWidth: Float = 10f,
    val brushMode: com.ixeken.nierika.feature.editor.model.DrawBrushMode = com.ixeken.nierika.feature.editor.model.DrawBrushMode.PEN,
    val isEraserActive: Boolean = false,
    val textStickers: List<com.ixeken.nierika.feature.editor.model.TextStickerData> = emptyList(),
    val videoDurationMs: Long = 0L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val videoCropBounds: Rect? = null,
    val videoVolume: Float = 1.0f,
    val isVideoMuted: Boolean = false,
    val videoSpeed: Float = 1.0f,
    val exportProgress: Float = 0f,
    val isExportingVideo: Boolean = false
) {
    /**
     * Indica si la pila de acciones permite ejecutar la operación Deshacer (Undo).
     */
    val canUndo: Boolean get() = drawingPaths.isNotEmpty() || undoStack.isNotEmpty()

    /**
     * Indica si la pila de acciones permite ejecutar la operación Rehacer (Redo).
     */
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /**
     * Calcula la matriz de color resultante combinando los valores de brillo, contraste, saturación, luces, sombras y temperatura.
     */
    val colorMatrix: ColorMatrix
        get() = ColorMatrixUtils.createColorMatrix(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            highlights = highlights,
            shadows = shadows,
            temperature = temperature
        )
}

/**
 * ViewModel que administra las operaciones de transformación, recorte, expansión de lienzo, dibujo e historial (Undo/Redo).
 * Mantiene el estado desarticulado de la interfaz gráfica siguiendo los principios de Compose Stateless.
 */
@HiltViewModel
class EditorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    /**
     * Determina si el usuario ha realizado modificaciones no guardadas en la imagen o video.
     */
    val hasUnsavedChanges: Boolean
        get() {
            val s = _uiState.value
            return s.rotationDegrees != 0f ||
                    s.isFlippedHorizontally ||
                    s.isFlippedVertically ||
                    s.selectedAspectRatio != CropAspectRatio.FREE ||
                    s.brightness != 0f ||
                    s.contrast != 1f ||
                    s.saturation != 1f ||
                    s.highlights != 0f ||
                    s.shadows != 0f ||
                    s.temperature != 0f ||
                    s.croppedBitmap != null ||
                    s.videoCropBounds != null ||
                    s.drawingPaths.isNotEmpty() ||
                    s.textStickers.isNotEmpty() ||
                    s.isCanvasEnabled ||
                    (s.isVideo && (s.trimStartMs > 0L || (s.trimEndMs > 0L && (s.videoDurationMs == 0L || s.trimEndMs < s.videoDurationMs)) || s.isVideoMuted || s.videoVolume != 1.0f || s.videoSpeed != 1.0f))
        }

    /**
     * Limpia y restablece todo el estado de edición para descartar cualquier modificación pendiente,
     * preservando la duración del video e inicializando los límites de recorte en el inicio y final completos.
     */
    fun resetAllEditorState() {
        val currentUri = _uiState.value.mediaUri
        val isVideo = _uiState.value.isVideo
        val currentDuration = _uiState.value.videoDurationMs
        _uiState.value = EditorUiState(
            mediaUri = currentUri,
            isVideo = isVideo,
            videoDurationMs = currentDuration,
            trimStartMs = 0L,
            trimEndMs = currentDuration,
            activeTool = if (isVideo) EditorTool.TRIM else EditorTool.CROP
        )
    }

    /**
     * Establece la URI y tipo de recurso multimedia (fotografía o video) sobre el que se aplicará la edición.
     */
    fun setMediaUri(uri: String, isVideo: Boolean = false) {
        val initialTool = if (isVideo) EditorTool.TRIM else EditorTool.CROP
        if (_uiState.value.mediaUri != uri) {
            _uiState.value = EditorUiState(mediaUri = uri, isVideo = isVideo, activeTool = initialTool)
        } else {
            _uiState.value = _uiState.value.copy(isVideo = isVideo, activeTool = initialTool)
        }
    }

    /**
     * Cambia la herramienta/pestaña activa en el editor.
     */
    fun setActiveTool(tool: EditorTool) {
        _uiState.value = _uiState.value.copy(activeTool = tool)
    }

    /**
     * Alterna la subherramienta activa (Dibujo / Texto) dentro de la suite Markup.
     */
    fun setMarkupSubTool(subTool: MarkupSubTool) {
        _uiState.value = _uiState.value.copy(markupSubTool = subTool)
    }

    /**
     * Aplica rotación incremental de noventa grados en sentido horario.
     */
    fun rotate90Degrees() {
        val nextRotation = (_uiState.value.rotationDegrees + 90f) % 360f
        _uiState.value = _uiState.value.copy(rotationDegrees = nextRotation)
    }

    /**
     * Alterna el reflejo especular en el eje horizontal.
     */
    fun toggleFlipHorizontal() {
        _uiState.value = _uiState.value.copy(
            isFlippedHorizontally = !_uiState.value.isFlippedHorizontally
        )
    }

    /**
     * Alterna el reflejo especular en el eje vertical.
     */
    fun toggleFlipVertical() {
        _uiState.value = _uiState.value.copy(
            isFlippedVertically = !_uiState.value.isFlippedVertically
        )
    }

    /**
     * Actualiza la proporción de aspecto activa para el marco de recorte.
     */
    fun setAspectRatio(aspectRatio: CropAspectRatio) {
        _uiState.value = _uiState.value.copy(selectedAspectRatio = aspectRatio)
    }

    /**
     * Establece una relación de aspecto personalizada (X : Y).
     */
    fun setCustomAspectRatio(pair: Pair<Float, Float>) {
        _uiState.value = _uiState.value.copy(
            selectedAspectRatio = CropAspectRatio.OTHER,
            customAspectRatioPair = pair
        )
    }

    /**
     * Modifica el nivel de brillo de la imagen (-1.0 a 1.0).
     */
    fun setBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value.coerceIn(-1f, 1f))
    }

    /**
     * Modifica el factor de contraste de la imagen (0.2 a 2.0).
     */
    fun setContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value.coerceIn(0.2f, 2f))
    }

    /**
     * Modifica la intensidad de saturación de color (0.0 a 2.0).
     */
    fun setSaturation(value: Float) {
        _uiState.value = _uiState.value.copy(saturation = value.coerceIn(0f, 2f))
    }

    /**
     * Modifica el ajuste de altas luces (-1.0 a 1.0).
     */
    fun setHighlights(value: Float) {
        _uiState.value = _uiState.value.copy(highlights = value.coerceIn(-1f, 1f))
    }

    /**
     * Modifica el ajuste de sombras (-1.0 a 1.0).
     */
    fun setShadows(value: Float) {
        _uiState.value = _uiState.value.copy(shadows = value.coerceIn(-1f, 1f))
    }

    /**
     * Modifica la temperatura de color de la fotografía (-1.0 a 1.0).
     */
    fun setTemperature(value: Float) {
        _uiState.value = _uiState.value.copy(temperature = value.coerceIn(-1f, 1f))
    }

    /**
     * Modifica el margen o expansión del lienzo en el eje horizontal X (0.0 a 1.0).
     */
    fun setCanvasPaddingX(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(
            canvasPaddingX = clamped,
            isCanvasEnabled = (clamped > 0f || _uiState.value.canvasPaddingY > 0f)
        )
    }

    /**
     * Modifica el margen o expansión del lienzo en el eje vertical Y (0.0 a 1.0).
     */
    fun setCanvasPaddingY(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _uiState.value = _uiState.value.copy(
            canvasPaddingY = clamped,
            isCanvasEnabled = (clamped > 0f || _uiState.value.canvasPaddingX > 0f)
        )
    }

    /**
     * Modifica el modo de fondo para la expansión del lienzo (Desenfoque Gaussiano o Color Sólido).
     */
    fun setCanvasBgMode(mode: CanvasBackgroundMode) {
        _uiState.value = _uiState.value.copy(canvasBgMode = mode)
    }

    /**
     * Modifica el radio de desenfoque gaussiano del fondo (1.0 a 50.0).
     */
    fun setCanvasBlurRadius(radius: Float) {
        _uiState.value = _uiState.value.copy(canvasBlurRadius = radius.coerceIn(1f, 50f))
    }

    /**
     * Modifica el color sólido de fondo del lienzo extendido.
     */
    fun setCanvasSolidColor(color: Color) {
        _uiState.value = _uiState.value.copy(
            canvasSolidColor = color,
            canvasBgMode = CanvasBackgroundMode.SOLID
        )
    }

    /**
     * Restablece la configuración del lienzo extendido desactivándolo por completo.
     */
    fun resetCanvas() {
        _uiState.value = _uiState.value.copy(
            isCanvasEnabled = false,
            canvasPaddingX = 0f,
            canvasPaddingY = 0f,
            canvasBgMode = CanvasBackgroundMode.BLUR,
            canvasBlurRadius = 25f,
            canvasSolidColor = Color.Black
        )
    }

    /**
     * Agrega un nuevo trazo a mano alzada a la lista actual y actualiza la pila de Undo.
     */
    fun addDrawingPath(pathData: DrawPathData) {
        val currentPaths = _uiState.value.drawingPaths
        val newUndoStack = _uiState.value.undoStack + listOf(currentPaths)
        val newPaths = currentPaths + pathData
        _uiState.value = _uiState.value.copy(
            drawingPaths = newPaths,
            undoStack = newUndoStack,
            redoStack = emptyList()
        )
    }

    /**
     * Ejecuta la operación Deshacer (Undo), revirtiendo al estado anterior de trazos.
     */
    fun undoDrawing() {
        val currentPaths = _uiState.value.drawingPaths
        val undoStack = _uiState.value.undoStack

        if (currentPaths.isNotEmpty()) {
            val previousPaths = if (undoStack.isNotEmpty()) undoStack.last() else emptyList()
            val newUndoStack = if (undoStack.isNotEmpty()) undoStack.dropLast(1) else emptyList()
            val newRedoStack = _uiState.value.redoStack + listOf(currentPaths)

            _uiState.value = _uiState.value.copy(
                drawingPaths = previousPaths,
                undoStack = newUndoStack,
                redoStack = newRedoStack
            )
        }
    }

    /**
     * Ejecuta la operación Rehacer (Redo), aplicando nuevamente la última acción revertida.
     */
    fun redoDrawing() {
        val redoStack = _uiState.value.redoStack
        if (redoStack.isNotEmpty()) {
            val nextPaths = redoStack.last()
            val newRedoStack = redoStack.dropLast(1)
            val newUndoStack = _uiState.value.undoStack + listOf(_uiState.value.drawingPaths)

            _uiState.value = _uiState.value.copy(
                drawingPaths = nextPaths,
                undoStack = newUndoStack,
                redoStack = newRedoStack
            )
        }
    }

    /**
     * Elimina todos los trazos realizados sobre el lienzo y guarda el historial.
     */
    fun clearDrawing() {
        if (_uiState.value.drawingPaths.isNotEmpty()) {
            val newUndoStack = _uiState.value.undoStack + listOf(_uiState.value.drawingPaths)
            _uiState.value = _uiState.value.copy(
                drawingPaths = emptyList(),
                undoStack = newUndoStack,
                redoStack = emptyList()
            )
        }
    }

    /**
     * Selecciona el color para el trazo del pincel.
     */
    fun setBrushColor(color: Color) {
        _uiState.value = _uiState.value.copy(
            brushColor = color,
            brushMode = com.ixeken.nierika.feature.editor.model.DrawBrushMode.PEN,
            isEraserActive = false
        )
    }

    /**
     * Modifica el grosor del pincel, pixelador o borrador (4.0 a 40.0).
     */
    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(strokeWidth = width.coerceIn(4f, 40f))
    }

    /**
     * Establece el modo activo del pincel (Pluma, Pixelar o Borrador).
     */
    fun setBrushMode(mode: com.ixeken.nierika.feature.editor.model.DrawBrushMode) {
        _uiState.value = _uiState.value.copy(
            brushMode = mode,
            isEraserActive = (mode == com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER)
        )
    }

    /**
     * Alterna entre el modo Borrador y Pincel.
     */
    fun setEraserActive(active: Boolean) {
        setBrushMode(if (active) com.ixeken.nierika.feature.editor.model.DrawBrushMode.ERASER else com.ixeken.nierika.feature.editor.model.DrawBrushMode.PEN)
    }

    /**
     * Agrega o actualiza una capa de texto flotante en la imagen/video.
     */
    fun addOrUpdateTextSticker(sticker: com.ixeken.nierika.feature.editor.model.TextStickerData) {
        val current = _uiState.value.textStickers
        val index = current.indexOfFirst { it.id == sticker.id }
        val updated = if (index >= 0) {
            current.toMutableList().apply { set(index, sticker) }
        } else {
            current + sticker
        }
        _uiState.value = _uiState.value.copy(textStickers = updated)
    }

    /**
     * Elimina una capa de texto flotante por su identificador.
     */
    fun removeTextSticker(id: String) {
        _uiState.value = _uiState.value.copy(
            textStickers = _uiState.value.textStickers.filterNot { it.id == id }
        )
    }

    /**
     * Registra la duración total del video en milisegundos e inicializa los puntos de recorte si es necesario.
     */
    fun setVideoDuration(durationMs: Long) {
        if (durationMs <= 0L) return
        val currentDuration = _uiState.value.videoDurationMs
        val currentEnd = _uiState.value.trimEndMs
        val currentStart = _uiState.value.trimStartMs

        // Evitar sobreescribir puntos de recorte ya editados por el usuario si la duración ya fue registrada
        if (currentDuration == durationMs && currentEnd > 0L) return

        val newEnd = if (currentEnd <= 0L || currentEnd > durationMs) durationMs else currentEnd
        val newStart = currentStart.coerceIn(0L, (newEnd - 100L).coerceAtLeast(0L))
        _uiState.value = _uiState.value.copy(
            videoDurationMs = durationMs,
            trimStartMs = newStart,
            trimEndMs = newEnd
        )
    }

    /**
     * Actualiza el rango de tiempo recortado [startMs, endMs] del video.
     */
    fun setTrimRange(startMs: Long, endMs: Long) {
        val maxDuration = _uiState.value.videoDurationMs
        val safeDuration = if (maxDuration > 0L) maxDuration else Long.MAX_VALUE
        val safeStart = startMs.coerceIn(0L, safeDuration)
        val safeEnd = endMs.coerceIn(safeStart, safeDuration)
        _uiState.value = _uiState.value.copy(
            trimStartMs = safeStart,
            trimEndMs = safeEnd
        )
    }

    /**
     * Ajusta el nivel de volumen del canal de audio original (0.0f a 1.0f).
     */
    fun setVideoVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(videoVolume = volume.coerceIn(0f, 1f))
    }

    /**
     * Silencia o activa el sonido original del video.
     */
    fun setVideoMuted(muted: Boolean) {
        _uiState.value = _uiState.value.copy(isVideoMuted = muted)
    }

    /**
     * Modifica la velocidad de reproducción del video (0.25x a 4.0x).
     */
    fun setVideoSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(videoSpeed = speed.coerceIn(0.25f, 4.0f))
    }

    /**
     * Inicia el proceso de exportación y recodificación de video mediante Media3 Transformer.
     */
    fun exportVideo(
        context: Context,
        onSaveSuccess: (String) -> Unit,
        onSaveError: ((String) -> Unit)? = null
    ) {
        _uiState.value = _uiState.value.copy(isExportingVideo = true, exportProgress = 0f)
        viewModelScope.launch {
            val result = com.ixeken.nierika.feature.editor.util.EditorVideoExporter.exportVideo(
                context = context,
                state = _uiState.value,
                onProgress = { progress ->
                    _uiState.value = _uiState.value.copy(exportProgress = progress)
                }
            )
            _uiState.value = _uiState.value.copy(isExportingVideo = false)
            result.onSuccess { outputUri ->
                onSaveSuccess(outputUri)
            }.onFailure { error ->
                onSaveError?.invoke(error.localizedMessage ?: "Error al exportar video")
            }
        }
    }

    /**
     * Cancela la tarea de exportación de video actual.
     */
    fun cancelVideoExport() {
        _uiState.value = _uiState.value.copy(isExportingVideo = false, exportProgress = 0f)
    }

    /**
     * Restablece únicamente los ajustes técnicos de color a sus valores neutros.
     */
    fun resetAdjustments() {
        _uiState.value = _uiState.value.copy(
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            highlights = 0f,
            shadows = 0f,
            temperature = 0f
        )
    }

    /**
     * Establece directamente el bitmap recortado sin forzar el cambio de herramienta activa.
     */
    fun setCroppedBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            _uiState.value = _uiState.value.copy(croppedBitmap = bitmap)
        }
    }

    /**
     * Aplica el bitmap recortado generado por el motor de recorte táctil y avanza a la pestaña de ajustes.
     */
    fun applyCroppedBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            _uiState.value = _uiState.value.copy(
                croppedBitmap = bitmap,
                rotationDegrees = 0f,
                isFlippedHorizontally = false,
                isFlippedVertically = false,
                activeTool = EditorTool.ADJUST
            )
        }
    }

    /**
     * Establece los límites normalizados [0f..1f] de recorte de video.
     */
    fun setVideoCropBounds(rect: Rect?) {
        _uiState.value = _uiState.value.copy(videoCropBounds = rect)
    }

    /**
     * Ejecuta el proceso de exportación y guardado de la fotografía editada en segundo plano (Dispatchers.IO).
     * Copia metadatos EXIF de origen y notifica el resultado mediante el callback onSaveSuccess.
     */
    fun saveEditedImage(
        context: Context,
        baseBitmap: Bitmap? = null,
        onSaveSuccess: (String) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val bitmapToExport = baseBitmap ?: _uiState.value.croppedBitmap
            val result = EditorImageExporter.saveEditedImage(context, _uiState.value, bitmapToExport)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.onSuccess { savedUri ->
                onSaveSuccess(savedUri)
            }
        }
    }

    /**
     * Restablece todas las transformaciones aplicadas a los valores por defecto.
     */
    fun resetTransformations() {
        _uiState.value = _uiState.value.copy(
            rotationDegrees = 0f,
            isFlippedHorizontally = false,
            isFlippedVertically = false,
            selectedAspectRatio = CropAspectRatio.FREE,
            videoCropBounds = null
        )
    }
}
