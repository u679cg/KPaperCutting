// 该文件用于承载创作页核心画布组件，负责把剪纸引擎内容渲染到 Compose 画布并采集手势。
package com.example.kpappercutting.ui.features.creation.component

import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.example.kpappercutting.ui.features.creation.CreateUiAction
import com.example.kpappercutting.ui.features.creation.CreateUiState
import com.example.kpappercutting.ui.features.creation.EditTool
import com.example.kpappercutting.ui.features.creation.EditablePatternState
import com.example.kpappercutting.ui.features.creation.PatternCatalog
import com.example.kpappercutting.ui.features.creation.PatternDefaults
import com.example.kpappercutting.ui.features.creation.fitsWithin
import com.example.kpappercutting.ui.features.creation.scaleUniform
import com.example.kpappercutting.ui.features.creation.withCenter
import com.example.kpappercutting.ui.features.creation.withSize
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

@Composable
fun PaperCanvas(
    uiState: CreateUiState,
    engine: PaperCutEngine,
    modifier: Modifier = Modifier,
    onAction: (CreateUiAction) -> Unit
) {
    val renderVersion = uiState.renderVersion
    val selectedShape = uiState.selectedShape
    val selectedTool = uiState.selectedTool
    val activePattern = uiState.activePattern
    var eraserPreviewCenter by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(selectedTool) {
        if (selectedTool != EditTool.ERASER) {
            eraserPreviewCenter = null
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size ->
                engine.attachSize(size.width, size.height)
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activePattern) {
                    if (activePattern == null) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            onAction(CreateUiAction.EndStroke)
                            onAction(
                                CreateUiAction.TransformCanvas(
                                    centroid = centroid,
                                    pan = pan,
                                    zoom = zoom
                                )
                            )
                        }
                    }
                }
                .pointerInput(selectedTool, activePattern) {
                    if (activePattern == null) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var strokeStarted = true
                            if (selectedTool == EditTool.ERASER) {
                                eraserPreviewCenter = down.position
                            }
                            onAction(CreateUiAction.StartStroke(down.position))

                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedCount = event.changes.count { it.pressed }

                                if (pressedCount > 1) {
                                    eraserPreviewCenter = null
                                    if (strokeStarted) {
                                        onAction(CreateUiAction.EndStroke)
                                    }
                                    break
                                }

                                val activeChange = event.changes.firstOrNull { it.id == down.id }
                                    ?: event.changes.firstOrNull()
                                    ?: break

                                if (activeChange.changedToUpIgnoreConsumed()) {
                                    eraserPreviewCenter = null
                                    if (strokeStarted) {
                                        onAction(CreateUiAction.EndStroke)
                                    }
                                    break
                                }

                                if (!activeChange.positionChanged()) {
                                    continue
                                }

                                activeChange.consume()
                                if (selectedTool == EditTool.ERASER) {
                                    eraserPreviewCenter = activeChange.position
                                }
                                onAction(CreateUiAction.AppendStrokePoint(activeChange.position))
                            }
                        }
                    }
                }
        ) {
            renderVersion
            selectedShape
            selectedTool
            drawIntoCanvas { canvas ->
                engine.render(canvas.nativeCanvas)
            }
            if (selectedTool == EditTool.ERASER && activePattern == null) {
                eraserPreviewCenter?.let { center ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = uiState.selectedEraserSize.strokeWidth / 2f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFFB02621).copy(alpha = 0.95f),
                        radius = uiState.selectedEraserSize.strokeWidth / 2f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        if (activePattern != null) {
            PatternEditOverlay(
                pattern = activePattern,
                engine = engine,
                modifier = Modifier.fillMaxSize(),
                onPatternChange = { onAction(CreateUiAction.UpdateActivePattern(it)) },
                onConfirm = { onAction(CreateUiAction.ConfirmActivePattern) },
                onDelete = { onAction(CreateUiAction.DeleteActivePattern) }
            )
        }
    }
}

private enum class PatternHandleType {
    BODY,
    DELETE,
    CONFIRM,
    ROTATE_SCALE,
    LEFT_EDGE,
    RIGHT_EDGE,
    TOP_EDGE,
    BOTTOM_EDGE
}

