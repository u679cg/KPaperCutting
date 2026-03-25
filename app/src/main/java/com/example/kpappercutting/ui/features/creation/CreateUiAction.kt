// 该文件用于定义创作页的用户操作类型，作为 Screen 与 ViewModel 之间的统一事件协议。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.ui.geometry.Offset
import com.example.kpappercutting.data.model.PaperShape

sealed interface CreateUiAction {
    data class SelectShape(val shape: PaperShape) : CreateUiAction
    data class SelectTool(val tool: EditTool) : CreateUiAction
    data class SelectFoldMode(val mode: FoldMode) : CreateUiAction
    data class StartStroke(val point: Offset) : CreateUiAction
    data class AppendStrokePoint(val point: Offset) : CreateUiAction
    data object EndStroke : CreateUiAction
    data object ToggleFold : CreateUiAction
    data object ClearCanvas : CreateUiAction
    data object Undo : CreateUiAction
    data object Redo : CreateUiAction
}
