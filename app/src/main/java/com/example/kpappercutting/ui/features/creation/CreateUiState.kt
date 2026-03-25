// 该文件用于定义创作页的 UI 状态，统一描述纸张形状、工具、轨迹和可操作状态。
package com.example.kpappercutting.ui.features.creation

import com.example.kpappercutting.data.model.PaperShape

data class CreateUiState(
    val selectedShape: PaperShape = PaperShape.CIRCLE,
    val selectedTool: EditTool = EditTool.SCISSORS,
    val selectedFoldMode: FoldMode = FoldMode.FIVE_POINT,
    val strokes: List<DrawStroke> = emptyList(),
    val currentStroke: DrawStroke? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
