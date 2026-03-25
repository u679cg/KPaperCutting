// 该文件用于承载创作页的状态与业务逻辑，负责处理画布编辑、撤销重做和工具切换。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

class CreateViewModel : ViewModel() {
    val engine = PaperCutEngine()

    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        syncUiState()
    }

    fun onAction(action: CreateUiAction) {
        when (action) {
            is CreateUiAction.SelectShape -> engine.setShape(action.shape)
            is CreateUiAction.SelectTool -> engine.setTool(action.tool)
            is CreateUiAction.SelectFoldMode -> engine.selectFoldMode(action.mode)
            is CreateUiAction.StartStroke -> engine.startStroke(action.point)
            is CreateUiAction.AppendStrokePoint -> engine.appendStroke(action.point)
            CreateUiAction.EndStroke -> engine.endStroke()
            CreateUiAction.ToggleFold -> engine.toggleFold()
            CreateUiAction.ClearCanvas -> engine.clearCanvas()
            CreateUiAction.Undo -> engine.undo()
            CreateUiAction.Redo -> engine.redo()
        }
        syncUiState()
    }

    fun resetCanvas() {
        engine.resetAll()
        syncUiState()
    }

    private fun syncUiState() {
        uiState = CreateUiState(
            selectedShape = engine.selectedShape,
            selectedTool = engine.selectedTool,
            foldMode = engine.foldMode,
            isFolded = engine.isFolded,
            canUndo = engine.canUndo,
            canRedo = engine.canRedo,
            canExpand = engine.canExpand,
            canSelectFiveFold = engine.canSelectFiveFold,
            canSelectEightFold = engine.canSelectEightFold,
            renderVersion = engine.renderVersion
        )
    }
}
