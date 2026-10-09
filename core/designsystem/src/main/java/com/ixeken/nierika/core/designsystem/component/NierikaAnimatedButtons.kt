package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Botón primario Material 3 Expressive con morphing dinámico de forma al interactuar (Píldora a Squircle).
 */
@Composable
fun ButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "buttonShapeMorph"
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(animatedCornerPercent),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Botón tonal Material 3 Expressive con morphing dinámico de forma al interactuar.
 */
@Composable
fun FilledTonalButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "filledTonalButtonShapeMorph"
    )

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(animatedCornerPercent),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Botón elevado Material 3 Expressive con morphing dinámico de forma al interactuar.
 */
@Composable
fun ElevatedButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "elevatedButtonShapeMorph"
    )

    ElevatedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(animatedCornerPercent),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Botón delineado Material 3 Expressive con morphing dinámico de forma al interactuar.
 */
@Composable
fun OutlinedButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "outlinedButtonShapeMorph"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(animatedCornerPercent),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Botón de texto plano Material 3 Expressive con morphing dinámico de forma en el contenedor de interacción.
 */
@Composable
fun TextButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "textButtonShapeMorph"
    )

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(animatedCornerPercent),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Botón de icono Material 3 Expressive con morphing dinámico de forma (Círculo 50% a Squircle 26%).
 */
@Composable
fun IconButtonWithAnimatedShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonSize: Dp = 48.dp,
    enforceMinTouchTarget: Boolean = true,
    restCornerPercent: Int = 50,
    pressedCornerPercent: Int = 26,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 0.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionScheme = MaterialTheme.nierikaMotionScheme

    val animatedCornerPercent by animateIntAsState(
        targetValue = if (isPressed) pressedCornerPercent else restCornerPercent,
        animationSpec = motionScheme.fastSpatialSpec(),
        label = "iconButtonShapeMorph"
    )

    val shape = RoundedCornerShape(animatedCornerPercent)

    Surface(
        shape = shape,
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.38f),
        shadowElevation = shadowElevation,
        modifier = modifier
            .then(if (enforceMinTouchTarget) Modifier.minimumInteractiveComponentSize() else Modifier)
            .size(buttonSize)
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(
                LocalContentColor provides (if (enabled) contentColor else contentColor.copy(alpha = 0.38f))
            ) {
                content()
            }
        }
    }
}
