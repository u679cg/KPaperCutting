// 该文件用于承载创作页的状态与业务逻辑，负责处理画布编辑、撤销重做和工具切换。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

class CreateViewModel : ViewModel() {
    val engine = PaperCutEngine()

    private var selectedFoldTechnique: FoldTechniqueOption =
        FoldTechniqueOption.fromFoldMode(engine.foldMode)
    private var continuousFoldLayerCount: Int = ContinuousFoldLayerOptions.first()

    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        CreateSessionMemoryStore.engineSessionState?.let(engine::restoreSessionState)
        CreateSessionMemoryStore.uiSessionState?.let { session ->
            selectedFoldTechnique = session.selectedFoldTechnique
            continuousFoldLayerCount = session.continuousFoldLayerCount
        } ?: run {
            selectedFoldTechnique = FoldTechniqueOption.fromFoldMode(engine.foldMode)
        }
        syncUiState()
    }

    fun onAction(action: CreateUiAction) {
        when (action) {
            is CreateUiAction.SelectShape -> engine.setShape(action.shape)
            is CreateUiAction.SelectCreationMode -> engine.setShape(action.mode.defaultShape)
            is CreateUiAction.SelectTool -> engine.setTool(action.tool)
            is CreateUiAction.SelectEraserSize -> engine.setEraserSize(action.size)
            is CreateUiAction.SelectPaperColor -> engine.setPaperColor(action.color)
            is CreateUiAction.SelectFoldMode -> {
                selectedFoldTechnique = FoldTechniqueOption.fromFoldMode(action.mode)
                engine.selectFoldMode(action.mode)
            }
            is CreateUiAction.SelectFoldTechnique -> selectFoldTechnique(action.technique)
            is CreateUiAction.SetContinuousFoldLayerCount -> updateContinuousFoldLayerCount(action.layerCount)
            is CreateUiAction.StartStroke -> engine.startStroke(action.point)
            is CreateUiAction.AppendStrokePoint -> engine.appendStroke(action.point)
            is CreateUiAction.TransformCanvas -> engine.transform(
                centroid = action.centroid,
                pan = action.pan,
                zoom = action.zoom
            )

            CreateUiAction.EndStroke -> engine.endStroke()
            CreateUiAction.ToggleFold -> engine.toggleFold()
            CreateUiAction.ClearCanvas -> engine.clearCanvasPreservingFoldSelection()
            CreateUiAction.Undo -> engine.undo()
            CreateUiAction.Redo -> engine.redo()
        }
        syncUiState()
    }

    fun resetCanvas() {
        engine.resetAll()
        if (selectedFoldTechnique == FoldTechniqueOption.CONTINUOUS) {
            engine.selectFoldMode(FoldTechniqueOption.CONTINUOUS.effectiveFoldMode(continuousFoldLayerCount))
        } else {
            selectedFoldTechnique = FoldTechniqueOption.fromFoldMode(engine.foldMode)
        }
        persistSession()
        syncUiState()
    }

    override fun onCleared() {
        persistSession()
        super.onCleared()
    }

    fun persistSession() {
        CreateSessionMemoryStore.engineSessionState = engine.saveSessionState()
        CreateSessionMemoryStore.uiSessionState = CreateUiSessionState(
            selectedFoldTechnique = selectedFoldTechnique,
            continuousFoldLayerCount = continuousFoldLayerCount
        )
    }

    private fun selectFoldTechnique(technique: FoldTechniqueOption) {
        selectedFoldTechnique = technique
        engine.selectFoldMode(technique.effectiveFoldMode(continuousFoldLayerCount))
    }

    private fun updateContinuousFoldLayerCount(layerCount: Int) {
        continuousFoldLayerCount = layerCount
        if (selectedFoldTechnique == FoldTechniqueOption.CONTINUOUS) {
            // TODO: When PaperCutEngine supports real continuous binary folding geometry,
            // replace this temporary TWO_PART fallback with the selected layer count.
            engine.selectFoldMode(FoldTechniqueOption.CONTINUOUS.effectiveFoldMode(layerCount))
        }
    }

    private fun syncUiState() {
        uiState = CreateUiState(
            creationMode = CreationMode.fromShape(engine.selectedShape),
            selectedShape = engine.selectedShape,
            selectedTool = engine.selectedTool,
            selectedEraserSize = engine.selectedEraserSize,
            selectedPaperColor = engine.selectedPaperColor,
            foldMode = engine.foldMode,
            selectedFoldTechnique = selectedFoldTechnique,
            continuousFoldLayerCount = continuousFoldLayerCount,
            availableFoldModes = engine.availableFoldModes,
            isFolded = engine.isFolded,
            canUndo = engine.canUndo,
            canRedo = engine.canRedo,
            canExpand = engine.canExpand,
            renderVersion = engine.renderVersion
        )
    }
}
