package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.kpappercutting.ui.features.creation.FoldCatalog
import com.example.kpappercutting.ui.features.creation.FoldMode
import com.example.kpappercutting.ui.features.creation.spec
import com.example.kpappercutting.ui.theme.PaperRed
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FoldTechniqueRadialLauncher(
    currentMode: FoldMode,
    availableModes: List<FoldMode>,
    compact: Boolean,
    onTap: () -> Unit,
    onModeSelected: (FoldMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectableModes = remember(availableModes) {
        FoldCatalog.specsForSelection().map { it.mode }.filter { it in availableModes }
    }
    val density = LocalDensity.current

    var expanded by rememberSaveable { mutableStateOf(false) }
    var highlightedMode by rememberSaveable { mutableStateOf<FoldMode?>(null) }
    var trackingGesture by rememberSaveable { mutableStateOf(false) }
    var anchorInWindow by remember { mutableStateOf(Offset.Unspecified) }
    var buttonBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
    val popupProperties = remember {
        PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = false
        )
    }

    val menuRadius = if (compact) 112.dp else 126.dp
    val minHitRadius = if (compact) 48.dp else 56.dp
    val maxHitRadius = if (compact) 156.dp else 172.dp
    val itemSize = if (compact) 62.dp else 68.dp
    val menuRadiusPx = with(density) { menuRadius.toPx() }
    val minHitRadiusPx = with(density) { minHitRadius.toPx() }
    val maxHitRadiusPx = with(density) { maxHitRadius.toPx() }
    val itemSizePx = with(density) { itemSize.toPx() }

    val buttonCorner by animateDpAsState(
        targetValue = if (expanded) 32.dp else 28.dp,
        animationSpec = spring(stiffness = 380f),
        label = "fold_button_corner"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (expanded) 0.94f else 1f,
        animationSpec = spring(stiffness = 420f, dampingRatio = 0.72f),
        label = "fold_button_scale"
    )
    val buttonElevation by animateDpAsState(
        targetValue = if (expanded) 12.dp else 4.dp,
        animationSpec = spring(stiffness = 420f),
        label = "fold_button_elevation"
    )

    fun dismissWheel() {
        expanded = false
        trackingGesture = false
        highlightedMode = null
    }

    fun pointerInWindow(localPointer: Offset): Offset {
        return Offset(
            x = buttonBoundsInWindow.left + localPointer.x,
            y = buttonBoundsInWindow.top + localPointer.y
        )
    }

    fun selectMode(mode: FoldMode) {
        onModeSelected(mode)
        dismissWheel()
    }

    LaunchedEffect(selectableModes) {
        if (highlightedMode !in selectableModes) {
            highlightedMode = null
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .width(if (compact) 188.dp else 220.dp)
                .height(if (compact) 52.dp else 56.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .shadow(buttonElevation, RoundedCornerShape(buttonCorner), clip = false)
                .clip(RoundedCornerShape(buttonCorner))
                .onGloballyPositioned { coordinates ->
                    val bounds = coordinates.boundsInWindow()
                    buttonBoundsInWindow = bounds
                    anchorInWindow = Offset(
                        x = bounds.left + bounds.width / 2f,
                        y = bounds.top + bounds.height / 2f
                    )
                }
                .pointerInput(selectableModes, compact, buttonBoundsInWindow, anchorInWindow) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var releasedBeforeLongPress = false
                        var cancelledBeforeLongPress = false

                        val longPressReached = withTimeoutOrNull(
                            viewConfiguration.longPressTimeoutMillis.toLong()
                        ) {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val primary = event.changes.firstOrNull { it.id == down.id }
                                    ?: run {
                                        cancelledBeforeLongPress = true
                                        return@withTimeoutOrNull false
                                    }

                                if (primary.changedToUpIgnoreConsumed() || !primary.pressed) {
                                    releasedBeforeLongPress = true
                                    return@withTimeoutOrNull false
                                }

                                if (event.changes.count { it.pressed } > 1) {
                                    cancelledBeforeLongPress = true
                                    return@withTimeoutOrNull false
                                }

                                if (primary.position.isOutsideButtonBounds(size)) {
                                    cancelledBeforeLongPress = true
                                    return@withTimeoutOrNull false
                                }
                            }
                        } == null

                        if (!longPressReached) {
                            dismissWheel()
                            if (releasedBeforeLongPress && !cancelledBeforeLongPress) {
                                onTap()
                            }
                            return@awaitEachGesture
                        }

                        expanded = true
                        trackingGesture = true
                        highlightedMode = resolveFoldModeHit(
                            pointer = pointerInWindow(down.position),
                            anchor = anchorInWindow,
                            specs = selectableModes,
                            minRadius = minHitRadiusPx,
                            maxRadius = maxHitRadiusPx,
                            previous = null
                        )

                        while (trackingGesture) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val primary = event.changes.firstOrNull { it.id == down.id }

                            if (primary == null) {
                                dismissWheel()
                                break
                            }

                            highlightedMode = resolveFoldModeHit(
                                pointer = pointerInWindow(primary.position),
                                anchor = anchorInWindow,
                                specs = selectableModes,
                                minRadius = minHitRadiusPx,
                                maxRadius = maxHitRadiusPx,
                                previous = highlightedMode
                            )

                            if (event.changes.count { it.pressed } > 1) {
                                dismissWheel()
                                break
                            }

                            if (primary.changedToUpIgnoreConsumed() || !primary.pressed) {
                                highlightedMode?.let(::selectMode) ?: dismissWheel()
                                break
                            }

                            if (primary.position.isOutsideButtonBounds(size)) {
                                dismissWheel()
                                break
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(buttonCorner),
            color = if (expanded) ColorPalette.expandedButton else ColorPalette.baseButton
        ) {
            Box(contentAlignment = Alignment.Center) {
                val currentLabel = when (currentMode) {
                    FoldMode.NONE -> "选择剪纸技法"
                    else -> currentMode.spec.displayName
                }
                val helperLabel = if (expanded) "滑动选择，松手确认" else "长按展开轮盘"
                Text(
                    text = "$currentLabel\n$helperLabel",
                    color = if (expanded) PaperRed else ColorPalette.baseText,
                    fontSize = if (compact) 14.sp else 15.sp
                )
            }
        }

        if (expanded && anchorInWindow.isSpecified) {
            Popup(
                onDismissRequest = ::dismissWheel,
                alignment = Alignment.TopStart,
                offset = IntOffset.Zero,
                properties = popupProperties
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + scaleIn(initialScale = 0.86f),
                    exit = fadeOut() + scaleOut(targetScale = 0.86f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        FoldTechniqueRadialMenu(
                            anchor = anchorInWindow,
                            specs = selectableModes,
                            highlightedMode = highlightedMode,
                            currentMode = currentMode,
                            menuRadiusPx = menuRadiusPx,
                            itemDiameterPx = itemSizePx,
                            itemSize = itemSize,
                            onItemClick = ::selectMode
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.FoldTechniqueRadialMenu(
    anchor: Offset,
    specs: List<FoldMode>,
    highlightedMode: FoldMode?,
    currentMode: FoldMode,
    menuRadiusPx: Float,
    itemDiameterPx: Float,
    itemSize: Dp,
    onItemClick: (FoldMode) -> Unit
) {
    specs.forEachIndexed { index, mode ->
        val angle = itemAngleDegrees(index, specs.size)
        val radians = Math.toRadians(angle.toDouble())
        val itemCenter = Offset(
            x = anchor.x + (cos(radians) * menuRadiusPx).toFloat(),
            y = anchor.y + (sin(radians) * menuRadiusPx).toFloat()
        )
        val isHighlighted = mode == highlightedMode
        val isCurrent = mode == currentMode

        val scale by animateFloatAsState(
            targetValue = when {
                isHighlighted -> 1.15f
                isCurrent -> 1.04f
                else -> 0.94f
            },
            animationSpec = spring(stiffness = 420f, dampingRatio = 0.75f),
            label = "fold_item_scale_$index"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isHighlighted) 1f else 0.84f,
            animationSpec = spring(stiffness = 380f),
            label = "fold_item_alpha_$index"
        )
        val elevation by animateDpAsState(
            targetValue = if (isHighlighted) 14.dp else 8.dp,
            animationSpec = spring(stiffness = 380f),
            label = "fold_item_elevation_$index"
        )

        Surface(
            modifier = Modifier
                .size(itemSize)
                .graphicsLayer {
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
                .offset {
                    IntOffset(
                        x = (itemCenter.x - itemDiameterPx / 2f).roundToInt(),
                        y = (itemCenter.y - itemDiameterPx / 2f).roundToInt()
                    )
                }
                .shadow(elevation, CircleShape, clip = false),
            shape = CircleShape,
            color = when {
                isHighlighted -> PaperRed
                isCurrent -> ColorPalette.currentItem
                else -> ColorPalette.baseItem
            },
            onClick = { onItemClick(mode) }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.spec.displayName,
                    color = if (isHighlighted) Color.White else ColorPalette.baseText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private object ColorPalette {
    val baseButton = Color.White
    val expandedButton = Color(0xFFFFF4EA)
    val baseItem = Color.White.copy(alpha = 0.96f)
    val currentItem = Color(0xFFFCF0E1)
    val baseText = Color(0xFF6C6C6C)
}

private fun itemAngleDegrees(index: Int, total: Int): Float {
    val step = 360f / total.coerceAtLeast(1)
    return -90f + (index * step)
}

private fun resolveFoldModeHit(
    pointer: Offset,
    anchor: Offset,
    specs: List<FoldMode>,
    minRadius: Float,
    maxRadius: Float,
    previous: FoldMode?
): FoldMode? {
    if (!anchor.isSpecified || specs.isEmpty()) return null

    val dx = pointer.x - anchor.x
    val dy = pointer.y - anchor.y
    val distance = hypot(dx, dy)
    if (distance < minRadius || distance > maxRadius) {
        return null
    }

    val clockwiseAngle = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 90.0) + 360.0) % 360.0
    val sector = 360f / specs.size
    val candidateIndex = (((clockwiseAngle + sector / 2f) / sector).toInt()) % specs.size
    val candidateMode = specs[candidateIndex]

    previous ?: return candidateMode

    val previousIndex = specs.indexOf(previous)
    if (previousIndex == -1) return candidateMode

    val previousCenter = previousIndex * sector
    val previousDistance = angularDistance(clockwiseAngle.toFloat(), previousCenter)
    val hysteresis = sector * 0.12f
    return if (candidateMode != previous && previousDistance <= sector / 2f + hysteresis) {
        previous
    } else {
        candidateMode
    }
}

private fun angularDistance(angle: Float, center: Float): Float {
    val diff = ((angle - center + 540f) % 360f) - 180f
    return abs(diff)
}

private fun Offset.isOutsideButtonBounds(size: IntSize): Boolean {
    return x < 0f || y < 0f || x > size.width.toFloat() || y > size.height.toFloat()
}
