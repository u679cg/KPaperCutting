// 该文件用于定义创作页专属模型，包括工具类型、折法类型和绘制轨迹数据。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.ui.geometry.Offset

enum class EditTool {
    SCISSORS,
    PENCIL,
    ERASER
}

enum class FoldMode {
    FIVE_POINT,
    EIGHT_POINT
}

data class DrawStroke(
    val points: List<Offset> = emptyList()
)
