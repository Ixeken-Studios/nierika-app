package com.ixeken.nierika.feature.editor.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedButtonGroup
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.feature.editor.model.DrawBrushMode
import com.ixeken.nierika.feature.editor.model.MarkupSubTool
import com.ixeken.nierika.feature.editor.model.TextStickerData

/**
 * Barra de herramientas unificada para Markup (Marcado), permitiendo alternar
 * de manera fluida entre Dibujo a mano alzada (Draw) y Texto flotante (Text).
 */
@Composable
fun MarkupToolbar(
    selectedSubTool: MarkupSubTool,
    onSelectSubTool: (MarkupSubTool) -> Unit,
    isVideo: Boolean,
    // Parámetros de Dibujo
    brushMode: DrawBrushMode,
    isEraserActive: Boolean,
    strokeWidth: Float,
    brushColor: Color,
    canUndo: Boolean,
    canRedo: Boolean,
    hasStrokes: Boolean,
    onSelectBrushMode: (DrawBrushMode) -> Unit,
    onToggleEraser: (Boolean) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onSelectColor: (Color) -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onClearClick: () -> Unit,
    // Parámetros de Texto
    textStickers: List<TextStickerData>,
    onAddNewTextClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val drawLabel = stringResource(R.string.editor_tool_draw)
        val textLabel = stringResource(R.string.editor_tool_text)

        NierikaConnectedButtonGroup(
            items = MarkupSubTool.entries,
            selectedItem = selectedSubTool,
            onItemSelected = onSelectSubTool,
            labelProvider = { if (it == MarkupSubTool.DRAW) drawLabel else textLabel },
            iconProvider = { it.icon },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = selectedSubTool,
            transitionSpec = {
                fadeIn(animationSpec = motionScheme.fastEffectsSpec()) togetherWith
                        fadeOut(animationSpec = motionScheme.fastEffectsSpec())
            },
            label = "markupSubToolAnimation"
        ) { subTool ->
            when (subTool) {
                MarkupSubTool.DRAW -> {
                    DrawingToolbar(
                        brushMode = brushMode,
                        isEraserActive = isEraserActive,
                        strokeWidth = strokeWidth,
                        selectedColor = brushColor,
                        canUndo = canUndo,
                        canRedo = canRedo,
                        hasStrokes = hasStrokes,
                        isVideo = isVideo,
                        onSelectBrushMode = onSelectBrushMode,
                        onToggleEraser = onToggleEraser,
                        onStrokeWidthChange = onStrokeWidthChange,
                        onSelectColor = onSelectColor,
                        onUndoClick = onUndoClick,
                        onRedoClick = onRedoClick,
                        onClearClick = onClearClick
                    )
                }

                MarkupSubTool.TEXT -> {
                    TextToolbar(
                        textStickers = textStickers,
                        onAddNewTextClick = onAddNewTextClick,
                        onDoneClick = onDoneClick
                    )
                }
            }
        }
    }
}
