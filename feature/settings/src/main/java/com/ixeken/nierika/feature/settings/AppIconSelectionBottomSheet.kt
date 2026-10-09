package com.ixeken.nierika.feature.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.NierikaConnectedFlowButtonGroup
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import com.ixeken.nierika.core.domain.model.AppIconStyle
import com.ixeken.nierika.core.domain.model.AppLabelOption
import com.ixeken.nierika.core.domain.model.AppLauncherAlias

import com.ixeken.nierika.feature.settings.util.getDrawableRes
import com.ixeken.nierika.feature.settings.util.getTitleRes
import com.ixeken.nierika.feature.settings.util.getLabelRes

/**
 * Bottom Sheet modal de Material 3 Expressive que permite personalizar el icono y la etiqueta
 * del Launcher de manera independiente con previsualización en vivo.
 *
 * Mantiene un estado borrador local (draftIcon y draftLabel) durante la interacción del usuario
 * para evitar que la hoja se cierre prematuramente, ya que invocar setComponentEnabledSetting
 * en el PackageManager de Android invalida la pila de tareas del Launcher al instante. La persistencia
 * y cambio de alias únicamente se consolidan tras pulsar el botón de confirmación explícito.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppIconSelectionBottomSheet(
    isVisible: Boolean,
    currentAlias: AppLauncherAlias,
    onAliasSelected: (AppLauncherAlias) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return

    val motionScheme = MaterialTheme.nierikaMotionScheme
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val initialIcon = currentAlias.iconStyle
    val initialLabel = if (currentAlias.labelOption == AppLabelOption.CAMERA) {
        AppLabelOption.PHOTOS
    } else {
        currentAlias.labelOption
    }

    var draftIcon by remember(currentAlias) { mutableStateOf(initialIcon) }
    var draftLabel by remember(currentAlias) { mutableStateOf(initialLabel) }

    val allIcons = remember { AppIconStyle.entries }
    val allLabels = remember { AppLabelOption.entries.filter { it != AppLabelOption.CAMERA } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera compacta
            Column {
                Text(
                    text = stringResource(R.string.app_icon_bs_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(R.string.app_icon_bs_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Previsualización en Vivo compacta
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = draftIcon.getDrawableRes()),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(draftLabel.getLabelRes()),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Sección 1: Selector de Icono compacto (solo glifo)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.app_icon_section_icon),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(
                        items = allIcons,
                        key = { it.id },
                        contentType = { "app_icon_card" }
                    ) { iconStyle ->
                        val isSelected = iconStyle == draftIcon
                        val borderWidth by animateDpAsState(
                            targetValue = if (isSelected) 2.5.dp else 0.dp,
                            animationSpec = motionScheme.fastSpatialSpec(),
                            label = "iconBorder"
                        )
                        val cardScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = motionScheme.fastSpatialSpec(),
                            label = "iconScale"
                        )

                        val shape = RoundedCornerShape(15.dp)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .scale(cardScale)
                                .clip(shape)
                                .border(
                                    width = borderWidth,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = shape
                                )
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                                    }
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    val newLabel = when (iconStyle) {
                                        AppIconStyle.NEPO -> if (draftLabel == AppLabelOption.NEPO) AppLabelOption.NEPO else AppLabelOption.CALCULATOR
                                        AppIconStyle.DRAFTO -> if (draftLabel == AppLabelOption.DRAFTO) AppLabelOption.DRAFTO else AppLabelOption.NOTES
                                        else -> if (draftLabel.isCamouflage) AppLabelOption.NIERIKA else draftLabel
                                    }
                                    draftIcon = iconStyle
                                    draftLabel = newLabel
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = iconStyle.getDrawableRes()),
                                    contentDescription = stringResource(iconStyle.getTitleRes()),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Sección 2: Selector de Etiqueta (Nombre) con Flow Button Group compacto
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.app_icon_section_label),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                NierikaConnectedFlowButtonGroup(
                    items = allLabels,
                    selectedItem = draftLabel,
                    onItemSelected = { labelOption ->
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        val newIcon = when (labelOption) {
                            AppLabelOption.CALCULATOR, AppLabelOption.NEPO -> AppIconStyle.NEPO
                            AppLabelOption.NOTES, AppLabelOption.DRAFTO -> AppIconStyle.DRAFTO
                            else -> if (draftIcon.isCamouflage) AppIconStyle.PHOTO_RAINBOW else draftIcon
                        }
                        draftIcon = newIcon
                        draftLabel = labelOption
                    },
                    labelProvider = { labelOption ->
                        context.getString(labelOption.getLabelRes())
                    },
                    iconProvider = { labelOption ->
                        when (labelOption) {
                            AppLabelOption.NIERIKA -> Icons.Filled.AutoAwesome
                            AppLabelOption.GALLERY -> Icons.Filled.PhotoLibrary
                            AppLabelOption.PHOTOS -> Icons.Filled.Photo
                            AppLabelOption.LIBRARY -> Icons.Filled.CollectionsBookmark
                            AppLabelOption.CAMERA -> Icons.Filled.Photo
                            AppLabelOption.CALCULATOR, AppLabelOption.NEPO -> Icons.Filled.Calculate
                            AppLabelOption.NOTES, AppLabelOption.DRAFTO -> Icons.Filled.EditNote
                        }
                    },
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                    fontSize = 12.sp,
                    iconSize = 15.dp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Botón de Confirmación para aplicar los cambios de alias sin cierres prematuros
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val targetAlias = AppLauncherAlias.resolveAlias(draftIcon, draftLabel)
                    if (targetAlias != currentAlias) {
                        onAliasSelected(targetAlias)
                    }
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_icon_action_confirm),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
