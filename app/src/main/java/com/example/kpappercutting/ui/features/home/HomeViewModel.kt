// 该文件用于承载首页的状态与业务逻辑，负责把仓库数据转换为首页可渲染状态。
package com.example.kpappercutting.ui.features.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var uiState by mutableStateOf(HomeUiState())
        private set
}
