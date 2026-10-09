package com.ixeken.nierika.feature.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.theme.ColorSchemeStyle
import com.ixeken.nierika.core.designsystem.theme.getM3ColorScheme

/**
 * Bottom Sheet M3 Expressive Modal para la selección de tema visual de Nierika.
 * Muestra las opciones con retícula de 3 columnas (3 per row) y extracción en tiempo real
 * del color primario del fondo de pantalla del sistema (Android 12+ Dynamic Color).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionBottomSheet(
    isVisible: Boolean,
    selectedStyle: ColorSchemeStyle,
    onStyleSelect: (ColorSchemeStyle) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_bs_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.theme_bs_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(bottom = 24.dp)
            ) {
                items(ColorSchemeStyle.entries.toTypedArray()) { style ->
                    ThemeCircleItem(
                        style = style,
                        isSelected = selectedStyle == style,
                        isDarkTheme = isDarkTheme,
                        onClick = { onStyleSelect(style) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCircleItem(
    style: ColorSchemeStyle,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val staticScheme = getM3ColorScheme(style, isDarkTheme)

    val circleColor = if (style == ColorSchemeStyle.DYNAMIC) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamicScheme = if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            dynamicScheme.primary
        } else {
            staticScheme.primary
        }
    } else {
        staticScheme.primary
    }

    val activeBorderColor = MaterialTheme.colorScheme.primary

    val labelName = when (style) {
        ColorSchemeStyle.DYNAMIC -> "Dynamic"
        ColorSchemeStyle.MONOCHROME -> "Monochrome"
        ColorSchemeStyle.PINK -> "Pink"
        ColorSchemeStyle.ROSE -> "Rose"
        ColorSchemeStyle.RED -> "Red"
        ColorSchemeStyle.ORANGE -> "Orange"
        ColorSchemeStyle.YELLOW -> "Yellow"
        ColorSchemeStyle.CHARTREUSE -> "Chartreuse"
        ColorSchemeStyle.GREEN -> "Green"
        ColorSchemeStyle.TEAL -> "Teal"
        ColorSchemeStyle.CYAN -> "Cyan"
        ColorSchemeStyle.BLUE -> "Blue"
        ColorSchemeStyle.INDIGO -> "Indigo"
        ColorSchemeStyle.PURPLE -> "Purple"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, activeBorderColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = labelName,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
