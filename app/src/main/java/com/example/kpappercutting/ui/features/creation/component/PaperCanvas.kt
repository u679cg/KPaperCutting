// 该文件用于承载创作页核心画布组件，后续将负责纸张绘制、手势采集和轨迹渲染。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.kpappercutting.ui.features.creation.CreateUiState
import com.example.kpappercutting.ui.features.creation.CreateUiAction
import com.example.kpappercutting.ui.features.creation.DrawStroke
import com.example.kpappercutting.ui.theme.PaperRed

@Composable
fun PaperCanvas(
    uiState: CreateUiState,
    modifier: Modifier = Modifier,
    onAction: (CreateUiAction) -> Unit
) {
    Canvas(
        modifier = modifier.pointerInput(uiState.selectedTool) {
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
        if (uiState.selectedShape == com.example.kpappercutting.data.model.PaperShape.CIRCLE) {
            drawCircle(color = PaperRed)
        } else {
            drawRect(color = PaperRed)
        }

        (uiState.strokes + listOfNotNull(uiState.currentStroke)).forEach { stroke ->
            drawStroke(stroke)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStroke(stroke: DrawStroke) {
    val firstPoint = stroke.points.firstOrNull() ?: return
    val path = Path().apply {
        moveTo(firstPoint.x, firstPoint.y)
        stroke.points.drop(1).forEach { point ->
            lineTo(point.x, point.y)
        }
    }
    drawPath(
        path = path,
        color = Color(0xFFFDF8F2),
        style = Stroke(width = 8f)
    )
}
