// 该文件用于承载创作页的状态与业务逻辑，负责处理画布编辑、撤销重做和工具切换。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.example.kpappercutting.data.model.PaperShape

class CreateViewModel : ViewModel() {
    var uiState by mutableStateOf(CreateUiState())
        private set

    private val redoStack = mutableListOf<DrawStroke>()

    fun onAction(action: CreateUiAction) {
        when (action) {
            is CreateUiAction.SelectShape -> updateShape(action.shape)
            is CreateUiAction.SelectTool -> updateTool(action.tool)
            is CreateUiAction.SelectFoldMode -> updateFoldMode(action.mode)
            is CreateUiAction.StartStroke -> startStroke(action.point)
            is CreateUiAction.AppendStrokePoint -> appendStrokePoint(action.point)
            CreateUiAction.EndStroke -> endStroke()
            CreateUiAction.ClearCanvas -> clearCanvas()
            CreateUiAction.Undo -> undo()
            CreateUiAction.Redo -> redo()
        }
    }

    private fun updateShape(shape: PaperShape) {
        uiState = uiState.copy(selectedShape = shape)
    }

    private fun updateTool(tool: EditTool) {
        uiState = uiState.copy(selectedTool = tool)
    }

    private fun updateFoldMode(mode: FoldMode) {
        uiState = uiState.copy(selectedFoldMode = mode)
    }

    private fun startStroke(point: Offset) {
        redoStack.clear()
        uiState = uiState.copy(
            currentStroke = DrawStroke(points = listOf(point)),
            canRedo = false
        )
    }

    private fun appendStrokePoint(point: Offset) {
        val stroke = uiState.currentStroke ?: return
        uiState = uiState.copy(
            currentStroke = stroke.copy(points = stroke.points + point)
        )
    }

    private fun endStroke() {
        val stroke = uiState.currentStroke ?: return
        val updatedStrokes = uiState.strokes + stroke
        uiState = uiState.copy(
            strokes = updatedStrokes,
            currentStroke = null,
            canUndo = updatedStrokes.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )
    }

    private fun clearCanvas() {
        redoStack.clear()
        uiState = uiState.copy(
            strokes = emptyList(),
            currentStroke = null,
            canUndo = false,
            canRedo = false
        )
    }

    private fun undo() {
        val lastStroke = uiState.strokes.lastOrNull() ?: return
        redoStack += lastStroke
        val updatedStrokes = uiState.strokes.dropLast(1)
        uiState = uiState.copy(
            strokes = updatedStrokes,
            canUndo = updatedStrokes.isNotEmpty(),
            canRedo = true
        )
    }

    private fun redo() {
        val restoredStroke = redoStack.removeLastOrNull() ?: return
        val updatedStrokes = uiState.strokes + restoredStroke
        uiState = uiState.copy(
            strokes = updatedStrokes,
            canUndo = true,
            canRedo = redoStack.isNotEmpty()
        )
    }
}
