// 该文件用于承载文化页的状态与业务逻辑，负责管理年代切换等页面交互。
package com.example.kpappercutting.ui.features.culture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CultureViewModel : ViewModel() {
    var uiState by mutableStateOf(CultureUiState())
        private set

    fun selectEra(era: String) {
        uiState = uiState.copy(selectedEra = era)
    }
}
