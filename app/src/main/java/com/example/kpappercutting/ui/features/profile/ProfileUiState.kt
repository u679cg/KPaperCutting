// 该文件用于定义个人页的 UI 状态，集中表达用户信息、统计信息和当前标签页。
package com.example.kpappercutting.ui.features.profile

data class ProfileUiState(
    val nickname: String = "陈陈 ✨",
    val paperCutId: String = "746u679c",
    val region: String = "北京",
    val selectedTabIndex: Int = 0
)
