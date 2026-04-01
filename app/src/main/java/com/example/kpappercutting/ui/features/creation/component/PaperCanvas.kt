// 该文件用于承载创作页核心画布组件，负责把剪纸引擎内容渲染到 Compose 画布并采集手势。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
                detectDragGestures(
                    onDragStart = { offset ->
                        onAction(CreateUiAction.StartStroke(offset))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onAction(CreateUiAction.AppendStrokePoint(change.position))
                    },
                    onDragEnd = {
                        onAction(CreateUiAction.EndStroke)
                    },
                    onDragCancel = {
                        onAction(CreateUiAction.EndStroke)
                    }
                )
            }
    ) {
        renderVersion
        selectedShape
        selectedTool
        engine.attachSize(size.width.toInt(), size.height.toInt())
        drawIntoCanvas { canvas ->
            engine.render(canvas.nativeCanvas)
        }
    }
}
