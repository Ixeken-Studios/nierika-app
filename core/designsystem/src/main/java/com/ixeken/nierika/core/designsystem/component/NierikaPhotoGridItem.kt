package com.ixeken.nierika.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.util.shimmerEffect

/**
 * Componente estándar de celda fotográfica para cuadrículas en toda la aplicación Nierika.
 * Proporciona soporte uniforme para recorte según posición periférica de cuadrícula (`cellShape`),
 * insignias de vídeo, favoritos en color sólido, orden/check de selección múltiple,
 * carga optimizada con Coil 3 y animación de onda `shimmerEffect`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NierikaPhotoGridItem(
    uri: String,
    displayName: String? = null,
    isVideo: Boolean = false,
    isGif: Boolean = false,
    durationMillis: Long = 0L,
    isFavorite: Boolean = false,
    isSelected: Boolean = false,
    selectionIndex: Int? = null,
    isSelectionModeActive: Boolean = false,
    cellShape: Shape = RoundedCornerShape(0.dp),
    targetRatio: Float = 1.0f,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(targetRatio)
            .clip(cellShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh
                else MaterialTheme.colorScheme.surface
            )
            .padding(if (isSelected) 4.dp else 0.dp)
            .clip(cellShape)
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    ) {
        if (uri.isNotEmpty()) {
            val context = LocalContext.current
            val thumbnailRequest = remember(uri) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .size(512, 512)
                    .memoryCacheKey("${uri}_thumb_512")
                    .crossfade(false)
                    .build()
            }
            val painter = rememberAsyncImagePainter(model = thumbnailRequest)
            val painterState by painter.state.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                if (painterState is AsyncImagePainter.State.Loading || painterState is AsyncImagePainter.State.Empty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmerEffect()
                    )
                }
                Image(
                    painter = painter,
                    contentDescription = displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isGif && !isVideo) {
                GifBadge(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                )
            } else if (isVideo) {
                VideoDurationBadge(
                    durationMillis = durationMillis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(5.dp)
                )
            }

            if (isFavorite) {
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
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
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Insignia ergonómica y ultracompacta para indicar la duración de videos en cuadrículas.
 * Utiliza una cápsula con esquinas suaves de 6dp, fondo semitransparente oscuro de alto contraste
 * y tipografía compacta (10.5sp) con micro-icono sólido para no saturar la miniatura.
 */
@Composable
fun VideoDurationBadge(
    durationMillis: Long = 0L,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(durationMillis) {
        if (durationMillis <= 0L) ""
        else {
            val totalSeconds = durationMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            if (hours > 0) {
                String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
            }
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 4.5.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(10.dp)
        )
        if (formattedTime.isNotEmpty()) {
            Text(
                text = formattedTime,
                color = Color.White,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 11.sp
            )
        }
    }
}

/**
 * Insignia visual compacta M3 Expressive para elementos GIF animados.
 * Despliega una pastilla de esquinas suaves de 6dp con texto en negrita y alto contraste
 * para identificar imágenes animadas sin tratarlas como archivos de video.
 */
@Composable
fun GifBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GIF",
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 11.sp
        )
    }
}

