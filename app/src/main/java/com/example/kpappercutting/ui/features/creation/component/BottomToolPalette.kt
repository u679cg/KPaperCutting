// 该文件用于承载创作页底部工具栏组件，后续负责剪刀、铅笔和橡皮等工具切换。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.features.creation.EditTool
import com.example.kpappercutting.ui.theme.PaperRed

@Composable
fun BottomToolPaletteSection(
    modifier: Modifier = Modifier,
    activeTool: EditTool,
    onToolSelect: (EditTool) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 40.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(80.dp)
                .width(240.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolItem("✂️", "剪刀", activeTool == EditTool.SCISSORS) {
                    onToolSelect(EditTool.SCISSORS)
                }
                ToolItem("🖌️", "铅笔", activeTool == EditTool.PENCIL) {
                    onToolSelect(EditTool.PENCIL)
                }
                ToolItem("🧽", "橡皮", activeTool == EditTool.ERASER) {
                    onToolSelect(EditTool.ERASER)
                }
            }
        }
    }
}

@Composable
private fun ToolItem(
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .border(
                    width = if (isActive) 2.dp else 0.dp,
                    color = PaperRed,
                    shape = CircleShape
                )
                .background(if (isActive) PaperRed.copy(alpha = 0.1f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Text(label, fontSize = 10.sp, color = if (isActive) PaperRed else Color.Gray)
    }
}
