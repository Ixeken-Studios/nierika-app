package com.ixeken.nierika.feature.viewer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.component.NierikaRichTooltipBox

/**
 * Barra inferior de acciones del visor multimedia.
 * Dispone de forma simétrica el botón de Compartir en el extremo izquierdo, la píldora de acciones
 * en el centro y el botón de Eliminar/Papelera en el extremo derecho, respondiendo ergonómicamente
 * al formato vertical u horizontal.
 */
@Composable
fun MediaViewerBottomBar(
    isLandscape: Boolean,
    isCompactLandscape: Boolean,
    isItemInTrash: Boolean,
    isInVault: Boolean,
    isFavorite: Boolean,
    adaptiveHorizontalPadding: Dp,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPickMedia: (() -> Unit)? = null,
    onRestoreClick: () -> Unit = {},
    onRestoreFromVaultClick: (() -> Unit)? = null,
    onInfoClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onRemoveFromAlbumClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val buttonSize = if (isLandscape) 40.dp else 48.dp
    val iconSize = if (isLandscape) 18.dp else 24.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = adaptiveHorizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isItemInTrash) {
            NierikaPlainTooltipBox(
                tooltipText = stringResource(R.string.action_share)
            ) {
                IconButtonWithAnimatedShape(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShareClick()
                    },
                    buttonSize = buttonSize,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = stringResource(R.string.action_share),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(buttonSize))
        }

        if (!isCompactLandscape) {
            MediaViewerActionsPill(
                isCompact = false,
                isLandscape = isLandscape,
                isItemInTrash = isItemInTrash,
                isInVault = isInVault,
                isFavorite = isFavorite,
                onPickMedia = onPickMedia,
                onRestoreClick = onRestoreClick,
                onRestoreFromVaultClick = onRestoreFromVaultClick,
                onInfoClick = onInfoClick,
                onEditClick = onEditClick,
                onToggleFavorite = onToggleFavorite,
                onRemoveFromAlbumClick = onRemoveFromAlbumClick
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        NierikaRichTooltipBox(
            title = stringResource(if (isItemInTrash || isInVault) R.string.vault_action_delete_permanent else R.string.tooltip_move_to_trash_title),
            text = stringResource(if (isItemInTrash || isInVault) R.string.vault_delete_dialog_desc else R.string.tooltip_move_to_trash_desc)
        ) {
            IconButtonWithAnimatedShape(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteClick()
                },
                buttonSize = buttonSize,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = if (isItemInTrash || isInVault) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
