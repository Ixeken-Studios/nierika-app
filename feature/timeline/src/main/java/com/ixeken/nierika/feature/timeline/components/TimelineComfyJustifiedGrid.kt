package com.ixeken.nierika.feature.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.component.GifBadge
import com.ixeken.nierika.core.designsystem.component.VideoDurationBadge
import com.ixeken.nierika.core.designsystem.util.shimmerEffect
import com.ixeken.nierika.core.domain.model.MediaItem
import com.ixeken.nierika.core.designsystem.R

/**
 * Vista Comfy implementada con algoritmo de Filas Justificadas Adaptativas (Justified Row Layout Engine).
 * Agrupa las fotos en filas con altura dinámica respetando los aspect ratios reales sin dejar espacios vacíos.
 */
@Composable
fun TimelineComfyJustifiedGrid(
    showDatesInGrid: Boolean,
    groupedItems: List<Pair<String, List<MediaItem>>>,
    selectedMediaIds: SnapshotStateList<Long>,
    isSelectionModeActive: Boolean,
    snapshotItems: List<MediaItem>,
    comfyListState: LazyListState,
    onSelectIndex: (Int) -> Unit,
    isPickerMode: Boolean = false,
    allowMultiplePick: Boolean = false,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth - 12.dp
        val density = LocalDensity.current
        val totalWidthPx = with(density) { totalWidth.toPx() }
        val spacingPx = with(density) { 4.dp.toPx() }

        LazyColumn(
            state = comfyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 6.dp,
                end = 6.dp,
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 88.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) 24.dp else 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showDatesInGrid) {
                groupedItems.forEach { (dateHeader, groupPhotos) ->
                    item(key = "header_$dateHeader") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateHeader,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val rows = partitionIntoJustifiedRows(groupPhotos, totalWidthPx, spacingPx)

                    items(
                        count = rows.size,
                        key = { rowIndex -> rows[rowIndex].firstOrNull()?.id ?: rowIndex }
                    ) { rowIndex ->
                        val row = rows[rowIndex]
                        JustifiedRowLayout(
                            rowItems = row,
                            totalWidth = totalWidth,
                            selectedMediaIds = selectedMediaIds,
                            isSelectionModeActive = isSelectionModeActive,
                            snapshotItems = snapshotItems,
                            onSelectIndex = onSelectIndex,
                            isPickerMode = isPickerMode,
                            allowMultiplePick = allowMultiplePick
                        )
                    }
                }
            } else {
                val rows = partitionIntoJustifiedRows(snapshotItems, totalWidthPx, spacingPx)

                items(
                    count = rows.size,
                    key = { rowIndex -> rows[rowIndex].firstOrNull()?.id ?: rowIndex }
                ) { rowIndex ->
                    val row = rows[rowIndex]
                    JustifiedRowLayout(
                        rowItems = row,
                        totalWidth = totalWidth,
                        selectedMediaIds = selectedMediaIds,
                        isSelectionModeActive = isSelectionModeActive,
                        snapshotItems = snapshotItems,
                        onSelectIndex = onSelectIndex,
                        isPickerMode = isPickerMode,
                        allowMultiplePick = allowMultiplePick
                    )
                }
            }
        }
    }
}

