// 该文件用于承载创作页底部工具栏组件，后续负责剪刀、铅笔和橡皮等工具切换。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.runtime.Composable
import com.example.kpappercutting.ui.features.creation.EditTool

@Composable
fun BottomToolPaletteSection(
    activeTool: EditTool,
    onToolSelect: (EditTool) -> Unit
) {
    // 这里预留给后续的底部工具栏 UI。
}
