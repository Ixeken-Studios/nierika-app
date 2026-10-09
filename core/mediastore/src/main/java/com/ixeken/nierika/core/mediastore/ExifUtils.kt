package com.ixeken.nierika.core.mediastore

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Obtiene la ruta física del archivo a partir de su URI de MediaStore si está disponible.
 */
fun Context.getFilePathFromUri(uri: Uri): String? {
    if (uri.scheme == "file") return uri.path
    return try {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                if (index >= 0) cursor.getString(index) else null
            } else null
        }
    } catch (_: Throwable) {
        null
    }
}

enum class ExifPermissionReason {
    PERMISSION_REQUIRED,
    STORAGE_ACCESS_REQUIRED
}

enum class ExifErrorCode {
    READ_FAILED,
    WRITE_FAILED,
    CORRUPT_OR_EMPTY,
    UNKNOWN
}

/**
 * Resultado detallado de la operación de eliminación de metadatos EXIF.
 */
sealed class ExifRemovalResult {
    data object Success : ExifRemovalResult()
    data class PermissionRequired(
        val intentSender: IntentSender? = null,
        val reason: ExifPermissionReason = ExifPermissionReason.PERMISSION_REQUIRED
    ) : ExifRemovalResult()
    data class Error(
        val message: String? = null,
        val code: ExifErrorCode = ExifErrorCode.UNKNOWN
    ) : ExifRemovalResult()
}

/**
 * Lista exhaustiva de atributos EXIF sin dimensiones que contienen datos personales,
 * ubicación GPS, detalles de cámara, fechas y comentarios del dispositivo.
 */
val AllNonDimensionExifAttributes = listOf(
    ExifInterface.TAG_IMAGE_DESCRIPTION,
    ExifInterface.TAG_MAKE,
    ExifInterface.TAG_MODEL,
    ExifInterface.TAG_SOFTWARE,
    ExifInterface.TAG_ARTIST,
    ExifInterface.TAG_COPYRIGHT,
    ExifInterface.TAG_DATETIME,
    ExifInterface.TAG_USER_COMMENT,
    ExifInterface.TAG_DATETIME_ORIGINAL,
    ExifInterface.TAG_DATETIME_DIGITIZED,
    ExifInterface.TAG_OFFSET_TIME,
    ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
    ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
    ExifInterface.TAG_SUBSEC_TIME,
    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
    ExifInterface.TAG_GPS_LATITUDE,
    ExifInterface.TAG_GPS_LONGITUDE,
    ExifInterface.TAG_GPS_LATITUDE_REF,
    ExifInterface.TAG_GPS_LONGITUDE_REF,
    ExifInterface.TAG_GPS_ALTITUDE,
    ExifInterface.TAG_GPS_ALTITUDE_REF,
    ExifInterface.TAG_GPS_TIMESTAMP,
    ExifInterface.TAG_GPS_DATESTAMP,
    ExifInterface.TAG_GPS_PROCESSING_METHOD,
    ExifInterface.TAG_GPS_SPEED,
    ExifInterface.TAG_GPS_SPEED_REF,
    ExifInterface.TAG_GPS_TRACK,
    ExifInterface.TAG_GPS_TRACK_REF,
    ExifInterface.TAG_GPS_IMG_DIRECTION,
    ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
    ExifInterface.TAG_GPS_DEST_LATITUDE,
    ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
    ExifInterface.TAG_GPS_DEST_LONGITUDE,
    ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
    ExifInterface.TAG_GPS_DEST_BEARING,
    ExifInterface.TAG_GPS_DEST_BEARING_REF,
    ExifInterface.TAG_GPS_DEST_DISTANCE,
    ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
    ExifInterface.TAG_EXPOSURE_TIME,
    ExifInterface.TAG_F_NUMBER,
    ExifInterface.TAG_EXPOSURE_PROGRAM,
    ExifInterface.TAG_SPECTRAL_SENSITIVITY,
    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
    ExifInterface.TAG_OECF,
    ExifInterface.TAG_EXIF_VERSION,
    ExifInterface.TAG_COMPONENTS_CONFIGURATION,
    ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
    ExifInterface.TAG_SHUTTER_SPEED_VALUE,
    ExifInterface.TAG_APERTURE_VALUE,
    ExifInterface.TAG_BRIGHTNESS_VALUE,
    ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
    ExifInterface.TAG_MAX_APERTURE_VALUE,
    ExifInterface.TAG_SUBJECT_DISTANCE,
    ExifInterface.TAG_METERING_MODE,
    ExifInterface.TAG_LIGHT_SOURCE,
    ExifInterface.TAG_FLASH,
    ExifInterface.TAG_FOCAL_LENGTH,
    ExifInterface.TAG_SUBJECT_AREA,
    ExifInterface.TAG_MAKER_NOTE,
    ExifInterface.TAG_FLASH_ENERGY,
    ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
    ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
    ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
    ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
    ExifInterface.TAG_SUBJECT_LOCATION,
    ExifInterface.TAG_EXPOSURE_INDEX,
    ExifInterface.TAG_SENSING_METHOD,
    ExifInterface.TAG_FILE_SOURCE,
    ExifInterface.TAG_SCENE_TYPE,
    ExifInterface.TAG_CFA_PATTERN,
    ExifInterface.TAG_CUSTOM_RENDERED,
    ExifInterface.TAG_EXPOSURE_MODE,
    ExifInterface.TAG_WHITE_BALANCE,
    ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
    ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
    ExifInterface.TAG_SCENE_CAPTURE_TYPE,
    ExifInterface.TAG_GAIN_CONTROL,
    ExifInterface.TAG_CONTRAST,
    ExifInterface.TAG_SATURATION,
    ExifInterface.TAG_SHARPNESS,
    ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
    ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
    ExifInterface.TAG_IMAGE_UNIQUE_ID,
    ExifInterface.TAG_CAMERA_OWNER_NAME,
    ExifInterface.TAG_BODY_SERIAL_NUMBER,
    ExifInterface.TAG_LENS_SPECIFICATION,
    ExifInterface.TAG_LENS_MAKE,
    ExifInterface.TAG_LENS_MODEL,
    ExifInterface.TAG_LENS_SERIAL_NUMBER
)

