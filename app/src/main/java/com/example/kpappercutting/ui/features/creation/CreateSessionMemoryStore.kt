package com.example.kpappercutting.ui.features.creation

import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

data class CreateUiSessionState(
    val selectedFoldTechnique: FoldTechniqueOption,
    val continuousFoldLayerCount: Int,
    val customPatterns: List<CustomPattern>,
    val activePattern: EditablePatternState?
)

object CreateSessionMemoryStore {
    var engineSessionState: PaperCutEngine.SessionState? = null
    var uiSessionState: CreateUiSessionState? = null
}
