// 该文件用于定义文化页的 UI 状态，集中表达当前时间轴、技法和科普内容。
package com.example.kpappercutting.ui.features.culture

data class CultureUiState(
    val selectedEra: String = "明",
    val eras: List<String> = listOf("唐", "宋", "明", "清", "现代")
)