private data class PatternOverlayLayout(
    val rect: Rect,
    val topLeft: Offset,
    val topRight: Offset,
    val bottomRight: Offset,
    val bottomLeft: Offset,
    val deleteCenter: Offset,
    val confirmCenter: Offset,
    val rotateScaleCenter: Offset,
    val leftCenter: Offset,
    val rightCenter: Offset,
    val topCenter: Offset,
    val bottomCenter: Offset
)

private data class PatternGestureSnapshot(
    val pattern: EditablePatternState,
    val startCenterCanvas: Offset,
    val startLocalPoint: Offset,
    val startDistance: Float,
    val startAngleDegrees: Float
)

@Composable
private fun PatternEditOverlay(
    pattern: EditablePatternState,
    engine: PaperCutEngine,
    modifier: Modifier = Modifier,
    onPatternChange: (EditablePatternState) -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    val currentPattern by rememberUpdatedState(pattern)
    val previewPath = remember(pattern, engine.renderVersion) {
        PatternCatalog.transformedPathFor(pattern)?.let(engine::canvasPathToDisplay)
    }
    val overlayLayout = remember(pattern, engine.renderVersion) {
        patternOverlayLayout(pattern, engine)
    }
    val overlayPaint = remember(pattern.previewColor) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pattern.previewColor
            style = Paint.Style.FILL
            alpha = 110
        }
    }

    Canvas(
        modifier = modifier.pointerInput(pattern.source.id, engine.renderVersion) {
            awaitEachGesture {
                val patternState = currentPattern
                val down = awaitFirstDown(requireUnconsumed = false)
                val layout = patternOverlayLayout(patternState, engine)
                val handle = detectPatternHandle(
                    screenPoint = down.position,
                    pattern = patternState,
                    layout = layout,
                    engine = engine
                ) ?: return@awaitEachGesture

                val startCanvasPoint = engine.displayPointToCanvas(down.position)
                val snapshot = PatternGestureSnapshot(
                    pattern = patternState,
                    startCenterCanvas = patternState.center,
                    startLocalPoint = toPatternLocal(startCanvasPoint, patternState),
                    startDistance = distanceBetween(patternState.center, startCanvasPoint),
                    startAngleDegrees = angleDegrees(patternState.center, startCanvasPoint)
                )

                down.consume()
                var moved = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id }
                        ?: event.changes.firstOrNull()
                        ?: break

                    if (change.changedToUpIgnoreConsumed()) {
                        if (!moved) {
                            when (handle) {
                                PatternHandleType.DELETE -> onDelete()
                                PatternHandleType.CONFIRM -> onConfirm()
                                else -> Unit
                            }
                        }
                        change.consume()
                        break
                    }

                    if (!change.positionChanged()) {
                        continue
                    }

                    val currentCanvas = engine.displayPointToCanvas(change.position)
                    val updatedPattern = when (handle) {
                        PatternHandleType.BODY -> {
                            snapshot.pattern.withCenter(
                                snapshot.startCenterCanvas + (currentCanvas - startCanvasPoint)
                            )
                        }

                        PatternHandleType.LEFT_EDGE,
                        PatternHandleType.RIGHT_EDGE -> {
                            val local = toPatternLocal(currentCanvas, snapshot.pattern)
                            snapshot.pattern.withSize(
                                width = max(abs(local.x) * 2f, PatternDefaults.MIN_PATTERN_SIZE),
                                height = snapshot.pattern.height
                            )
                        }

                        PatternHandleType.TOP_EDGE,
                        PatternHandleType.BOTTOM_EDGE -> {
                            val local = toPatternLocal(currentCanvas, snapshot.pattern)
                            snapshot.pattern.withSize(
                                width = snapshot.pattern.width,
                                height = max(abs(local.y) * 2f, PatternDefaults.MIN_PATTERN_SIZE)
                            )
                        }

                        PatternHandleType.ROTATE_SCALE -> {
                            val currentDistance = distanceBetween(snapshot.startCenterCanvas, currentCanvas)
                            val currentAngle = angleDegrees(snapshot.startCenterCanvas, currentCanvas)
                            val scaleFactor = max(currentDistance / snapshot.startDistance, 0.1f)
                            snapshot.pattern
                                .scaleUniform(scaleFactor)
                                .copy(rotationDegrees = snapshot.pattern.rotationDegrees + (currentAngle - snapshot.startAngleDegrees))
                        }

                        PatternHandleType.DELETE,
                        PatternHandleType.CONFIRM -> snapshot.pattern
                    }.fitsWithin()

                    moved = moved || distanceBetween(change.position, down.position) > 6f
                    onPatternChange(updatedPattern)
                    change.consume()
                }
            }
        }
    ) {
        previewPath?.let { path ->
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPath(path, overlayPaint)
            }
        }

        val layout = overlayLayout
        val strokeWidth = 2.dp.toPx()
        drawLine(Color.White.copy(alpha = 0.95f), layout.topLeft, layout.topRight, strokeWidth)
        drawLine(Color.White.copy(alpha = 0.95f), layout.topRight, layout.bottomRight, strokeWidth)
        drawLine(Color.White.copy(alpha = 0.95f), layout.bottomRight, layout.bottomLeft, strokeWidth)
        drawLine(Color.White.copy(alpha = 0.95f), layout.bottomLeft, layout.topLeft, strokeWidth)

        drawHandle(layout.leftCenter, Color.White, 10.dp.toPx())
        drawHandle(layout.rightCenter, Color.White, 10.dp.toPx())
        drawHandle(layout.topCenter, Color.White, 10.dp.toPx())
        drawHandle(layout.bottomCenter, Color.White, 10.dp.toPx())
        drawHandle(layout.rotateScaleCenter, Color(0xFFFFE082), 12.dp.toPx())
        drawHandle(layout.deleteCenter, Color(0xFFFF8A80), 14.dp.toPx())
        drawHandle(layout.confirmCenter, Color(0xFF8BC34A), 14.dp.toPx())

        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(layout.deleteCenter.x - 8.dp.toPx(), layout.deleteCenter.y - 8.dp.toPx()),
            end = Offset(layout.deleteCenter.x + 8.dp.toPx(), layout.deleteCenter.y + 8.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(layout.deleteCenter.x + 8.dp.toPx(), layout.deleteCenter.y - 8.dp.toPx()),
            end = Offset(layout.deleteCenter.x - 8.dp.toPx(), layout.deleteCenter.y + 8.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.95f),
            start = Offset(layout.confirmCenter.x - 6.dp.toPx(), layout.confirmCenter.y),
            end = Offset(layout.confirmCenter.x - 1.dp.toPx(), layout.confirmCenter.y + 6.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.95f),
            start = Offset(layout.confirmCenter.x - 1.dp.toPx(), layout.confirmCenter.y + 6.dp.toPx()),
            end = Offset(layout.confirmCenter.x + 8.dp.toPx(), layout.confirmCenter.y - 6.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandle(
    center: Offset,
    color: Color,
    radius: Float
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color(0xFF3A352E),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

private fun patternOverlayLayout(
    pattern: EditablePatternState,
    engine: PaperCutEngine
): PatternOverlayLayout {
    val halfWidth = pattern.width / 2f
    val halfHeight = pattern.height / 2f
    val topLeft = engine.canvasPointToDisplay(
        rotateLocalPoint(pattern, Offset(-halfWidth, -halfHeight))
    )
    val topRight = engine.canvasPointToDisplay(
        rotateLocalPoint(pattern, Offset(halfWidth, -halfHeight))
    )
    val bottomRight = engine.canvasPointToDisplay(
        rotateLocalPoint(pattern, Offset(halfWidth, halfHeight))
    )
    val bottomLeft = engine.canvasPointToDisplay(
        rotateLocalPoint(pattern, Offset(-halfWidth, halfHeight))
    )
    val leftCenter = midpoint(topLeft, bottomLeft)
    val rightCenter = midpoint(topRight, bottomRight)
    val topCenter = midpoint(topLeft, topRight)
    val bottomCenter = midpoint(bottomLeft, bottomRight)

    val minX = minOf(topLeft.x, topRight.x, bottomRight.x, bottomLeft.x)
    val maxX = maxOf(topLeft.x, topRight.x, bottomRight.x, bottomLeft.x)
    val minY = minOf(topLeft.y, topRight.y, bottomRight.y, bottomLeft.y)
    val maxY = maxOf(topLeft.y, topRight.y, bottomRight.y, bottomLeft.y)

    return PatternOverlayLayout(
        rect = Rect(
            left = minX,
            top = minY,
            right = maxX,
            bottom = maxY
        ),
        topLeft = topLeft,
        topRight = topRight,
        bottomRight = bottomRight,
        bottomLeft = bottomLeft,
        deleteCenter = topLeft,
        confirmCenter = topRight,
        rotateScaleCenter = bottomRight,
        leftCenter = leftCenter,
        rightCenter = rightCenter,
        topCenter = topCenter,
        bottomCenter = bottomCenter
    )
}

private fun detectPatternHandle(
    screenPoint: Offset,
    pattern: EditablePatternState,
    layout: PatternOverlayLayout,
    engine: PaperCutEngine
): PatternHandleType? {
    val hitRadius = 24f
    return when {
        distanceBetween(screenPoint, layout.deleteCenter) <= hitRadius -> PatternHandleType.DELETE
        distanceBetween(screenPoint, layout.confirmCenter) <= hitRadius -> PatternHandleType.CONFIRM
        distanceBetween(screenPoint, layout.rotateScaleCenter) <= hitRadius -> PatternHandleType.ROTATE_SCALE
        distanceBetween(screenPoint, layout.leftCenter) <= hitRadius -> PatternHandleType.LEFT_EDGE
        distanceBetween(screenPoint, layout.rightCenter) <= hitRadius -> PatternHandleType.RIGHT_EDGE
        distanceBetween(screenPoint, layout.topCenter) <= hitRadius -> PatternHandleType.TOP_EDGE
        distanceBetween(screenPoint, layout.bottomCenter) <= hitRadius -> PatternHandleType.BOTTOM_EDGE
        isPointInPatternBody(engine.displayPointToCanvas(screenPoint), pattern) -> PatternHandleType.BODY
        else -> null
    }
}

private fun rotateLocalPoint(pattern: EditablePatternState, local: Offset): Offset {
    val radians = Math.toRadians(pattern.rotationDegrees.toDouble())
    val rotatedX = local.x * kotlin.math.cos(radians) - local.y * kotlin.math.sin(radians)
    val rotatedY = local.x * kotlin.math.sin(radians) + local.y * kotlin.math.cos(radians)
    return Offset(
        x = pattern.center.x + rotatedX.toFloat(),
        y = pattern.center.y + rotatedY.toFloat()
    )
}

private fun toPatternLocal(point: Offset, pattern: EditablePatternState): Offset {
    val dx = point.x - pattern.center.x
    val dy = point.y - pattern.center.y
    val radians = Math.toRadians((-pattern.rotationDegrees).toDouble())
    val localX = dx * kotlin.math.cos(radians) - dy * kotlin.math.sin(radians)
    val localY = dx * kotlin.math.sin(radians) + dy * kotlin.math.cos(radians)
    return Offset(localX.toFloat(), localY.toFloat())
}

private fun isPointInPatternBody(point: Offset, pattern: EditablePatternState): Boolean {
    val local = toPatternLocal(point, pattern)
    return abs(local.x) <= pattern.width / 2f && abs(local.y) <= pattern.height / 2f
}

private fun midpoint(a: Offset, b: Offset): Offset {
    return Offset(
        x = (a.x + b.x) / 2f,
        y = (a.y + b.y) / 2f
    )
}

private fun distanceBetween(a: Offset, b: Offset): Float {
    return hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()
}

private fun angleDegrees(center: Offset, point: Offset): Float {
    return (atan2(point.y - center.y, point.x - center.x) * 180f / PI).toFloat()
}
