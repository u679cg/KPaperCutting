// 该文件用于承载创作页顶部控制栏组件，后续负责返回、纸张形状切换和菜单入口。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.runtime.Composable
import com.example.kpappercutting.data.model.PaperShape

@Composable
fun CreationTopControlBar(
    currentShape: PaperShape,
    onShapeChange: (PaperShape) -> Unit,
    onBack: () -> Unit
) {
    // 这里预留给后续的顶部控制栏 UI。
}
