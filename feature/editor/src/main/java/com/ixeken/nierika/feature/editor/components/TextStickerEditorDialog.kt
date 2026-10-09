package com.ixeken.nierika.feature.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.feature.editor.model.TextBackgroundStyle
import com.ixeken.nierika.feature.editor.model.TextStickerData

private val textColors = listOf(
    Color.White,
    Color.Black,
    Color(0xFFE53935), // Red
    Color(0xFFFFB300), // Amber / Yellow
    Color(0xFF43A047), // Green
    Color(0xFF1E88E5), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF8E24AA)  // Purple
)

/**
 * Diálogo de pantalla completa estilo Stories para redactar y estilizar texto flotante.
 */
@Composable
fun TextStickerEditorDialog(
    initialSticker: TextStickerData? = null,
    onDismiss: () -> Unit,
    onConfirm: (TextStickerData) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var text by remember(initialSticker) { mutableStateOf(initialSticker?.text ?: "") }
    var selectedColor by remember(initialSticker) { mutableStateOf(initialSticker?.color ?: Color.White) }
    var bgStyle by remember(initialSticker) { mutableStateOf(initialSticker?.bgStyle ?: TextBackgroundStyle.SEMI_TRANSPARENT) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Barra superior de acciones (Cancelar, Cambiar fondo y Confirmar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_cancel),
                        tint = Color.White
                    )
                }

                // Botón para alternar estilo de fondo del texto
                Surface(
                    shape = CircleShape,
                    color = when (bgStyle) {
                        TextBackgroundStyle.TRANSPARENT -> Color.White.copy(alpha = 0.2f)
                        TextBackgroundStyle.SEMI_TRANSPARENT -> Color.White.copy(alpha = 0.5f)
                        TextBackgroundStyle.SOLID -> Color.White
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            bgStyle = when (bgStyle) {
                                TextBackgroundStyle.TRANSPARENT -> TextBackgroundStyle.SEMI_TRANSPARENT
                                TextBackgroundStyle.SEMI_TRANSPARENT -> TextBackgroundStyle.SOLID
                                TextBackgroundStyle.SOLID -> TextBackgroundStyle.TRANSPARENT
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatColorFill,
                        contentDescription = stringResource(R.string.text_style_button),
                        tint = if (bgStyle == TextBackgroundStyle.SOLID) Color.Black else Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }

                // Botón de Confirmar / Listo
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val sticker = (initialSticker ?: TextStickerData(text = text.trim())).copy(
                                text = text.trim(),
                                color = selectedColor,
                                bgStyle = bgStyle
                            )
                            onConfirm(sticker)
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.action_done),
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            // Área central de entrada de texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                val textColor = if (bgStyle == TextBackgroundStyle.SOLID && selectedColor == Color.White) Color.Black else selectedColor
                val backgroundShape = RoundedCornerShape(12.dp)
                val backgroundColor = when (bgStyle) {
                    TextBackgroundStyle.TRANSPARENT -> Color.Transparent
                    TextBackgroundStyle.SEMI_TRANSPARENT -> Color.Black.copy(alpha = 0.6f)
                    TextBackgroundStyle.SOLID -> if (selectedColor == Color.White) Color.White else selectedColor.copy(alpha = 0.9f)
                }
                val actualTextColor = when (bgStyle) {
                    TextBackgroundStyle.SOLID -> if (selectedColor == Color.White) Color.Black else Color.White
                    else -> selectedColor
                }

                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    textStyle = TextStyle(
                        color = actualTextColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (text.isNotBlank()) {
                            val sticker = (initialSticker ?: TextStickerData(text = text.trim())).copy(
                                text = text.trim(),
                                color = selectedColor,
                                bgStyle = bgStyle
                            )
                            onConfirm(sticker)
                        }
                    }),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .clip(backgroundShape)
                        .background(backgroundColor)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.text_input_placeholder),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Fila inferior: Paleta de colores flotante
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    textColors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 30.dp else 22.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    when {
                                        isSelected -> Modifier.border(2.5.dp, Color.White, CircleShape)
                                        color == Color.Black -> Modifier.border(1.dp, Color.Gray, CircleShape)
                                        else -> Modifier
                                    }
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedColor = color
                                }
                        )
                    }
                }
            }
        }
    }
}
