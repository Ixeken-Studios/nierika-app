package com.ixeken.nierika.feature.viewer.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.ixeken.nierika.core.designsystem.component.AddTagDialog
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.FilledTonalButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.TextButtonWithAnimatedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.exifinterface.media.ExifInterface
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.mediastore.getFilePathFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Datos estructurados extraidos de los metadatos EXIF reales del archivo multimedia.
 */
private data class PhotoExifDetails(
    val cameraModel: String? = null,
    val lensModel: String? = null,
    val aperture: String? = null,
    val iso: String? = null,
    val focalLength: String? = null,
    val exposureTime: String? = null,
    val flash: String? = null,
    val software: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null
) {
    val hasCameraData: Boolean
        get() = !cameraModel.isNullOrBlank() || !lensModel.isNullOrBlank() || aperture != null || iso != null || exposureTime != null || focalLength != null

    val hasGpsData: Boolean
        get() = latitude != null && longitude != null

    val hasAnyExifData: Boolean
        get() = hasCameraData || hasGpsData || !software.isNullOrBlank() || flash != null
}

private fun formatExposureTime(exposure: String?): String? {
    if (exposure.isNullOrBlank()) return null
    return try {
        val seconds = exposure.toDoubleOrNull()
        when {
            seconds == null -> if (exposure.endsWith("s")) exposure else "${exposure}s"
            seconds >= 1.0 -> "${String.format(Locale.getDefault(), "%.1f", seconds)}s"
            seconds > 0.0 -> "1/${(1.0 / seconds).roundToInt()}s"
            else -> "${exposure}s"
        }
    } catch (_: Exception) {
        if (exposure.endsWith("s")) exposure else "${exposure}s"
    }
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0) return "0 KB"
    val kb = sizeBytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.getDefault(), "%.2f MB", mb)
    } else {
        String.format(Locale.getDefault(), "%.2f KB", kb)
    }
}

