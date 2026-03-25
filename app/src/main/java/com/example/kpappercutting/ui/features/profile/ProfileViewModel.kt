// 该文件用于承载个人页的状态与业务逻辑，负责管理用户资料和标签切换状态。
package com.example.kpappercutting.ui.features.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun selectTab(index: Int) {
        uiState = uiState.copy(selectedTabIndex = index)
    }
}
