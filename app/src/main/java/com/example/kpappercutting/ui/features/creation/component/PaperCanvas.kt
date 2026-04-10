// 该文件用于承载创作页核心画布组件，负责把剪纸引擎内容渲染到 Compose 画布并采集手势。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.example.kpappercutting.ui.features.creation.CreateUiAction
import com.example.kpappercutting.ui.features.creation.CreateUiState
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

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
    var eraserPreviewCenter by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(selectedTool) {
        if (selectedTool != com.example.kpappercutting.ui.features.creation.EditTool.ERASER) {
            eraserPreviewCenter = null
        }
    }

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                engine.attachSize(size.width, size.height)
            }
            .pointerInput(Unit) {
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
            .pointerInput(uiState.selectedTool) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var strokeStarted = true
                    if (selectedTool == com.example.kpappercutting.ui.features.creation.EditTool.ERASER) {
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
                        if (selectedTool == com.example.kpappercutting.ui.features.creation.EditTool.ERASER) {
                            eraserPreviewCenter = activeChange.position
                        }
                        onAction(CreateUiAction.AppendStrokePoint(activeChange.position))
                    }
                }
            }
    ) {
        renderVersion
        selectedShape
        selectedTool
        engine.attachSize(size.width.toInt(), size.height.toInt())
        drawIntoCanvas { canvas ->
            engine.render(canvas.nativeCanvas)
        }
        if (selectedTool == com.example.kpappercutting.ui.features.creation.EditTool.ERASER) {
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
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }
    }
}
