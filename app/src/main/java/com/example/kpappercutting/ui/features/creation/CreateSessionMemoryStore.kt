package com.example.kpappercutting.ui.features.creation

import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

data class CreateUiSessionState(
    val selectedFoldTechnique: FoldTechniqueOption,
    val continuousFoldLayerCount: Int
)

object CreateSessionMemoryStore {
    var engineSessionState: PaperCutEngine.SessionState? = null
    var uiSessionState: CreateUiSessionState? = null
}
