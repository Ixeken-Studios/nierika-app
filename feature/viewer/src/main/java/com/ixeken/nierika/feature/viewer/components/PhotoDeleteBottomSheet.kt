package com.ixeken.nierika.feature.viewer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.ButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.FilledTonalButtonWithAnimatedShape
import com.ixeken.nierika.core.domain.model.MediaItem

/**
 * Modal BottomSheet moderno estilo Material 3 Expressive de confirmación para mover elementos a la papelera.
 * Muestra el título centrado, subtítulo con conteo de elementos, vista previa con tarjetas redondeadas
 * y badge de conteo en la esquina superior, junto a botones táctiles alargados "Cancel" y "Confirm".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDeleteBottomSheet(
    isVisible: Boolean,
    items: List<MediaItem> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    isPermanentDelete: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val count = items.size.coerceAtLeast(1)

    val title = if (isPermanentDelete) {
        stringResource(R.string.delete_permanently_title)
    } else {
        stringResource(R.string.moving_to_trash_title)
    }

    val subtitle = if (count == 1) {
        "1 item"
    } else {
        "$count items"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título centrado
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtítulo con cantidad de items
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Contenedor central de miniaturas con badge
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (items.isEmpty()) {
                    // Fallback si la lista viene vacía
                    Box(
                        modifier = Modifier
                            .size(width = 100.dp, height = 140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    )
                } else if (items.size == 1) {
                    // 1 solo elemento
                    Box(
                        modifier = Modifier
                            .size(width = 100.dp, height = 140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        AsyncImage(
                            model = items.first().uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Múltiples elementos: Stack con miniaturas asomándose y badge de conteo en la esquina superior
                    val firstUri = items.getOrNull(0)?.uri
                    val secondUri = items.getOrNull(1)?.uri
                    val thirdUri = items.getOrNull(2)?.uri

                    // Tarjeta de fondo izquierda
                    if (secondUri != null) {
                        Box(
                            modifier = Modifier
                                .size(width = 90.dp, height = 130.dp)
                                .offset(x = (-22).dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(18.dp))
                        ) {
                            AsyncImage(
                                model = secondUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Tarjeta de fondo derecha
                    if (thirdUri != null) {
                        Box(
                            modifier = Modifier
                                .size(width = 90.dp, height = 130.dp)
                                .offset(x = 22.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(18.dp))
                        ) {
                            AsyncImage(
                                model = thirdUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Tarjeta principal frontal
                    Box(
                        modifier = Modifier
                            .size(width = 96.dp, height = 138.dp)
                            .zIndex(2f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
                    ) {
                        if (firstUri != null) {
                            AsyncImage(
                                model = firstUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Badge circular con el total de elementos en la esquina superior derecha
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .zIndex(3f)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones inferiores Cancel y Confirm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButtonWithAnimatedShape(
                    onClick = onDismiss,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ButtonWithAnimatedShape(
                    onClick = {
                        onDismiss()
                        onConfirmDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPermanentDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isPermanentDelete) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
