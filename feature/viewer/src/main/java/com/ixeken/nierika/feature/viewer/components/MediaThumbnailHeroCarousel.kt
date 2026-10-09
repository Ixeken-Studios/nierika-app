package com.ixeken.nierika.feature.viewer.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.domain.model.MediaItem

/**
 * Carrusel interactivo inferior de miniaturas sincronizado con el Pager del visor.
 *
 * Utiliza fisicas espaciales de Material 3 Expressive (nierikaMotionScheme) para
 * expandir suavemente el elemento activo mientras mantiene 120 fps de respuesta tactil.
 */
@Composable
fun MediaThumbnailHeroCarousel(
    mediaItems: List<MediaItem>,
    pagerState: PagerState,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaItems.size <= 1) return

    val motionScheme = MaterialTheme.nierikaMotionScheme
    val listState = rememberLazyListState()

    // Sincronizar suavemente la miniatura centrada cuando cambia la pagina activa del visor sin interferir con rotaciones de pantalla
    LaunchedEffect(pagerState.currentPage, mediaItems.size) {
        if (mediaItems.isNotEmpty()) {
            val targetIndex = pagerState.currentPage.coerceIn(0, mediaItems.size - 1)
            runCatching {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val horizontalContentPadding = if (maxWidth.isSpecified && maxWidth > 54.dp) {
            ((maxWidth / 2) - 27.dp).coerceAtLeast(0.dp)
        } else {
            0.dp
        }

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = horizontalContentPadding)
        ) {
            itemsIndexed(
                items = mediaItems,
                key = { index, item -> "${item.id}_$index" },
                contentType = { _, _ -> "thumbnail_item" }
            ) { index, item ->
                val isSelected = index == pagerState.currentPage

                val width by animateDpAsState(
                    targetValue = if (isSelected) 54.dp else 18.dp,
                    animationSpec = motionScheme.fastSpatialSpec(),
                    label = "thumbnailWidth"
                )

                val height by animateDpAsState(
                    targetValue = if (isSelected) 54.dp else 50.dp,
                    animationSpec = motionScheme.fastSpatialSpec(),
                    label = "thumbnailHeight"
                )

                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.65f,
                    animationSpec = motionScheme.fastEffectsSpec(),
                    label = "thumbnailAlpha"
                )

                val cornerRadius by animateDpAsState(
                    targetValue = if (isSelected) 16.dp else 25.dp,
                    animationSpec = motionScheme.fastSpatialSpec(),
                    label = "thumbnailCorner"
                )

                val shape = RoundedCornerShape(cornerRadius)
                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .size(width = width, height = height)
                        .graphicsLayer { this.alpha = alpha }
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = shape
                                )
                            } else Modifier
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectIndex(index) }
                        )
                ) {
                    val context = LocalContext.current
                    val heroThumbnailRequest = remember(item.uri) {
                        ImageRequest.Builder(context)
                            .data(item.uri)
                            .size(160, 160)
                            .memoryCacheKey("${item.uri}_hero_160")
                            .crossfade(false)
                            .build()
                    }
                    AsyncImage(
                        model = heroThumbnailRequest,
                        contentDescription = item.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
