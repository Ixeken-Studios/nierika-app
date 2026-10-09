package com.ixeken.nierika.feature.settings.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.domain.model.AppIconStyle
import com.ixeken.nierika.core.domain.model.AppLabelOption
import com.ixeken.nierika.core.domain.model.AppLauncherAlias

/**
 * Retorna el recurso drawable correspondiente al estilo de icono.
 */
fun AppIconStyle.getDrawableRes(): Int = when (this) {
    AppIconStyle.NIERIKA -> R.drawable.nierika_logo
    AppIconStyle.PHOTO_RAINBOW -> R.drawable.ic_launcher_photo_rainbow
    AppIconStyle.NEPO -> R.drawable.ic_launcher_nepo
    AppIconStyle.DRAFTO -> R.drawable.ic_launcher_drafto
}

/**
 * Retorna el título del estilo de icono.
 */
fun AppIconStyle.getTitleRes(): Int = when (this) {
    AppIconStyle.NIERIKA -> R.string.app_icon_style_classic
    AppIconStyle.PHOTO_RAINBOW -> R.string.app_icon_style_photo_rainbow
    AppIconStyle.NEPO -> R.string.app_icon_style_nepo
    AppIconStyle.DRAFTO -> R.string.app_icon_style_drafto
}

/**
 * Retorna el recurso de texto internacionalizado para la etiqueta.
 */
fun AppLabelOption.getLabelRes(): Int = when (this) {
    AppLabelOption.NIERIKA -> R.string.app_alias_nierika
    AppLabelOption.GALLERY -> R.string.app_alias_gallery
    AppLabelOption.PHOTOS -> R.string.app_alias_photos
    AppLabelOption.LIBRARY -> R.string.app_alias_library
    AppLabelOption.CAMERA -> R.string.app_alias_camera
    AppLabelOption.CALCULATOR -> R.string.app_alias_calculator
    AppLabelOption.NEPO -> R.string.app_alias_nepo
    AppLabelOption.NOTES -> R.string.app_alias_notes
    AppLabelOption.DRAFTO -> R.string.app_alias_drafto
}

/**
 * Retorna la descripción internacionalizada para la etiqueta.
 */
fun AppLabelOption.getDescRes(): Int = when (this) {
    AppLabelOption.NIERIKA -> R.string.app_alias_nierika_desc
    AppLabelOption.GALLERY -> R.string.app_alias_gallery_desc
    AppLabelOption.PHOTOS -> R.string.app_alias_photos_desc
    AppLabelOption.LIBRARY -> R.string.app_alias_library_desc
    AppLabelOption.CAMERA -> R.string.app_alias_camera_desc
    AppLabelOption.CALCULATOR -> R.string.app_alias_calculator_desc
    AppLabelOption.NEPO -> R.string.app_alias_calculator_desc
    AppLabelOption.NOTES -> R.string.app_alias_notes_desc
    AppLabelOption.DRAFTO -> R.string.app_alias_notes_desc
}

/**
 * Utilitario para alternar dinámicamente los <activity-alias> del launcher de la app.
 */
object LauncherAliasHelper {

    fun applyLauncherAlias(context: Context, targetAlias: AppLauncherAlias) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        AppLauncherAlias.entries.forEach { alias ->
            val componentName = ComponentName(packageName, alias.aliasName)
            val newState = if (alias == targetAlias) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            val currentState = try {
                packageManager.getComponentEnabledSetting(componentName)
            } catch (e: Exception) {
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            }

            if (currentState != newState) {
                try {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        newState,
                        PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    // Log or handle safely
                }
            }
        }
    }
}
