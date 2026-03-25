// 该文件用于定义创作页的 UI 状态，统一描述纸张、工具、折叠和渲染版本等可视状态。
package com.example.kpappercutting.ui.features.creation

import com.example.kpappercutting.data.model.PaperShape

data class CreateUiState(
    val selectedShape: PaperShape = PaperShape.CIRCLE,
    val selectedTool: EditTool = EditTool.SCISSORS,
    val foldMode: FoldMode = FoldMode.NONE,
    val isFolded: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val canExpand: Boolean = false,
    val canSelectFiveFold: Boolean = true,
    val canSelectEightFold: Boolean = true,
    val renderVersion: Int = 0
)
