// 该文件用于承载社区页的状态与业务逻辑，负责标签切换和后续动态流数据加载。
package com.example.kpappercutting.ui.features.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CommunityViewModel : ViewModel() {
    var uiState by mutableStateOf(CommunityUiState())
        private set

    fun selectTab(index: Int) {
        uiState = uiState.copy(selectedTabIndex = index)
    }
}
