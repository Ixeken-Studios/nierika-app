package com.ixeken.nierika.core.mediastore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings

/**
 * Utilitario para comprobar y solicitar permisos de administración directa de almacenamiento
 * (MANAGE_EXTERNAL_STORAGE en Android 11+ o MANAGE_MEDIA en Android 12+), permitiendo operaciones
 * de borrado y modificación directas sin confirmaciones repetitivas del sistema operativo.
 */
object StoragePermissionHelper {

    /**
     * Comprueba si la aplicación dispone de acceso de administración completo a archivos.
     */
    fun hasFullFileAccess(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager() -> true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && MediaStore.canManageMedia(context) -> true
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> true
            else -> false
        }
    }

    /**
     * Genera el intent de configuración del sistema operativo para otorgar permisos de administración de archivos.
     */
    fun createManageStorageIntent(context: Context): Intent {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
            else -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        }
    }
}
