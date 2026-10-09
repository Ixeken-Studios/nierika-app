package com.ixeken.nierika.feature.editor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Title
import androidx.compose.ui.graphics.vector.ImageVector
import com.ixeken.nierika.core.designsystem.R

/**
 * Subherramientas disponibles dentro de la suite unificada de Marcado / Markup.
 */
enum class MarkupSubTool(val icon: ImageVector, val titleResId: Int) {
    DRAW(Icons.Filled.Draw, R.string.editor_tool_draw),
    TEXT(Icons.Filled.Title, R.string.editor_tool_text)
}
