// 该文件用于定义首页的 UI 状态，集中管理页面渲染所需的数据快照。
package com.example.kpappercutting.ui.features.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val quickActions: List<String> = emptyList()
)