fun partitionIntoJustifiedRows(
    items: List<MediaItem>,
    containerWidthPx: Float,
    spacingPx: Float,
    targetRowHeightPx: Float = 520f
): List<List<MediaItem>> {
    if (items.isEmpty() || containerWidthPx <= 0f) return emptyList()

    val result = mutableListOf<List<MediaItem>>()
    var currentRow = mutableListOf<MediaItem>()
    var currentAspectSum = 0f

    for (item in items) {
        val rawRatio = if (item.aspectRatio > 0f) item.aspectRatio else 1.0f
        val clampedRatio = rawRatio.coerceIn(0.5f, 2.4f)

        if (clampedRatio > 1.85f) {
            if (currentRow.isNotEmpty()) {
                result.add(currentRow)
                currentRow = mutableListOf()
                currentAspectSum = 0f
            }
            result.add(listOf(item))
            continue
        }

        currentRow.add(item)
        currentAspectSum += clampedRatio

        val totalSpacing = (currentRow.size - 1) * spacingPx
        val estimatedWidth = (currentAspectSum * targetRowHeightPx) + totalSpacing

        if (estimatedWidth >= containerWidthPx || currentRow.size >= 3) {
            result.add(currentRow)
            currentRow = mutableListOf()
            currentAspectSum = 0f
        }
    }

    if (currentRow.isNotEmpty()) {
        result.add(currentRow)
    }

    return result
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun JustifiedRowLayout(
    rowItems: List<MediaItem>,
    totalWidth: Dp,
    selectedMediaIds: SnapshotStateList<Long>,
    isSelectionModeActive: Boolean,
    snapshotItems: List<MediaItem>,
    onSelectIndex: (Int) -> Unit,
    isPickerMode: Boolean = false,
    allowMultiplePick: Boolean = false
) {
    val spacing = 4.dp
    val spacingTotal = spacing * (rowItems.size - 1)
    val availableWidth = totalWidth - spacingTotal

    val aspectSum = rowItems.sumOf { item ->
        val raw = if (item.aspectRatio > 0f) item.aspectRatio else 1.0f
        raw.coerceIn(0.5f, 2.4f).toDouble()
    }.toFloat()

    val computedHeight = when {
        aspectSum > 0f && rowItems.size > 1 -> (availableWidth / aspectSum).coerceIn(110.dp, 320.dp)
        rowItems.size == 1 -> {
            val singleRatio = if (rowItems[0].aspectRatio > 0f) rowItems[0].aspectRatio.coerceIn(0.5f, 2.4f) else 1.0f
            (totalWidth / singleRatio).coerceIn(140.dp, 360.dp)
        }
        else -> 180.dp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(computedHeight),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        rowItems.forEach { item ->
            val rawRatio = if (item.aspectRatio > 0f) item.aspectRatio else 1.0f
            val clampedRatio = rawRatio.coerceIn(0.5f, 2.4f)

            val itemWidth = if (rowItems.size == 1) {
                totalWidth
            } else {
                availableWidth * (clampedRatio / aspectSum)
            }

            val isSelected = item.id in selectedMediaIds
            val cellShape = RoundedCornerShape(14.dp)
            val imageModifier = Modifier.fillMaxSize()

            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(cellShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .combinedClickable(
                        onClick = {
                            if (isSelectionModeActive || (isPickerMode && allowMultiplePick)) {
                                if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                            } else {
                                val globalIndex = snapshotItems.indexOf(item).coerceAtLeast(0)
                                onSelectIndex(globalIndex)
                            }
                        },
                        onLongClick = {
                            if (isSelected) selectedMediaIds.remove(item.id) else selectedMediaIds.add(item.id)
                        }
                    )
            ) {
                if (item.uri.isNotEmpty()) {
                    val context = LocalContext.current
                    val thumbnailRequest = remember(item.uri) {
                        ImageRequest.Builder(context)
                            .data(item.uri)
                            .size(512, 512)
                            .memoryCacheKey("${item.uri}_thumb_512")
                            .crossfade(false)
                            .build()
                    }
                    val painter = rememberAsyncImagePainter(model = thumbnailRequest)
                    val painterState by painter.state.collectAsState()
                    Box(modifier = imageModifier) {
                        if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Empty) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmerEffect()
                            )
                        }
                        Image(
                            painter = painter,
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (item.isGif) {
                        GifBadge(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(5.dp)
                        )
                    } else if (item.isVideo) {
                        VideoDurationBadge(
                            durationMillis = item.durationMillis,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(5.dp)
                        )
                    }
                    if (item.isFavorite) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = stringResource(R.string.action_favorite),
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                if (isSelectionModeActive) {
                    val selectionIndex = if (isSelected) selectedMediaIds.indexOf(item.id) + 1 else null
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(MaterialTheme.colorScheme.primary)
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                } else {
                                    Modifier.border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected && selectionIndex != null) {
                            Text(
                                text = selectionIndex.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