/**
 * Lista de etiquetas EXIF de geolocalización GPS.
 */
val GpsExifAttributes = listOf(
    ExifInterface.TAG_GPS_LATITUDE,
    ExifInterface.TAG_GPS_LONGITUDE,
    ExifInterface.TAG_GPS_LATITUDE_REF,
    ExifInterface.TAG_GPS_LONGITUDE_REF,
    ExifInterface.TAG_GPS_ALTITUDE,
    ExifInterface.TAG_GPS_ALTITUDE_REF,
    ExifInterface.TAG_GPS_TIMESTAMP,
    ExifInterface.TAG_GPS_DATESTAMP,
    ExifInterface.TAG_GPS_PROCESSING_METHOD,
    ExifInterface.TAG_GPS_SPEED,
    ExifInterface.TAG_GPS_SPEED_REF,
    ExifInterface.TAG_GPS_TRACK,
    ExifInterface.TAG_GPS_TRACK_REF,
    ExifInterface.TAG_GPS_IMG_DIRECTION,
    ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
    ExifInterface.TAG_GPS_DEST_LATITUDE,
    ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
    ExifInterface.TAG_GPS_DEST_LONGITUDE,
    ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
    ExifInterface.TAG_GPS_DEST_BEARING,
    ExifInterface.TAG_GPS_DEST_BEARING_REF,
    ExifInterface.TAG_GPS_DEST_DISTANCE,
    ExifInterface.TAG_GPS_DEST_DISTANCE_REF
)

/**
 * Elimina todos los metadatos EXIF no esenciales (GPS, cámara, autor, fecha, notas de fabricante, etc.)
 * preservando intactas las dimensiones originales y orientación de la imagen.
 * 
 * Flujo de ejecución:
 * 1. Intenta modificación directa si la ruta del archivo es accesible y editable.
 * 2. Si es Content URI, copia a archivo temporal en caché, limpia atributos con ExifInterface, y sobreescribe el destino.
 * 3. Captura y diagnostica excepciones específicas de permisos de Android.
 */
