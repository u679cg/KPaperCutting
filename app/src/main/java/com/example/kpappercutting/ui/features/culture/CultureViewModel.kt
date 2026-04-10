package com.example.kpappercutting.ui.features.culture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CultureViewModel : ViewModel() {
    var uiState by mutableStateOf(CultureUiState())
        private set

    fun selectEra(index: Int) {
        if (index !in uiState.eras.indices || index == uiState.selectedEraIndex) return
        uiState = uiState.copy(selectedEraIndex = index)
    }
}