private fun formatHeaderDateTime(timestampMillis: Long): String {
    val formatter = SimpleDateFormat("MMMM d, yy • HH:mm", Locale.getDefault())
    val dateStr = formatter.format(Date(timestampMillis))
    return dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

/**
 * Modal BottomSheet moderno estilo Material 3 Expressive de información detallada de la foto.
 * Disposición limpia:
 * - Encabezado superior con fecha/hora, opción de descripción y botón "Remove metadata".
 * - Tarjeta inferior agrupada con Label (editable preservando extensión), Path, Dimensions y Metadatos EXIF.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoInfoBottomSheet(
    isVisible: Boolean,
    mediaItem: MediaItem?,
    tags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onRemoveMetadata: () -> Unit = {},
    onRenameFile: (String) -> Unit = {},
    onUpdateDateTime: (Long) -> Unit = {},
    onAddTag: (String) -> Unit = {},
    onRemoveTag: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var displayedMediaItem by remember { mutableStateOf(mediaItem) }
    LaunchedEffect(mediaItem) {
        if (mediaItem != null) {
            displayedMediaItem = mediaItem
        }
    }
    val currentItem = mediaItem ?: displayedMediaItem
    if (currentItem == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var exifDetails by remember(currentItem.uri, isVisible) { mutableStateOf<PhotoExifDetails?>(null) }
    var filePath by remember(currentItem.uri, isVisible) { mutableStateOf<String?>(null) }
    var isLoaded by remember(currentItem.uri, isVisible) { mutableStateOf(false) }
    var isRenameDialogVisible by remember { mutableStateOf(false) }
    var isEditDateTimeDialogVisible by remember { mutableStateOf(false) }
    var isAddTagDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(currentItem.uri, isVisible) {
        withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(currentItem.uri)
                filePath = context.getFilePathFromUri(uri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                    val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                    val camera = when {
                        !model.isNullOrBlank() && !make.isNullOrBlank() -> {
                            if (model.contains(make, ignoreCase = true)) model else "$make $model"
                        }
                        !model.isNullOrBlank() -> model
                        !make.isNullOrBlank() -> make
                        else -> null
                    }

                    val lens = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)
                        ?: exif.getAttribute(ExifInterface.TAG_LENS_MAKE)
                    val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
                    val isoSpeed = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                    val focal = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0.0)
                    val exposureRaw = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                    val softwareVal = exif.getAttribute(ExifInterface.TAG_SOFTWARE)

                    val flashInt = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                    val flashText = when {
                        flashInt < 0 -> null
                        (flashInt and 1) != 0 -> context.getString(R.string.exif_flash_fired)
                        else -> context.getString(R.string.exif_flash_off)
                    }

                    val latLongArray = exif.latLong
                    val altitudeVal = exif.getAltitude(Double.NaN).let { if (it.isNaN()) null else it }

                    exifDetails = PhotoExifDetails(
                        cameraModel = camera,
                        lensModel = lens,
                        aperture = if (!fNumber.isNullOrBlank()) "f/$fNumber" else null,
                        iso = if (!isoSpeed.isNullOrBlank()) "ISO $isoSpeed" else null,
                        focalLength = if (focal > 0.0) String.format(Locale.getDefault(), "%.1f mm", focal) else null,
                        exposureTime = formatExposureTime(exposureRaw),
                        flash = flashText,
                        software = softwareVal,
                        latitude = latLongArray?.getOrNull(0),
                        longitude = latLongArray?.getOrNull(1),
                        altitude = altitudeVal
                    )
                }
            } catch (_: Exception) {
                exifDetails = null
            } finally {
                isLoaded = true
            }
        }
    }

    BackHandler(enabled = isVisible) {
        onDismiss()
    }

    // Diálogo de edición de nombre con preservación estricta de la extensión
    if (isRenameDialogVisible) {
        RenameFileDialog(
            currentDisplayName = currentItem.displayName,
            onDismiss = { isRenameDialogVisible = false },
            onConfirmRename = { newFullName ->
                onRenameFile(newFullName)
                isRenameDialogVisible = false
            }
        )
    }

    // Diálogo de ajuste de fecha y hora EXIF
    if (isEditDateTimeDialogVisible) {
        EditDateTimeDialog(
            initialTimestampMillis = currentItem.dateTakenMillis,
            onDismiss = { isEditDateTimeDialogVisible = false },
            onConfirm = { newTimestampMillis ->
                onUpdateDateTime(newTimestampMillis)
                isEditDateTimeDialogVisible = false
            }
        )
    }

    // Diálogo para agregar una nueva etiqueta (#tag)
    if (isAddTagDialogVisible) {
        AddTagDialog(
            existingTags = tags,
            onDismiss = { isAddTagDialogVisible = false },
            onConfirmAddTag = { newTag ->
                onAddTag(newTag)
                isAddTagDialogVisible = false
            }
        )
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = modifier
        ) {
            PhotoInfoBodyCards(
                mediaItem = currentItem,
                tags = tags,
                exifDetails = exifDetails,
                filePath = filePath,
                isLoaded = isLoaded,
                onEditDateTimeClick = { isEditDateTimeDialogVisible = true },
                onAddTagClick = { isAddTagDialogVisible = true },
                onRemoveTag = onRemoveTag,
                onRenameClick = { isRenameDialogVisible = true },
                onRemoveMetadata = {
                    onDismiss()
                    onRemoveMetadata()
                }
            )
        }
    }
}

/**
 * Contenedor modular de tarjetas informativas de la foto con diseño Material 3 Expressive.
 * Reutilizable en BottomSheet (pantallas móviles) y SidePanel (tablets y pantallas expandidas).
 */
@Composable
private fun PhotoInfoBodyCards(
    mediaItem: MediaItem,
    tags: List<String>,
    exifDetails: PhotoExifDetails?,
    filePath: String?,
    isLoaded: Boolean,
    onEditDateTimeClick: () -> Unit,
    onAddTagClick: () -> Unit,
    onRemoveTag: (String) -> Unit,
    onRenameClick: () -> Unit,
    onRemoveMetadata: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tarjeta Superior: Fecha y hora destacada con botón de edición + Sección de Tags
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatHeaderDateTime(mediaItem.dateTakenMillis),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButtonWithAnimatedShape(
                    onClick = onEditDateTimeClick,
                    buttonSize = 40.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditCalendar,
                        contentDescription = stringResource(R.string.edit_date_time_title),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Sección de Tags
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.info_tags_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botón Añadir tag
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onAddTagClick()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.action_add_tag),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.action_add_tag),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (tags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.info_no_tags),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f))
                                    .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                NierikaPlainTooltipBox(
                                    tooltipText = stringResource(R.string.tooltip_remove_tag)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                onRemoveTag(tag)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.tooltip_remove_tag),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Tarjeta Inferior de Detalles (Label, Path, Dimensions, Metadatos)
        val isVideo = mediaItem.mimeType.startsWith("video/")
        val formattedSize = formatFileSize(mediaItem.sizeBytes)
        val hasValidDimensions = mediaItem.width > 0 && mediaItem.height > 0
        val mpValue = if (hasValidDimensions) (mediaItem.width.toLong() * mediaItem.height) / 1_000_000.0 else 0.0
        val mpStr = if (hasValidDimensions && mpValue >= 0.1 && !isVideo) {
            String.format(Locale.getDefault(), "%.1f MP", mpValue)
        } else null

        val dimensionsStr = buildList {
            if (hasValidDimensions) {
                add("${mediaItem.width} × ${mediaItem.height}")
            }
            if (mpStr != null) {
                add(mpStr)
            }
            if (formattedSize.isNotBlank()) {
                add(formattedSize)
            }
        }.joinToString(" • ")
        val displayPath = filePath ?: mediaItem.uri

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item 1: Label con botón "Edit"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            mediaItem.isGif -> Icons.Filled.Gif
                            isVideo -> Icons.Filled.Videocam
                            else -> Icons.Filled.Image
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.info_label_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val nameScrollState = rememberScrollState()
                    LaunchedEffect(mediaItem.displayName) {
                        nameScrollState.scrollTo(0)
                    }
                    Text(
                        text = mediaItem.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.horizontalScroll(nameScrollState)
                    )
                }

                IconButtonWithAnimatedShape(
                    onClick = onRenameClick,
                    buttonSize = 40.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Item 2: Path
            InfoItemRow(
                icon = Icons.Filled.Folder,
                title = stringResource(R.string.info_path_title),
                subtitle = displayPath,
                isScrollable = true
            )

            // Item 3: Dimensions
            InfoItemRow(
                icon = Icons.Filled.AspectRatio,
                title = stringResource(R.string.info_dimensions_title),
                subtitle = dimensionsStr
            )
        }

        // 3. Sección de Metadatos EXIF (Cámara, Disparo, GPS y Botón Remove metadata)
        val details = exifDetails
        if (details != null && details.hasCameraData) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                details.cameraModel?.let { model ->
                    val shootingParams = listOfNotNull(
                        details.aperture,
                        details.exposureTime,
                        details.focalLength,
                        details.iso
                    ).joinToString(" • ")

                    InfoItemRow(
                        icon = Icons.Filled.CameraAlt,
                        title = model,
                        subtitle = if (shootingParams.isNotBlank()) shootingParams else details.lensModel
                    )
                }

                if (details.cameraModel == null) {
                    val shootingParams = listOfNotNull(
                        details.aperture,
                        details.exposureTime,
                        details.focalLength,
                        details.iso
                    ).joinToString(" • ")

                    if (shootingParams.isNotBlank()) {
                        InfoItemRow(
                            icon = Icons.Filled.CameraAlt,
                            title = stringResource(R.string.exif_camera_and_capture),
                            subtitle = shootingParams
                        )
                    }
                }

                details.lensModel?.let { lens ->
                    if (details.cameraModel != null) {
                        InfoItemRow(
                            icon = Icons.Filled.Info,
                            title = lens,
                            subtitle = details.flash
                        )
                    }
                }

                if (details.lensModel == null && details.flash != null) {
                    InfoItemRow(
                        icon = Icons.Filled.Info,
                        title = details.flash,
                        subtitle = details.software
                    )
                }
            }
        }

        // Tarjeta de Ubicación Geográfica GPS
        if (details != null && details.hasGpsData && details.latitude != null && details.longitude != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_permissions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val coordsStr = String.format(Locale.getDefault(), "%.4f, %.4f", details.latitude, details.longitude)
                val altitudeStr = details.altitude?.let { alt ->
                    stringResource(R.string.exif_location_altitude, alt)
                }
                val locationSubtitle = listOfNotNull(coordsStr, altitudeStr).joinToString(" • ")

                InfoItemRow(
                    icon = Icons.Filled.LocationOn,
                    title = stringResource(R.string.settings_permissions_title),
                    subtitle = locationSubtitle
                )

                FilledTonalButtonWithAnimatedShape(
                    onClick = {
                        val mapUri = Uri.parse("geo:${details.latitude},${details.longitude}?q=${details.latitude},${details.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.menu_open_with))
                }
            }
        }

        if (isLoaded && (details == null || !details.hasAnyExifData)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoItemRow(
                    icon = Icons.Filled.Shield,
                    title = stringResource(R.string.exif_no_metadata),
                    subtitle = stringResource(R.string.exif_no_metadata_desc)
                )
            }
        }

        // Botón Remove metadata colocado al final de los metadatos
        if (details != null && details.hasAnyExifData) {
            FilledTonalButtonWithAnimatedShape(
                onClick = onRemoveMetadata,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.action_remove_metadata),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Diálogo Material 3 Expressive para renombrar el archivo protegiendo y preservando la extensión original.
 */
@Composable
private fun RenameFileDialog(
    currentDisplayName: String,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit
) {
    val dotIndex = currentDisplayName.lastIndexOf('.')
    val baseName = if (dotIndex > 0) currentDisplayName.substring(0, dotIndex) else currentDisplayName
    val extension = if (dotIndex > 0) currentDisplayName.substring(dotIndex) else ""

    var nameInput by remember { mutableStateOf(baseName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_rename_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(stringResource(R.string.dialog_rename_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    suffix = if (extension.isNotEmpty()) {
                        {
                            Text(
                                text = extension,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ButtonWithAnimatedShape(
                onClick = {
                    val finalName = nameInput.trim() + extension
                    onConfirmRename(finalName)
                },
                enabled = nameInput.isNotBlank() && nameInput.trim() != baseName,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.action_save),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButtonWithAnimatedShape(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Fila modular de item de informacion con contenedor iconografico circular.
 */
@Composable
private fun InfoItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                if (isScrollable) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(subtitle) {
                        scrollState.scrollTo(0)
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.horizontalScroll(scrollState)
                    )
                } else {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