fun Context.removeExifMetadata(uri: Uri): ExifRemovalResult {
    // 1. Verificar si el archivo físico directo existe y es escribible
    val directPath = getFilePathFromUri(uri)
    if (directPath != null) {
        val file = File(directPath)
        if (file.exists() && file.canWrite()) {
            try {
                val exif = ExifInterface(file.absolutePath)
                AllNonDimensionExifAttributes.forEach { tag ->
                    exif.setAttribute(tag, null)
                }
                exif.saveAttributes()
                return ExifRemovalResult.Success
            } catch (e: Exception) {
                // Fallback al método por flujo temporal si saveAttributes falló en archivo
            }
        }
    }

    // 2. Método seguro por archivo temporal en caché + sobreescritura via ContentResolver
    val tempFile = File(cacheDir, "exif_clean_${System.currentTimeMillis()}.tmp")
    try {
        val inputStream = contentResolver.openInputStream(uri)
            ?: return ExifRemovalResult.Error(code = ExifErrorCode.READ_FAILED)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (!tempFile.exists() || tempFile.length() == 0L) {
            tempFile.delete()
            return ExifRemovalResult.Error(code = ExifErrorCode.CORRUPT_OR_EMPTY)
        }

        // Limpiar atributos en el archivo local de caché (siempre tiene permisos)
        val exif = ExifInterface(tempFile.absolutePath)
        AllNonDimensionExifAttributes.forEach { tag ->
            exif.setAttribute(tag, null)
        }
        exif.saveAttributes()

        // Sobreescribir el destino mediante ContentResolver
        val outputStream = contentResolver.openOutputStream(uri, "wt")
            ?: run {
                // Si el openOutputStream es rechazado por Scoped Storage en Android 11+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                    tempFile.delete()
                    return ExifRemovalResult.PermissionRequired(
                        intentSender = writeRequest.intentSender,
                        reason = ExifPermissionReason.PERMISSION_REQUIRED
                    )
                }
                tempFile.delete()
                return ExifRemovalResult.Error(code = ExifErrorCode.WRITE_FAILED)
            }

        outputStream.use { out ->
            tempFile.inputStream().use { cleanInput ->
                cleanInput.copyTo(out)
            }
        }

        tempFile.delete()
        return ExifRemovalResult.Success
    } catch (e: SecurityException) {
        tempFile.delete()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            return ExifRemovalResult.PermissionRequired(
                intentSender = e.userAction.actionIntent.intentSender,
                reason = ExifPermissionReason.PERMISSION_REQUIRED
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                return ExifRemovalResult.PermissionRequired(
                    intentSender = writeRequest.intentSender,
                    reason = ExifPermissionReason.PERMISSION_REQUIRED
                )
            } catch (_: Throwable) {}
        }
        return ExifRemovalResult.PermissionRequired(
            intentSender = null,
            reason = ExifPermissionReason.STORAGE_ACCESS_REQUIRED
        )
    } catch (e: Exception) {
        tempFile.delete()
        return ExifRemovalResult.Error(message = e.localizedMessage)
    }
}

/**
 * Elimina metadatos de ubicación GPS de la fotografía.
 */
fun Context.removeGpsMetadata(uri: Uri): ExifRemovalResult {
    val directPath = getFilePathFromUri(uri)
    if (directPath != null) {
        val file = File(directPath)
        if (file.exists() && file.canWrite()) {
            try {
                val exif = ExifInterface(file.absolutePath)
                GpsExifAttributes.forEach { tag ->
                    exif.setAttribute(tag, null)
                }
                exif.saveAttributes()
                return ExifRemovalResult.Success
            } catch (_: Exception) {
            }
        }
    }

    val tempFile = File(cacheDir, "gps_clean_${System.currentTimeMillis()}.tmp")
    try {
        val inputStream = contentResolver.openInputStream(uri)
            ?: return ExifRemovalResult.Error(code = ExifErrorCode.READ_FAILED)

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val exif = ExifInterface(tempFile.absolutePath)
        GpsExifAttributes.forEach { tag ->
            exif.setAttribute(tag, null)
        }
        exif.saveAttributes()

        val outputStream = contentResolver.openOutputStream(uri, "wt")
            ?: run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                    tempFile.delete()
                    return ExifRemovalResult.PermissionRequired(
                        intentSender = writeRequest.intentSender,
                        reason = ExifPermissionReason.PERMISSION_REQUIRED
                    )
                }
                tempFile.delete()
                return ExifRemovalResult.Error(code = ExifErrorCode.WRITE_FAILED)
            }

        outputStream.use { out ->
            tempFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }

        tempFile.delete()
        return ExifRemovalResult.Success
    } catch (e: SecurityException) {
        tempFile.delete()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            return ExifRemovalResult.PermissionRequired(
                intentSender = e.userAction.actionIntent.intentSender,
                reason = ExifPermissionReason.PERMISSION_REQUIRED
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                return ExifRemovalResult.PermissionRequired(
                    intentSender = writeRequest.intentSender,
                    reason = ExifPermissionReason.PERMISSION_REQUIRED
                )
            } catch (_: Throwable) {}
        }
        return ExifRemovalResult.PermissionRequired(
            intentSender = null,
            reason = ExifPermissionReason.STORAGE_ACCESS_REQUIRED
        )
    } catch (e: Exception) {
        tempFile.delete()
        return ExifRemovalResult.Error(message = e.localizedMessage)
    }
}

