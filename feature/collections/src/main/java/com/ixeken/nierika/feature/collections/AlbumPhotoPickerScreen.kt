package com.ixeken.nierika.feature.collections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.NierikaPhotoGridItem
import com.ixeken.nierika.core.designsystem.util.getGridItemShape
import com.ixeken.nierika.core.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * Pantalla de Selector de Fotos tipo Timeline para agregar a un Álbum.
 * El selector viene activado automáticamente y permite marcar fotos y presionar "Save".
 */
@Composable
fun AlbumPhotoPickerScreen(
    albumTitle: String,
    initialSelectedPhotos: List<MediaItem>,
    mediaPagingFlow: Flow<androidx.paging.PagingData<MediaItem>>,
    onDismiss: () -> Unit,
    onPhotosSelected: (List<MediaItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onDismiss)

    val pagingItems = mediaPagingFlow.collectAsLazyPagingItems()
    val selectedPhotos = remember(initialSelectedPhotos) {
        mutableStateListOf<MediaItem>().apply {
            addAll(initialSelectedPhotos.filter { !it.isTrash && it.uri.isNotBlank() })
        }
    }
    val haptic = LocalHapticFeedback.current
    val gridState = rememberLazyGridState()

    val displayTitle = if (albumTitle.isNotBlank()) {
        stringResource(R.string.album_form_add_to_album, albumTitle)
    } else {
        stringResource(R.string.album_form_add_to_album, stringResource(R.string.album_form_album_title))
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
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.items_selected_count, selectedPhotos.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onPhotosSelected(selectedPhotos.toList())
                        onDismiss()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_save),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Timeline Grid of All Photos
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 4.dp,
                    end = 4.dp,
                    top = 4.dp,
                    bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { index -> pagingItems.peek(index)?.id ?: index },
                    contentType = { "picker_item" }
                ) { index ->
                    val item = pagingItems[index]
                    if (item != null) {
                        val isSelected = selectedPhotos.any { it.id == item.id }
                        val selectionIndex = selectedPhotos.indexOfFirst { it.id == item.id }.takeIf { it >= 0 }?.let { it + 1 }
                        val cellShape = getGridItemShape(
                            index = index,
                            totalCount = pagingItems.itemCount,
                            columnsCount = 3,
                            cornerRadius = 16.dp
                        )

                        NierikaPhotoGridItem(
                            uri = item.uri,
                            displayName = item.displayName,
                            isVideo = item.isVideo,
                            isGif = item.isGif,
                            durationMillis = item.durationMillis,
                            isFavorite = item.isFavorite,
                            isSelected = isSelected,
                            selectionIndex = selectionIndex,
                            isSelectionModeActive = true,
                            cellShape = cellShape,
                            targetRatio = 1.0f,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                if (isSelected) {
                                    selectedPhotos.removeAll { it.id == item.id }
                                } else {
                                    selectedPhotos.add(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
