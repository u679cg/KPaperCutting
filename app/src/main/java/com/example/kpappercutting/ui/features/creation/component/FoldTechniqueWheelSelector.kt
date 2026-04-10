package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.features.creation.FoldCatalog
import com.example.kpappercutting.ui.features.creation.FoldMode
import com.example.kpappercutting.ui.features.creation.spec
import com.example.kpappercutting.ui.theme.PaperRed
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoldTechniqueWheelSelector(
    currentMode: FoldMode,
    availableModes: List<FoldMode>,
    compact: Boolean,
    onModeSelected: (FoldMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectableSpecs = remember(availableModes) {
        FoldCatalog.specsForSelection().filter { it.mode in availableModes }
    }
    val cycleSize = selectableSpecs.size.coerceAtLeast(1)
    val virtualCount = Int.MAX_VALUE
    val middleAnchor = remember(cycleSize) {
        val midpoint = virtualCount / 2
        midpoint - midpoint.floorMod(cycleSize)
    }
    val selectedIndex = remember(currentMode, selectableSpecs) {
        selectableSpecs.indexOfFirst { it.mode == currentMode }.coerceAtLeast(0)
    }
    val initialIndex = remember(selectedIndex, middleAnchor) {
        middleAnchor + selectedIndex
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier
    ) {
        val itemWidth = if (compact) 70.dp else 78.dp
        val edgePadding = ((maxWidth - itemWidth) / 2).coerceAtLeast(0.dp)

        LaunchedEffect(selectedIndex, cycleSize) {
            if (selectableSpecs.isEmpty()) return@LaunchedEffect
            val centeredRawIndex = listState.findCenteredItemIndex()
            val targetIndex = nearestVirtualIndex(
                currentIndex = centeredRawIndex,
                cycleSize = cycleSize,
                targetModulo = selectedIndex
            )
            if (centeredRawIndex != targetIndex && !listState.isScrollInProgress) {
                listState.animateScrollToItem(targetIndex)
            }
        }

        LaunchedEffect(listState, selectableSpecs, currentMode, cycleSize) {
            if (selectableSpecs.isEmpty()) return@LaunchedEffect
            snapshotFlow { listState.isScrollInProgress to listState.findCenteredItemIndex() }
                .map { (scrolling, rawIndex) ->
                    if (!scrolling) rawIndex.floorMod(cycleSize) else -1
                }
                .filter { it in 0 until cycleSize }
                .distinctUntilChanged()
                .collect { realIndex ->
                    val mode = selectableSpecs[realIndex].mode
                    if (mode != currentMode) {
                        onModeSelected(mode)
                    }
                }
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(4.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp), clip = false),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                    .drawWithCache {
                        val sideFade = size.width * 0.18f
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to Color.White,
                                    (sideFade / size.width).coerceIn(0f, 0.3f) to Color.White.copy(alpha = 0f),
                                    1f - (sideFade / size.width).coerceIn(0f, 0.3f) to Color.White.copy(alpha = 0f),
                                    1f to Color.White
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                LazyRow(
                    state = listState,
                    flingBehavior = flingBehavior,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = edgePadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(
                        count = virtualCount,
                        key = { it }
                    ) { virtualIndex ->
                        val realIndex = virtualIndex.floorMod(cycleSize)
                        val spec = selectableSpecs[realIndex]
                        val emphasis = listState.itemEmphasis(virtualIndex)
                        val scale by animateFloatAsState(
                            targetValue = lerp(0.84f, 1.14f, emphasis),
                            animationSpec = spring(stiffness = 420f, dampingRatio = 0.86f),
                            label = "fold_scale_$virtualIndex"
                        )
                        val alpha by animateFloatAsState(
                            targetValue = lerp(0.34f, 1f, emphasis),
                            animationSpec = spring(stiffness = 420f, dampingRatio = 0.9f),
                            label = "fold_alpha_$virtualIndex"
                        )

                        Box(
                            modifier = Modifier
                                .width(itemWidth)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                }
                                .clickable {
                                    val targetIndex = nearestVirtualIndex(
                                        currentIndex = listState.findCenteredItemIndex(),
                                        cycleSize = cycleSize,
                                        targetModulo = realIndex
                                    )
                                    if (targetIndex != listState.findCenteredItemIndex()) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(targetIndex)
                                        }
                                    } else {
                                        onModeSelected(spec.mode)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = spec.displayName,
                                color = if (emphasis > 0.72f) PaperRed else Color(0xFF8C8378),
                                fontSize = lerp(13f, 18f, emphasis).sp,
                                fontWeight = if (emphasis > 0.72f) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.findCenteredItemIndex(): Int {
    val layoutInfo = layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    if (visibleItems.isEmpty()) return firstVisibleItemIndex

    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    return visibleItems.minByOrNull { item ->
        abs((item.offset + item.size / 2f) - viewportCenter)
    }?.index ?: firstVisibleItemIndex
}

private fun androidx.compose.foundation.lazy.LazyListState.itemEmphasis(index: Int): Float {
    val layoutInfo = layoutInfo
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return 0.24f
    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
    val itemCenter = item.offset + item.size / 2f
    val distance = abs(itemCenter - viewportCenter)
    val maxDistance = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2f
    return (1f - (distance / maxDistance)).coerceIn(0f, 1f)
}

private fun nearestVirtualIndex(
    currentIndex: Int,
    cycleSize: Int,
    targetModulo: Int
): Int {
    val currentBase = currentIndex - currentIndex.floorMod(cycleSize)
    val candidates = listOf(
        currentBase + targetModulo,
        currentBase + targetModulo + cycleSize,
        currentBase + targetModulo - cycleSize
    )
    return candidates.minByOrNull { abs(it - currentIndex) } ?: (currentBase + targetModulo)
}

private fun Int.floorMod(other: Int): Int {
    return ((this % other) + other) % other
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
