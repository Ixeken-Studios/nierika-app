package com.ixeken.nierika.feature.viewer.components

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.text.Text
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlin.math.roundToInt

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer

/**
 * Modelo inmutable para bloques de texto fusionados espacialmente.
 */
data class MergedTextBlock(
    val text: String,
    val boundingBox: Rect
)

/**
 * Algoritmo que fusiona bloques de texto cercanos espacialmente (párrafos continuos, títulos con subtítulos)
 * unificando sus recuadros delimitadores y concatenando su texto para evitar fragmentación.
 */
fun mergeAdjacentTextBlocks(blocks: List<Text.TextBlock>): List<MergedTextBlock> {
    val validBlocks = blocks.mapNotNull { block ->
        val box = block.boundingBox
        if (box != null && block.text.isNotBlank()) {
            MergedTextBlock(text = block.text.trim(), boundingBox = Rect(box))
        } else null
    }

    if (validBlocks.isEmpty()) return emptyList()

    val mergedList = mutableListOf<MergedTextBlock>()
    val visited = BooleanArray(validBlocks.size) { false }

    for (i in validBlocks.indices) {
        if (visited[i]) continue

        var currentText = validBlocks[i].text
        val currentBox = Rect(validBlocks[i].boundingBox)
        visited[i] = true

        var mergedAny: Boolean
        do {
            mergedAny = false
            for (j in validBlocks.indices) {
                if (visited[j]) continue

                val otherBox = validBlocks[j].boundingBox
                val avgHeight = (currentBox.height() + otherBox.height()) / 2f
                val verticalGap = kotlin.math.max(0, kotlin.math.max(otherBox.top - currentBox.bottom, currentBox.top - otherBox.bottom))
                val horizontalOverlap = kotlin.math.max(0, kotlin.math.min(currentBox.right, otherBox.right) - kotlin.math.max(currentBox.left, otherBox.left))
                val minWidth = kotlin.math.min(currentBox.width(), otherBox.width()).coerceAtLeast(1)

                val isVerticallyClose = verticalGap <= (avgHeight * 1.25f)
                val isHorizontallyOverlapping = (horizontalOverlap.toFloat() / minWidth.toFloat()) >= 0.25f

                if (isVerticallyClose && isHorizontallyOverlapping) {
                    visited[j] = true
                    if (otherBox.top < currentBox.top) {
                        currentText = validBlocks[j].text + "\n" + currentText
                    } else {
                        currentText = currentText + "\n" + validBlocks[j].text
                    }
                    currentBox.union(otherBox)
                    mergedAny = true
                }
            }
        } while (mergedAny)

        mergedList.add(MergedTextBlock(text = currentText, boundingBox = currentBox))
    }

    return mergedList
}

/**
 * Superposición interactiva estilo Google Lens / Apple Live Text.
 * 
 * Mantiene la imagen original 100% visible, permitiendo seleccionar texto de forma nativa
 * arrastrando sobre cualquier palabra o tocando para copiarlo directamente.
 */
@Composable
fun OcrTextOverlay(
    imageWidth: Int,
    imageHeight: Int,
    textBlocks: List<Text.TextBlock>,
    onClose: () -> Unit,
    onCopyAll: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var isFormattedSheetVisible by remember { mutableStateOf(false) }

    val mergedBlocks = remember(textBlocks) { mergeAdjacentTextBlocks(textBlocks) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        if (imageWidth > 0 && imageHeight > 0 && containerWidthPx > 0 && containerHeightPx > 0) {
            val imgAspect = imageWidth.toFloat() / imageHeight.toFloat()
            val containerAspect = containerWidthPx / containerHeightPx

            val (dispWidth, dispHeight) = if (imgAspect > containerAspect) {
                containerWidthPx to (containerWidthPx / imgAspect)
            } else {
                (containerHeightPx * imgAspect) to containerHeightPx
            }

            val offsetX = (containerWidthPx - dispWidth) / 2f
            val offsetY = (containerHeightPx - dispHeight) / 2f

            val scaleX = dispWidth / imageWidth.toFloat()
            val scaleY = dispHeight / imageHeight.toFloat()

            // 1. Capa de selección nativa con resaltados sobre cada bloque detectado
            SelectionContainer(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    mergedBlocks.forEach { block ->
                        val box: Rect = block.boundingBox
                        val leftPx = offsetX + box.left * scaleX
                        val topPx = offsetY + box.top * scaleY
                        val widthPx = (box.right - box.left) * scaleX
                        val heightPx = (box.bottom - box.top) * scaleY

                        val widthDp = with(density) { widthPx.toDp() }
                        val heightDp = with(density) { heightPx.toDp() }

                        val lineCount = block.text.lines().size.coerceAtLeast(1)
                        val estimatedFontSize = with(density) {
                            ((heightPx / lineCount) * 0.75f).coerceIn(12f, 36f).toSp()
                        }

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                                .width(widthDp.coerceAtLeast(20.dp))
                                .height(heightDp.coerceAtLeast(14.dp))
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(context.getString(R.string.menu_detect_text), block.text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, context.getString(R.string.ocr_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = block.text,
                                fontSize = estimatedFontSize,
                                lineHeight = estimatedFontSize * 1.15f,
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        DisableSelection {
            // Insignia superior M3 AssistChip informando bloques detectados
            AssistChip(
                onClick = { isFormattedSheetVisible = true },
                label = {
                    Text(
                        text = stringResource(R.string.ocr_text_blocks_found, mergedBlocks.size),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )

            // Barra inferior de acciones flotantes tipo cápsula
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón "Copy all"
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val fullText = mergedBlocks.joinToString("\n\n") { it.text }
                            onCopyAll(fullText)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.ocr_action_copy_all),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón "Expand BottomSheet"
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isFormattedSheetVisible = true
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        buttonSize = 42.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandLess,
                            contentDescription = stringResource(R.string.ocr_peek_expand_hint),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón "Close" en rojo con IconButtonWithAnimatedShape
                    IconButtonWithAnimatedShape(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClose()
                        },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        buttonSize = 42.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal BottomSheet con el texto estructurado en párrafos legibles
    if (isFormattedSheetVisible) {
        OcrFormattedTextBottomSheet(
            mergedBlocks = mergedBlocks,
            onDismiss = { isFormattedSheetVisible = false },
            onCopyAll = onCopyAll,
            onShare = { fullText ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, fullText)
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }
        )
    }
}

/**
 * Modal BottomSheet de Material 3 que despliega todo el texto detectado estructurado
 * en tarjetas legibles por párrafo con acciones de copiar individual, copiar todo y compartir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrFormattedTextBottomSheet(
    mergedBlocks: List<MergedTextBlock>,
    onDismiss: () -> Unit,
    onCopyAll: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val fullText = remember(mergedBlocks) { mergedBlocks.joinToString("\n\n") { it.text } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Encabezado principal del BottomSheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.menu_detect_text),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.ocr_text_blocks_found, mergedBlocks.size),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Acción Compartir
                    IconButton(
                        onClick = { onShare(fullText) }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.ocr_action_share),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Acción Copiar Todo
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onCopyAll(fullText) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FileCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.ocr_action_copy_all),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Lista de párrafos estructurados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(mergedBlocks) { index, block ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.ocr_paragraph_title, index + 1),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(context.getString(R.string.ocr_paragraph_title, index + 1), block.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, context.getString(R.string.ocr_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FileCopy,
                                        contentDescription = stringResource(R.string.ocr_action_copy_paragraph),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            SelectionContainer {
                                Text(
                                    text = block.text,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
