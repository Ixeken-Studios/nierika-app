package com.ixeken.nierika.feature.viewer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Píldora flotante de acciones rápidas del visor multimedia.
 * Centraliza los botones primarios interactivos con retroalimentación háptica (Seleccionar, Restaurar,
 * Bóveda, Editar, Favorito, Información y Desvincular de Álbum), adaptando sus dimensiones según
 * la orientación apaisada o vertical.
 */
@Composable
fun MediaViewerActionsPill(
    isCompact: Boolean = true,
    isLandscape: Boolean = false,
    isItemInTrash: Boolean,
    isInVault: Boolean,
    isFavorite: Boolean,
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
    val isCompactMode = isCompact || isLandscape
    val pillHeight = if (isCompactMode) 32.dp else 48.dp
    val btnSize = if (isCompactMode) 28.dp else 40.dp
    val icnSize = if (isCompactMode) 16.dp else 22.dp

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 4.dp,
        modifier = modifier
            .clip(CircleShape)
            .height(pillHeight)
    ) {
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides Dp.Unspecified
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = if (isCompactMode) 4.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactMode) 2.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (onPickMedia != null) {
                    NierikaPlainTooltipBox(
                        tooltipText = stringResource(R.string.action_select)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPickMedia()
                            },
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = stringResource(R.string.action_select),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }
                }

                if (isItemInTrash) {
                    NierikaPlainTooltipBox(
                        tooltipText = stringResource(R.string.menu_restore)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onRestoreClick()
                            },
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestoreFromTrash,
                                contentDescription = stringResource(R.string.menu_restore),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }

                    NierikaRichTooltipBox(
                        title = stringResource(R.string.tooltip_exif_info_title),
                        text = stringResource(R.string.tooltip_exif_info_desc)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = onInfoClick,
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(R.string.action_info),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }
                } else if (isInVault) {
                    if (onRestoreFromVaultClick != null) {
                        NierikaPlainTooltipBox(
                            tooltipText = stringResource(R.string.vault_action_restore)
                        ) {
                            IconButtonWithAnimatedShape(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRestoreFromVaultClick()
                                },
                                buttonSize = btnSize,
                                enforceMinTouchTarget = false,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LockOpen,
                                    contentDescription = stringResource(R.string.vault_action_restore),
                                    modifier = Modifier.size(icnSize)
                                )
                            }
                        }
                    }

                    NierikaRichTooltipBox(
                        title = stringResource(R.string.tooltip_exif_info_title),
                        text = stringResource(R.string.tooltip_exif_info_desc)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = onInfoClick,
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(R.string.action_info),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }
                } else {
                    NierikaPlainTooltipBox(
                        tooltipText = stringResource(R.string.tooltip_edit_media)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEditClick()
                            },
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.action_edit),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }

                    NierikaPlainTooltipBox(
                        tooltipText = stringResource(
                            if (isFavorite) R.string.tooltip_favorite_remove else R.string.tooltip_favorite_add
                        )
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleFavorite()
                            },
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.action_favorite),
                                modifier = Modifier.size(icnSize),
                                tint = if (isFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    NierikaRichTooltipBox(
                        title = stringResource(R.string.tooltip_exif_info_title),
                        text = stringResource(R.string.tooltip_exif_info_desc)
                    ) {
                        IconButtonWithAnimatedShape(
                            onClick = onInfoClick,
                            buttonSize = btnSize,
                            enforceMinTouchTarget = false,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(R.string.action_info),
                                modifier = Modifier.size(icnSize)
                            )
                        }
                    }

                    if (onRemoveFromAlbumClick != null) {
                        NierikaRichTooltipBox(
                            title = stringResource(R.string.tooltip_remove_from_album_title),
                            text = stringResource(R.string.tooltip_remove_from_album_desc)
                        ) {
                            IconButtonWithAnimatedShape(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRemoveFromAlbumClick()
                                },
                                buttonSize = btnSize,
                                enforceMinTouchTarget = false,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FolderDelete,
                                    contentDescription = stringResource(R.string.action_remove_from_album),
                                    modifier = Modifier.size(icnSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
