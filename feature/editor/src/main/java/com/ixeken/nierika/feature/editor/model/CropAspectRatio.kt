package com.ixeken.nierika.feature.editor.model

import com.ixeken.nierika.core.designsystem.R

/**
 * Representa las proporciones de aspecto de recorte disponibles en la suite de edición.
 */
enum class CropAspectRatio(val ratio: Float?, val labelResId: Int) {
    FREE(null, R.string.crop_aspect_free),
    RATIO_1_1(1.0f, R.string.crop_aspect_1_1),
    RATIO_4_5(4.0f / 5.0f, R.string.crop_aspect_4_5),
    RATIO_4_3(4.0f / 3.0f, R.string.crop_aspect_4_3),
    RATIO_3_4(3.0f / 4.0f, R.string.crop_aspect_3_4),
    RATIO_3_2(3.0f / 2.0f, R.string.crop_aspect_3_2),
    RATIO_2_3(2.0f / 3.0f, R.string.crop_aspect_2_3),
    RATIO_9_16(9.0f / 16.0f, R.string.crop_aspect_9_16),
    RATIO_16_9(16.0f / 9.0f, R.string.crop_aspect_16_9),
    OTHER(null, R.string.crop_aspect_custom)
}
