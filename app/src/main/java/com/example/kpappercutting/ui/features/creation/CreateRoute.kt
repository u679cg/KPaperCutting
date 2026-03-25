// 该文件用于承接创作页的 ViewModel 与导航入口，把状态驱动的 Route 与纯渲染 Screen 分离开。
package com.example.kpappercutting.ui.features.creation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateRoute(
    onBack: () -> Unit = {},
    viewModel: CreateViewModel = viewModel()
) {
    CreateScreen(
        uiState = viewModel.uiState,
        onAction = viewModel::onAction,
        onBack = onBack
    )
}
