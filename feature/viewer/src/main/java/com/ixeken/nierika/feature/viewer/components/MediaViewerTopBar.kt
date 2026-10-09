package com.ixeken.nierika.feature.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.R
import com.ixeken.nierika.core.designsystem.component.IconButtonWithAnimatedShape
import com.ixeken.nierika.core.designsystem.component.NierikaPlainTooltipBox
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme

/**
 * Barra superior de navegación e información del visor multimedia.
 * Presenta el botón de retroceso accesible, la cápsula de fecha y hora localizada adaptativa
 * y el contenedor de acciones contextuales o menú de opciones con transiciones espaciales simétricas.
 */
@Composable
fun MediaViewerTopBar(
    isVisible: Boolean,
    isDraggingToDismiss: Boolean,
    isLandscape: Boolean,
    isCompactLandscape: Boolean,
    isInfoSheetVisible: Boolean,
    screenWidthDp: Int,
    adaptiveHorizontalPadding: Dp,
    dateLine1: String,
    timeLine2: String,
    compactDateTimeLine: String,
    onBackClick: () -> Unit,
    actionsContent: (@Composable () -> Unit)? = null,
    menuContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val motionScheme = MaterialTheme.nierikaMotionScheme

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            animationSpec = motionScheme.fastSpatialSpec(),
            initialOffsetY = { -it * 2 }
        ) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
        exit = if (isDraggingToDismiss) androidx.compose.animation.ExitTransition.None
        else slideOutVertically(
            animationSpec = motionScheme.fastSpatialSpec(),
            targetOffsetY = { -it * 2 }
        ) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = adaptiveHorizontalPadding,
                    vertical = if (isLandscape) 4.dp else if (screenWidthDp >= 600) 16.dp else 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NierikaPlainTooltipBox(
                    tooltipText = stringResource(R.string.tooltip_back)
                ) {
                    IconButtonWithAnimatedShape(
                        onClick = onBackClick,
                        buttonSize = if (isLandscape) 40.dp else 48.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                        )
                    }
                }

                if (isCompactLandscape && !isInfoSheetVisible) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .height(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = compactDateTimeLine,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (!isCompactLandscape && !isInfoSheetVisible) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .height(if (isLandscape) 32.dp else 48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(
                                horizontal = if (isLandscape) 12.dp else 16.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = dateLine1,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = if (isLandscape) 12.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "·",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = if (isLandscape) 12.sp else 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = timeLine2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (isLandscape) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (!isInfoSheetVisible) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionsContent?.invoke()
                    menuContent?.invoke()
                }
            }
        }
    }
}
