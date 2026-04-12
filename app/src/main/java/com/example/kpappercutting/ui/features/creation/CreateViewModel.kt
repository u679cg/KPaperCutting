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
    private var customPatterns: List<CustomPattern> = emptyList()
    private var activePattern: EditablePatternState? = null

    var uiState by mutableStateOf(CreateUiState())
        private set

    init {
        CreateSessionMemoryStore.engineSessionState?.let(engine::restoreSessionState)
        CreateSessionMemoryStore.uiSessionState?.let { session ->
            selectedFoldTechnique = session.selectedFoldTechnique
            continuousFoldLayerCount = session.continuousFoldLayerCount
            customPatterns = session.customPatterns
            activePattern = session.activePattern
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
            is CreateUiAction.StartPatternPlacement -> startPatternPlacement(action.source)
            is CreateUiAction.UpdateActivePattern -> activePattern = action.pattern.fitsWithin()
            is CreateUiAction.AddCustomPattern -> customPatterns = customPatterns + action.pattern

            CreateUiAction.EndStroke -> engine.endStroke()
            CreateUiAction.ToggleFold -> engine.toggleFold()
            CreateUiAction.ClearCanvas -> engine.clearCanvasPreservingFoldSelection()
            CreateUiAction.DeleteActivePattern -> activePattern = null
            CreateUiAction.ConfirmActivePattern -> confirmActivePattern()
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
            continuousFoldLayerCount = continuousFoldLayerCount,
            customPatterns = customPatterns,
            activePattern = activePattern
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

    private fun startPatternPlacement(source: PatternSource) {
        val center = engine.patternPlacementCenter()
        val maxSize = engine.patternPlacementMaxSize()
        activePattern = PatternCatalog.createEditablePattern(
            source = source,
            canvasCenter = center,
            maxSize = maxSize,
            previewColor = PatternDefaults.PREVIEW_COLOR
        )
    }

    private fun confirmActivePattern() {
        val pattern = activePattern ?: return
        val path = PatternCatalog.transformedPathFor(pattern) ?: run {
            // TODO: Convert imported PNG custom patterns into cuttable contours before enabling confirm.
            return
        }
        engine.applyPatternCut(path)
        activePattern = null
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
            builtinPatterns = PatternCatalog.builtinPatterns,
            customPatterns = customPatterns,
            activePattern = activePattern,
            availableFoldModes = engine.availableFoldModes,
            isFolded = engine.isFolded,
            canUndo = engine.canUndo,
            canRedo = engine.canRedo,
            canExpand = engine.canExpand,
            renderVersion = engine.renderVersion
        )
    }
}
