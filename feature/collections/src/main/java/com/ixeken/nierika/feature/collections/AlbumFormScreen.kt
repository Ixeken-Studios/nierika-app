package com.ixeken.nierika.feature.collections

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.Image
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.designsystem.util.shimmerEffect
import com.ixeken.nierika.core.domain.model.MediaItem

import androidx.compose.foundation.layout.FlowRow
import com.ixeken.nierika.core.designsystem.component.AddTagDialog
import com.ixeken.nierika.feature.collections.components.CoverPhotoSelectionSheet

enum class AlbumFormMode {
    NEW,
    EDIT
}

/**
 * Pantalla de formulario para Crear Nuevo Álbum y Editar Álbum.
 * Sigue la maqueta oficial con entradas de texto amplias, sección dedicada de Foto de Portada M3 Expressive,
 * sección de etiquetas (#tags), retícula de fotos redondeadas con botón de eliminación y barra de acciones flotante inferior.
 */
@Composable
fun AlbumFormScreen(
    mode: AlbumFormMode,
    initialTitle: String = "",
    initialDescription: String = "",
    initialCoverUri: String? = null,
    initialTags: List<String> = emptyList(),
    initialPhotos: List<MediaItem> = emptyList(),
    onCloseClick: () -> Unit,
    onSaveClick: (title: String, description: String, coverUri: String?, photos: List<MediaItem>, tags: List<String>) -> Unit,
    onAddPhotoClick: () -> Unit = {},
    onDeleteAlbumClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var coverUri by remember { mutableStateOf(initialCoverUri) }
    val selectedTags = remember { mutableStateListOf<String>().apply { addAll(initialTags) } }
    val selectedPhotos = remember { mutableStateListOf<MediaItem>().apply { addAll(initialPhotos) } }
    var isPhotoPickerVisible by remember { mutableStateOf(false) }
    var isCoverPickerVisible by remember { mutableStateOf(false) }
    var isAddTagDialogVisible by remember { mutableStateOf(false) }

    val oldestPhoto = remember(selectedPhotos.toList()) {
        selectedPhotos.minByOrNull { it.dateTakenMillis.takeIf { d -> d > 0 } ?: it.dateModifiedMillis }
    }
    val isCustomCover = coverUri != null
    val effectiveCoverUri = coverUri ?: oldestPhoto?.uri

    val haptic = LocalHapticFeedback.current
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val context = LocalContext.current

    BackHandler(onBack = {
        when {
            isAddTagDialogVisible -> isAddTagDialogVisible = false
            isCoverPickerVisible -> isCoverPickerVisible = false
            isPhotoPickerVisible -> isPhotoPickerVisible = false
            else -> onCloseClick()
        }
    })

    // Diálogo interactivo para agregar una nueva etiqueta (#tag) al álbum
    if (isAddTagDialogVisible) {
        AddTagDialog(
            existingTags = selectedTags.toList(),
            onDismiss = { isAddTagDialogVisible = false },
            onConfirmAddTag = { newTag ->
                if (!selectedTags.any { it.equals(newTag, ignoreCase = true) }) {
                    selectedTags.add(newTag)
                }
                isAddTagDialogVisible = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Top Bar con botones circulares elevados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButtonWithAnimatedShape(
                    onClick = onCloseClick,
                    buttonSize = 40.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButtonWithAnimatedShape(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        val finalCover = coverUri ?: oldestPhoto?.uri
                        onSaveClick(title, description, finalCover, selectedPhotos.toList(), selectedTags.toList())
                    },
                    buttonSize = 40.dp,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.action_save),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Scrollable Content
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = 110.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sección 1: Inputs de Título y Descripción
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        // Title Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (title.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.album_form_add_a_title),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Description Input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (description.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.album_form_add_description),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Sección de Tags del Álbum
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                            isAddTagDialogVisible = true
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
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (selectedTags.isEmpty()) {
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
                                    selectedTags.forEach { tag ->
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
                                                style = MaterialTheme.typography.bodyMedium,
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
                                                            selectedTags.remove(tag)
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
                }

                // Sección 2: Sección de Foto de Portada (M3 Expressive)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Header de sección con insignia de estado (Auto / Custom)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.album_form_album_cover),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Badge de estado de portada
                            if (selectedPhotos.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isCustomCover) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isCustomCover) Icons.Filled.Bookmark else Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isCustomCover) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isCustomCover) stringResource(R.string.album_form_cover_custom_badge)
                                            else stringResource(R.string.album_form_cover_auto_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isCustomCover) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Tarjeta interactiva de Portada
                        if (effectiveCoverUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Miniatura de la portada
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                ) {
                                    val coverRequest = remember(effectiveCoverUri) {
                                        ImageRequest.Builder(context)
                                            .data(effectiveCoverUri)
                                            .size(240, 240)
                                            .memoryCacheKey("${effectiveCoverUri}_thumb_240")
                                            .crossfade(false)
                                            .build()
                                    }
                                    val coverPainter = rememberAsyncImagePainter(model = coverRequest)
                                    val coverState by coverPainter.state.collectAsState()

                                    if (coverState is AsyncImagePainter.State.Loading || coverState is AsyncImagePainter.State.Empty) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .shimmerEffect()
                                        )
                                    }
                                    Image(
                                        painter = coverPainter,
                                        contentDescription = stringResource(R.string.album_form_album_cover),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // Controles de Portada
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isCustomCover) stringResource(R.string.album_form_cover_custom_badge)
                                        else stringResource(R.string.album_form_cover_auto_badge),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Botón Cambiar portada
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                    isCoverPickerVisible = true
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = stringResource(R.string.album_form_change_cover),
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.album_form_change_cover),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }

                                        // Botón Restablecer si es portada personalizada
                                        if (isCustomCover) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                                        coverUri = null
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = stringResource(R.string.album_form_reset_cover_auto),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = stringResource(R.string.album_form_reset_cover_auto),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sección 3: Opciones cuando no hay fotos seleccionadas
                if (selectedPhotos.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    isPhotoPickerVisible = true
                                }
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = stringResource(R.string.album_form_select_photos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    // Sección 4: Retícula de fotos seleccionadas con bordes adaptativos periféricos
                    itemsIndexed(selectedPhotos, key = { _, photo -> photo.id }) { index, photo ->
                        val cellShape = getGridItemShape(
                            index = index,
                            totalCount = selectedPhotos.size,
                            columnsCount = 3,
                            cornerRadius = 16.dp
                        )
                        val isPhotoCurrentCover = photo.uri == effectiveCoverUri

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.0f)
                                .clip(cellShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    coverUri = photo.uri
                                }
                        ) {
                            val thumbnailRequest = remember(photo.uri) {
                                ImageRequest.Builder(context)
                                    .data(photo.uri)
                                    .size(512, 512)
                                    .memoryCacheKey("${photo.uri}_thumb_512")
                                    .crossfade(false)
                                    .build()
                            }
                            val thumbnailPainter = rememberAsyncImagePainter(model = thumbnailRequest)
                            val thumbnailState by thumbnailPainter.state.collectAsState()

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (thumbnailState is AsyncImagePainter.State.Loading || thumbnailState is AsyncImagePainter.State.Empty) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .shimmerEffect()
                                    )
                                }
                                Image(
                                    painter = thumbnailPainter,
                                    contentDescription = photo.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Badge de Foto de Portada Activa
                            if (isPhotoCurrentCover) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = stringResource(R.string.album_form_badge_cover),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.album_form_badge_cover),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            if (photo.isVideo || photo.mimeType.startsWith("video/")) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Botón de eliminar foto del borrador con morphing
                            IconButtonWithAnimatedShape(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    selectedPhotos.remove(photo)
                                    if (coverUri == photo.uri) {
                                        coverUri = null
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp),
                                buttonSize = 24.dp,
                                containerColor = Color.Black.copy(alpha = 0.6f),
                                contentColor = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Barra de acciones flotante inferior (estilo Media View de Nierika)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                // 1. Add photos
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.album_form_add_photo)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            isPhotoPickerVisible = true
                        },
                        buttonSize = 36.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.album_form_add_photo),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 2. Add tag (#)
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.action_add_tag)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            isAddTagDialogVisible = true
                        },
                        buttonSize = 36.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = stringResource(R.string.action_add_tag),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 3. Delete Album (Opcional solo en modo edición)
                if (mode == AlbumFormMode.EDIT && onDeleteAlbumClick != null) {
                    NierikaPlainTooltipBox(
                        tooltipText = stringResource(R.string.album_detail_delete_album)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onDeleteAlbumClick()
                            },
                            buttonSize = 36.dp
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderDelete,
                                contentDescription = stringResource(R.string.album_detail_delete_album),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sheet interactivo para Seleccionar Foto de Portada
        AnimatedVisibility(
            visible = isCoverPickerVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            ),
            modifier = Modifier.fillMaxSize().zIndex(130f)
        ) {
            CoverPhotoSelectionSheet(
                photos = selectedPhotos,
                activeCoverUri = effectiveCoverUri,
                isCustomCover = isCustomCover,
                onDismiss = { isCoverPickerVisible = false },
                onSelectCover = { selectedUri ->
                    coverUri = selectedUri
                    isCoverPickerVisible = false
                },
                onResetToAuto = {
                    coverUri = null
                    isCoverPickerVisible = false
                },
                onPickFromLibrary = {
                    isCoverPickerVisible = false
                    isPhotoPickerVisible = true
                }
            )
        }

        // Selector de fotos tipo Timeline integrado
        AnimatedVisibility(
            visible = isPhotoPickerVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = motionScheme.defaultSpatialSpec()
            ),
            modifier = Modifier.fillMaxSize().zIndex(120f)
        ) {
            AlbumPhotoPickerScreen(
                albumTitle = title,
                initialSelectedPhotos = selectedPhotos,
                mediaPagingFlow = viewModel.mediaPagingData,
                onDismiss = { isPhotoPickerVisible = false },
                onPhotosSelected = { updatedList ->
                    selectedPhotos.clear()
                    selectedPhotos.addAll(updatedList)
                    if (coverUri != null && updatedList.none { it.uri == coverUri }) {
                        coverUri = null
                    }
                }
            )
        }
    }
}

