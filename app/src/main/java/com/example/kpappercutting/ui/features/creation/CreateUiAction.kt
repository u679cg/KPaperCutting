// 该文件用于定义创作页的用户操作类型，作为 Screen 与 ViewModel 之间的统一事件协议。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.ui.geometry.Offset
import com.example.kpappercutting.data.model.PaperShape

sealed interface CreateUiAction {
    data class SelectShape(val shape: PaperShape) : CreateUiAction
    data class SelectCreationMode(val mode: CreationMode) : CreateUiAction
    data class SelectTool(val tool: EditTool) : CreateUiAction
    data class SelectEraserSize(val size: EraserSize) : CreateUiAction
    data class SelectPaperColor(val color: Int) : CreateUiAction
    data class SelectFoldMode(val mode: FoldMode) : CreateUiAction
    data class SelectFoldTechnique(val technique: FoldTechniqueOption) : CreateUiAction
    data class SetContinuousFoldLayerCount(val layerCount: Int) : CreateUiAction
    data class StartPatternPlacement(val source: PatternSource) : CreateUiAction
    data class UpdateActivePattern(val pattern: EditablePatternState) : CreateUiAction
    data class StartStroke(val point: Offset) : CreateUiAction
    data class AppendStrokePoint(val point: Offset) : CreateUiAction
    data class TransformCanvas(
        val centroid: Offset,
        val pan: Offset,
        val zoom: Float
    ) : CreateUiAction
    data class AddCustomPattern(val pattern: CustomPattern) : CreateUiAction
    data object EndStroke : CreateUiAction
    data object ToggleFold : CreateUiAction
    data object ClearCanvas : CreateUiAction
    data object DeleteActivePattern : CreateUiAction
    data object ConfirmActivePattern : CreateUiAction
    data object Undo : CreateUiAction
    data object Redo : CreateUiAction
}
