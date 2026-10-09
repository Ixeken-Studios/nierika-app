package com.ixeken.nierika.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ixeken.nierika.core.designsystem.theme.nierikaMotionScheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ScrollerHandleShape = RoundedCornerShape(
    topStart = 23.dp,
    bottomStart = 23.dp,
    topEnd = 6.dp,
    bottomEnd = 6.dp
)

/**
 * Componente NierikaFastScroller estilo Material 3 Expressive.
 * Mango de desplazamiento semi-circular plano al filo derecho de la pantalla.
 */
@Composable
fun NierikaFastScroller(
    gridState: LazyGridState,
    totalItemCount: Int,
    currentDateLabel: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badgeContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    badgeContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (totalItemCount <= 0) return

    val coroutineScope = rememberCoroutineScope()
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val density = LocalDensity.current
    val handleHeightPx = remember(density) { with(density) { 44.dp.toPx() } }

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }
    var dragYPx by remember { mutableFloatStateOf(0f) }

    val currentTotalItemCount by rememberUpdatedState(totalItemCount)

    val scrollFraction by remember {
        derivedStateOf {
            if (isDragging) {
                (dragYPx / trackHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
            } else {
                val layoutInfo = gridState.layoutInfo
                val totalVisibleItems = layoutInfo.visibleItemsInfo.size
                if (totalVisibleItems == 0 || layoutInfo.totalItemsCount == 0) {
                    0f
                } else {
                    val firstItemIndex = gridState.firstVisibleItemIndex.toFloat()
                    (firstItemIndex / (layoutInfo.totalItemsCount - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                trackHeightPx = coordinates.size.height.toFloat()
            }
    ) {
        AnimatedVisibility(
            visible = isVisible || isDragging,
            enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                    val yOffset = (scrollFraction * maxOffset).roundToInt()
                    IntOffset(0, yOffset)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = isDragging,
                    enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
                    exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(badgeContainerColor)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentDateLabel,
                            color = badgeContentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(width = 28.dp, height = 46.dp)
                        .clip(ScrollerHandleShape)
                        .background(if (isDragging) MaterialTheme.colorScheme.primaryContainer else containerColor)
                        .pointerInput(Unit) {
                            try {
                                detectVerticalDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                                        dragYPx = offset.y + (scrollFraction * maxOffset)
                                    },
                                    onDragEnd = { isDragging = false },
                                    onDragCancel = { isDragging = false },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragYPx = (dragYPx + dragAmount).coerceIn(0f, trackHeightPx)
                                        val fraction = (dragYPx / trackHeightPx).coerceIn(0f, 1f)
                                        val count = currentTotalItemCount
                                        val targetIndex = (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)

                                        coroutineScope.launch {
                                            gridState.scrollToItem(targetIndex)
                                        }
                                    }
                                )
                            } finally {
                                isDragging = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = null,
                        tint = if (isDragging) MaterialTheme.colorScheme.onPrimaryContainer else contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NierikaFastScroller(
    staggeredGridState: LazyStaggeredGridState,
    totalItemCount: Int,
    currentDateLabel: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badgeContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    badgeContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (totalItemCount <= 0) return

    val coroutineScope = rememberCoroutineScope()
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val density = LocalDensity.current
    val handleHeightPx = remember(density) { with(density) { 44.dp.toPx() } }

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }
    var dragYPx by remember { mutableFloatStateOf(0f) }

    val currentTotalItemCount by rememberUpdatedState(totalItemCount)

    val scrollFraction by remember {
        derivedStateOf {
            if (isDragging) {
                (dragYPx / trackHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
            } else {
                val layoutInfo = staggeredGridState.layoutInfo
                val totalVisibleItems = layoutInfo.visibleItemsInfo.size
                if (totalVisibleItems == 0 || layoutInfo.totalItemsCount == 0) {
                    0f
                } else {
                    val firstItemIndex = staggeredGridState.firstVisibleItemIndex.toFloat()
                    (firstItemIndex / (layoutInfo.totalItemsCount - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                trackHeightPx = coordinates.size.height.toFloat()
            }
    ) {
        AnimatedVisibility(
            visible = isVisible || isDragging,
            enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                    val yOffset = (scrollFraction * maxOffset).roundToInt()
                    IntOffset(0, yOffset)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = isDragging,
                    enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
                    exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(badgeContainerColor)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentDateLabel,
                            color = badgeContentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(width = 28.dp, height = 46.dp)
                        .clip(ScrollerHandleShape)
                        .background(if (isDragging) MaterialTheme.colorScheme.primaryContainer else containerColor)
                        .pointerInput(Unit) {
                            try {
                                detectVerticalDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                                        dragYPx = offset.y + (scrollFraction * maxOffset)
                                    },
                                    onDragEnd = { isDragging = false },
                                    onDragCancel = { isDragging = false },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragYPx = (dragYPx + dragAmount).coerceIn(0f, trackHeightPx)
                                        val fraction = (dragYPx / trackHeightPx).coerceIn(0f, 1f)
                                        val count = currentTotalItemCount
                                        val targetIndex = (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)

                                        coroutineScope.launch {
                                            staggeredGridState.scrollToItem(targetIndex)
                                        }
                                    }
                                )
                            } finally {
                                isDragging = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = null,
                        tint = if (isDragging) MaterialTheme.colorScheme.onPrimaryContainer else contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NierikaFastScroller(
    listState: LazyListState,
    totalItemCount: Int,
    currentDateLabel: String,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badgeContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    badgeContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (totalItemCount <= 0) return

    val coroutineScope = rememberCoroutineScope()
    val motionScheme = MaterialTheme.nierikaMotionScheme
    val density = LocalDensity.current
    val handleHeightPx = remember(density) { with(density) { 44.dp.toPx() } }

    var isDragging by remember { mutableStateOf(false) }
    var trackHeightPx by remember { mutableFloatStateOf(1f) }
    var dragYPx by remember { mutableFloatStateOf(0f) }

    val currentTotalItemCount by rememberUpdatedState(totalItemCount)

    val scrollFraction by remember {
        derivedStateOf {
            if (isDragging) {
                (dragYPx / trackHeightPx.coerceAtLeast(1f)).coerceIn(0f, 1f)
            } else {
                val layoutInfo = listState.layoutInfo
                val totalVisibleItems = layoutInfo.visibleItemsInfo.size
                if (totalVisibleItems == 0 || layoutInfo.totalItemsCount == 0) {
                    0f
                } else {
                    val firstItemIndex = listState.firstVisibleItemIndex.toFloat()
                    (firstItemIndex / (layoutInfo.totalItemsCount - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                trackHeightPx = coordinates.size.height.toFloat()
            }
    ) {
        AnimatedVisibility(
            visible = isVisible || isDragging,
            enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec()),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset {
                    val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                    val yOffset = (scrollFraction * maxOffset).roundToInt()
                    IntOffset(0, yOffset)
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                AnimatedVisibility(
                    visible = isDragging,
                    enter = scaleIn(animationSpec = motionScheme.fastSpatialSpec()) + fadeIn(animationSpec = motionScheme.fastEffectsSpec()),
                    exit = scaleOut(animationSpec = motionScheme.fastSpatialSpec()) + fadeOut(animationSpec = motionScheme.fastEffectsSpec())
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(badgeContainerColor)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentDateLabel,
                            color = badgeContentColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(width = 28.dp, height = 46.dp)
                        .clip(ScrollerHandleShape)
                        .background(if (isDragging) MaterialTheme.colorScheme.primaryContainer else containerColor)
                        .pointerInput(Unit) {
                            try {
                                detectVerticalDragGestures(
                                    onDragStart = { offset ->
                                        isDragging = true
                                        val maxOffset = (trackHeightPx - handleHeightPx).coerceAtLeast(0f)
                                        dragYPx = offset.y + (scrollFraction * maxOffset)
                                    },
                                    onDragEnd = { isDragging = false },
                                    onDragCancel = { isDragging = false },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragYPx = (dragYPx + dragAmount).coerceIn(0f, trackHeightPx)
                                        val fraction = (dragYPx / trackHeightPx).coerceIn(0f, 1f)
                                        val count = currentTotalItemCount
                                        val targetIndex = (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)

                                        coroutineScope.launch {
                                            listState.scrollToItem(targetIndex)
                                        }
                                    }
                                )
                            } finally {
                                isDragging = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = null,
                        tint = if (isDragging) MaterialTheme.colorScheme.onPrimaryContainer else contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

