package com.example.kpappercutting.ui.features.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.creation.component.BottomToolPaletteSection
import com.example.kpappercutting.ui.features.creation.component.CreationTopControlBar
import com.example.kpappercutting.ui.features.creation.component.PaperCanvas
import com.example.kpappercutting.ui.features.creation.component.SideActionItem
import com.example.kpappercutting.ui.features.creation.component.SideActionPanel
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine

@Composable
fun CreateScreen(
    uiState: CreateUiState,
    engine: PaperCutEngine,
    onAction: (CreateUiAction) -> Unit,
    onMenuAction: (CreationMenuAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-96).dp),
            contentAlignment = Alignment.Center
        ) {
            PaperCanvas(
                uiState = uiState,
                engine = engine,
                modifier = Modifier.size(280.dp),
                onAction = onAction
            )
        }

        CreationTopControlBar(
            currentShape = uiState.selectedShape,
            onShapeChange = { onAction(CreateUiAction.SelectShape(it)) },
            onMenuAction = onMenuAction,
            onBack = onBack
        )

        SideActionPanel(
            actions = listOf(
                SideActionItem("🗑️", "清空") { onAction(CreateUiAction.ClearCanvas) },
                SideActionItem("↩️", "撤销", enabled = uiState.canUndo) {
                    onAction(CreateUiAction.Undo)
                },
                SideActionItem("↪️", "恢复", enabled = uiState.canRedo) {
                    onAction(CreateUiAction.Redo)
                }
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, top = 144.dp)
        )

        SideActionPanel(
            actions = listOf(
                SideActionItem(
                    icon = if (uiState.foldMode == FoldMode.FIVE_POINT) "⌛" else "◻️",
                    label = "五角",
                    enabled = uiState.canSelectFiveFold
                ) {
                    onAction(CreateUiAction.SelectFoldMode(FoldMode.FIVE_POINT))
                },
                SideActionItem(
                    icon = if (uiState.foldMode == FoldMode.EIGHT_POINT) "🛡️" else "◻️",
                    label = "八角",
                    enabled = uiState.canSelectEightFold
                ) {
                    onAction(CreateUiAction.SelectFoldMode(FoldMode.EIGHT_POINT))
                },
                SideActionItem(
                    icon = "✨",
                    label = if (uiState.isFolded) "展开" else "收起",
                    enabled = uiState.canExpand
                ) {
                    onAction(CreateUiAction.ToggleFold)
                }
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp, top = 144.dp)
        )

        BottomToolPaletteSection(
            activeTool = uiState.selectedTool,
            onToolSelect = { onAction(CreateUiAction.SelectTool(it)) }
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun CreateScreenPreview() {
    MaterialTheme {
        CreateScreen(
            uiState = CreateUiState(
                selectedShape = PaperShape.CIRCLE,
                selectedTool = EditTool.SCISSORS,
                canUndo = true
            ),
            engine = PaperCutEngine(),
            onAction = {},
            onMenuAction = {}
        )
    }
}

