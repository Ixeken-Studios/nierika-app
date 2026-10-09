package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R

/**
 * TopBar oficial para el modo de Selección Múltiple según Material 3 Expressive.
 * Muestra el contador en cápsula interactiva con botón de cierre ("X items selected")
 * y el botón de Seleccionar Todo / Deseleccionar Todo a la derecha.
 */
@Composable
fun NierikaSelectionTopBar(
    selectedCount: Int,
    isAllSelected: Boolean,
    onClearSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cápsula izquierda de cierre y conteo
        NierikaPlainTooltipBox(
            tooltipText = stringResource(R.string.tooltip_cancel_selection)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onClearSelection)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                AnimatedContent(
                    targetState = selectedCount,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "selectedCountAnim"
                ) { count ->
                    Text(
                        text = stringResource(R.string.items_selected_count, count),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Botón derecho de Seleccionar Todo
        NierikaPlainTooltipBox(
            tooltipText = stringResource(R.string.tooltip_select_all)
        ) {
            IconButton(
                onClick = onToggleSelectAll,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    imageVector = Icons.Filled.SelectAll,
                    contentDescription = stringResource(R.string.tooltip_select_all),
                    tint = if (isAllSelected && selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * BottomBar oficial para el modo de Selección Múltiple según Material 3 Expressive.
 * Despliega uniformemente los botones de acción contextuales: Quitar del álbum, Compartir, Favorito, Agregar a Álbum y Mover a Papelera.
 */
@Composable
fun NierikaSelectionBottomBar(
    onRemoveFromAlbumClick: (() -> Unit)? = null,
    onRestoreClick: (() -> Unit)? = null,
    onShareClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    isAllFavorites: Boolean = false,
    onAddToAlbumClick: (() -> Unit)? = null,
    onSelectClick: (() -> Unit)? = null,
    onDeleteClick: () -> Unit,
    deleteLabel: String = stringResource(R.string.selection_action_trash),
    deleteIcon: ImageVector = Icons.Filled.Delete,
    deleteColor: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Quitar del Álbum (si aplica a álbum personalizado)
            if (onRemoveFromAlbumClick != null) {
                SelectionBarButton(
                    icon = Icons.Filled.FolderDelete,
                    label = stringResource(R.string.selection_action_remove_from_album),
                    contentDescription = stringResource(R.string.action_remove_from_album),
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = onRemoveFromAlbumClick
                )
            }

            // 2. Restaurar (si aplica a papelera)
            if (onRestoreClick != null) {
                SelectionBarButton(
                    icon = Icons.Filled.RestoreFromTrash,
                    label = stringResource(R.string.selection_action_restore),
                    contentDescription = stringResource(R.string.action_restore_photo),
                    iconTint = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.primary,
                    onClick = onRestoreClick
                )
            }

            // 3. Compartir
            SelectionBarButton(
                icon = Icons.Filled.Share,
                label = stringResource(R.string.selection_action_share),
                contentDescription = stringResource(R.string.action_share),
                iconTint = MaterialTheme.colorScheme.onSurface,
                textColor = MaterialTheme.colorScheme.onSurface,
                onClick = onShareClick
            )

            // 4. Favoritos (si no está en papelera)
            if (onFavoriteClick != null) {
                SelectionBarButton(
                    icon = if (isAllFavorites) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = stringResource(R.string.selection_action_favorite),
                    contentDescription = stringResource(
                        if (isAllFavorites) R.string.action_remove_from_favorite else R.string.action_add_to_favorite
                    ),
                    iconTint = if (isAllFavorites) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface,
                    textColor = if (isAllFavorites) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface,
                    onClick = onFavoriteClick
                )
            }

            // 5. Agregar a Álbum
            if (onAddToAlbumClick != null) {
                SelectionBarButton(
                    icon = Icons.Filled.CreateNewFolder,
                    label = stringResource(R.string.selection_action_album),
                    contentDescription = stringResource(R.string.action_add_to_album),
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onAddToAlbumClick
                )
            }

            // 6. Seleccionar (si Nierika fue invocada como selector de fotos/videos)
            if (onSelectClick != null) {
                SelectionBarButton(
                    icon = Icons.Filled.Check,
                    label = stringResource(R.string.action_select),
                    contentDescription = stringResource(R.string.action_select),
                    iconTint = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.primary,
                    onClick = onSelectClick
                )
            }

            // 7. Papelera / Eliminar
            SelectionBarButton(
                icon = deleteIcon,
                label = deleteLabel,
                contentDescription = deleteLabel,
                iconTint = deleteColor,
                textColor = deleteColor,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun RowScope.SelectionBarButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                lineHeight = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 2,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