/**
 * Resultado de la operación de renombrado de archivos multimedia.
 */
sealed class RenameResult {
    data class Success(val newName: String) : RenameResult()
    data class PermissionRequired(
        val intentSender: IntentSender? = null
    ) : RenameResult()
    data class Error(val message: String? = null) : RenameResult()
}

/**
 * Renombra el archivo en MediaStore preservando su tipo y ubicación.
 */
fun Context.renameMediaFile(uri: Uri, newDisplayName: String): RenameResult {
    return try {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newDisplayName)
        }
        val rows = contentResolver.update(uri, values, null, null)
        if (rows > 0) {
            RenameResult.Success(newDisplayName)
        } else {
            RenameResult.Error("No se pudo actualizar el nombre del archivo")
        }
    } catch (e: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (e is RecoverableSecurityException) {
                RenameResult.PermissionRequired(e.userAction.actionIntent.intentSender)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intentSender = MediaStore.createWriteRequest(contentResolver, listOf(uri)).intentSender
                RenameResult.PermissionRequired(intentSender)
            } else {
                RenameResult.Error(e.localizedMessage)
            }
        } else {
            RenameResult.Error(e.localizedMessage)
        }
    } catch (e: Exception) {
        RenameResult.Error(e.localizedMessage)
    }
}

/**
 * Copia los metadatos EXIF de un archivo de origen a un archivo de destino.
 */
fun Context.copyExifMetadata(sourceUri: Uri, destinationUri: Uri) {
    try {
        val sourceStream = contentResolver.openInputStream(sourceUri) ?: return
        val sourceExif = sourceStream.use { ExifInterface(it) }

        contentResolver.openFileDescriptor(destinationUri, "rw")?.use { pfd ->
            val destExif = ExifInterface(pfd.fileDescriptor)
            val attributes = arrayOf(
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_DATETIME_DIGITIZED,
                ExifInterface.TAG_DATETIME_ORIGINAL,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF,
                ExifInterface.TAG_GPS_ALTITUDE,
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                ExifInterface.TAG_GPS_TIMESTAMP,
                ExifInterface.TAG_GPS_DATESTAMP,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_WHITE_BALANCE,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_LENS_MAKE,
                ExifInterface.TAG_LENS_MODEL
            )

            for (attr in attributes) {
                val value = sourceExif.getAttribute(attr)
                if (value != null) {
                    destExif.setAttribute(attr, value)
                }
            }
            // Los píxeles del bitmap exportado ya están renderizados en orientación física normal (0°).
            // Fijar la orientación a NORMAL (1) previene que visores apliquen una segunda rotación sobre fotos de cámara.
            destExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
            destExif.saveAttributes()
        }
    } catch (_: Exception) {}
}

/**
 * Resultado de la operación de actualización de fecha y hora.
 */
sealed class UpdateDateTimeResult {
    data class Success(val newTimestampMillis: Long) : UpdateDateTimeResult()
    data class PermissionRequired(
        val intentSender: IntentSender? = null
    ) : UpdateDateTimeResult()
    data class Error(val message: String? = null) : UpdateDateTimeResult()
}

/**
 * Actualiza las etiquetas EXIF de fecha (TAG_DATETIME, TAG_DATETIME_ORIGINAL, TAG_DATETIME_DIGITIZED)
 * y las columnas DATE_TAKEN y DATE_MODIFIED de MediaStore para el archivo multimedia.
 */
