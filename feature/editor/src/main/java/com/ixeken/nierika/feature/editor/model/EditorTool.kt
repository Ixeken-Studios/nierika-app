package com.ixeken.nierika.feature.editor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.ixeken.nierika.core.designsystem.R

/**
 * Representa las herramientas de edición disponibles en las pestañas principales del editor multimedia.
 */
enum class EditorTool(val icon: ImageVector, val titleResId: Int) {
    TRIM(Icons.Filled.ContentCut, R.string.video_tool_trim),
    CROP(Icons.Filled.Crop, R.string.editor_tool_crop),
    ADJUST(Icons.Filled.Tune, R.string.editor_tool_adjust),
    VOLUME(Icons.AutoMirrored.Filled.VolumeUp, R.string.video_volume),
    SPEED(Icons.Filled.Speed, R.string.video_cd_speed),
    CANVAS(Icons.Filled.AspectRatio, R.string.editor_tool_canvas),
    MARKUP(Icons.Filled.Draw, R.string.editor_tool_markup)
}
