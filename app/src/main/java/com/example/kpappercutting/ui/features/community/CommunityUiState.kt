// 该文件用于定义社区页的 UI 状态，管理当前标签和动态列表的页面数据。
package com.example.kpappercutting.ui.features.community

data class CommunityUiState(
    val selectedTabIndex: Int = 0,
    val tabs: List<String> = listOf("动态圈", "云市")
)
