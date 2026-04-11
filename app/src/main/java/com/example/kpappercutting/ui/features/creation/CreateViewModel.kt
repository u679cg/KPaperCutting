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
        CreateSessionMemoryStore.sessionState?.let(engine::restoreSessionState)
        syncUiState()
    }

    fun onAction(action: CreateUiAction) {
        when (action) {
            is CreateUiAction.SelectShape -> engine.setShape(action.shape)
            is CreateUiAction.SelectTool -> engine.setTool(action.tool)
            is CreateUiAction.SelectEraserSize -> engine.setEraserSize(action.size)
            is CreateUiAction.SelectPaperColor -> engine.setPaperColor(action.color)
            is CreateUiAction.SelectFoldMode -> engine.selectFoldMode(action.mode)
            is CreateUiAction.StartStroke -> engine.startStroke(action.point)
            is CreateUiAction.AppendStrokePoint -> engine.appendStroke(action.point)
            is CreateUiAction.TransformCanvas -> engine.transform(
                centroid = action.centroid,
                pan = action.pan,
                zoom = action.zoom
            )
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
        persistSession()
        syncUiState()
    }

    override fun onCleared() {
        persistSession()
        super.onCleared()
    }

    private fun syncUiState() {
        uiState = CreateUiState(
            selectedShape = engine.selectedShape,
            selectedTool = engine.selectedTool,
            selectedEraserSize = engine.selectedEraserSize,
            selectedPaperColor = engine.selectedPaperColor,
            foldMode = engine.foldMode,
            availableFoldModes = engine.availableFoldModes,
            isFolded = engine.isFolded,
            canUndo = engine.canUndo,
            canRedo = engine.canRedo,
            canExpand = engine.canExpand,
            renderVersion = engine.renderVersion
        )
    }

    fun persistSession() {
        CreateSessionMemoryStore.sessionState = engine.saveSessionState()
    }
}