fun Context.updateMediaDateTime(uri: Uri, newTimestampMillis: Long): UpdateDateTimeResult {
    val date = Date(newTimestampMillis)
    val exifFormatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
    val formattedExifDate = exifFormatter.format(date)
    val offsetFormatter = SimpleDateFormat("XXX", Locale.US)
    val formattedOffset = offsetFormatter.format(date)

    var isExifUpdated = false

    // 1. Intento de actualización in-place con FileDescriptor "rw" (estándar en Android Scoped Storage)
    try {
        contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
            val exif = ExifInterface(pfd.fileDescriptor)
            exif.setAttribute(ExifInterface.TAG_DATETIME, formattedExifDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedExifDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedExifDate)
            exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, formattedOffset)
            exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, formattedOffset)
            exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_DIGITIZED, formattedOffset)
            exif.saveAttributes()
            isExifUpdated = true
        }
    } catch (e: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            return UpdateDateTimeResult.PermissionRequired(e.userAction.actionIntent.intentSender)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                return UpdateDateTimeResult.PermissionRequired(writeRequest.intentSender)
            } catch (_: Throwable) {}
        }
        return UpdateDateTimeResult.Error(e.localizedMessage)
    } catch (_: Throwable) {}

    // 2. Intento de actualización en archivo directo si es accesible
    if (!isExifUpdated) {
        val directPath = getFilePathFromUri(uri)
        if (directPath != null) {
            val file = File(directPath)
            if (file.exists() && file.canWrite()) {
                try {
                    val exif = ExifInterface(file.absolutePath)
                    exif.setAttribute(ExifInterface.TAG_DATETIME, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, formattedOffset)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, formattedOffset)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_DIGITIZED, formattedOffset)
                    exif.saveAttributes()
                    isExifUpdated = true
                } catch (_: Throwable) {}
            }
        }
    }

    // 3. Método por archivo temporal en caché como fallback
    if (!isExifUpdated) {
        val tempFile = File(cacheDir, "exif_date_${System.currentTimeMillis()}.tmp")
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.exists() && tempFile.length() > 0L) {
                    val exif = ExifInterface(tempFile.absolutePath)
                    exif.setAttribute(ExifInterface.TAG_DATETIME, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, formattedExifDate)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, formattedOffset)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, formattedOffset)
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_DIGITIZED, formattedOffset)
                    exif.saveAttributes()

                    val outputStream = contentResolver.openOutputStream(uri, "wt")
                    if (outputStream != null) {
                        outputStream.use { out ->
                            tempFile.inputStream().use { cleanInput ->
                                cleanInput.copyTo(out)
                            }
                        }
                        isExifUpdated = true
                    }
                }
            }
        } catch (e: SecurityException) {
            tempFile.delete()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                return UpdateDateTimeResult.PermissionRequired(e.userAction.actionIntent.intentSender)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val writeRequest = MediaStore.createWriteRequest(contentResolver, listOf(uri))
                    return UpdateDateTimeResult.PermissionRequired(writeRequest.intentSender)
                } catch (_: Throwable) {}
            }
            return UpdateDateTimeResult.Error(e.localizedMessage)
        } catch (_: Throwable) {
        } finally {
            tempFile.delete()
        }
    }

    // 4. Actualizar MediaStore columnas DATE_TAKEN y DATE_MODIFIED
    return try {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATE_TAKEN, newTimestampMillis)
            put(MediaStore.MediaColumns.DATE_MODIFIED, (newTimestampMillis / 1000).toInt())
        }
        val rows = contentResolver.update(uri, values, null, null)
        if (rows > 0 || isExifUpdated) {
            UpdateDateTimeResult.Success(newTimestampMillis)
        } else {
            UpdateDateTimeResult.Error("No se pudo actualizar la fecha en MediaStore")
        }
    } catch (e: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
            UpdateDateTimeResult.PermissionRequired(e.userAction.actionIntent.intentSender)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intentSender = MediaStore.createWriteRequest(contentResolver, listOf(uri)).intentSender
                UpdateDateTimeResult.PermissionRequired(intentSender)
            } catch (_: Throwable) {
                UpdateDateTimeResult.Error(e.localizedMessage)
            }
        } else {
            UpdateDateTimeResult.Error(e.localizedMessage)
        }
    } catch (e: Exception) {
        UpdateDateTimeResult.Error(e.localizedMessage)
    }
}


