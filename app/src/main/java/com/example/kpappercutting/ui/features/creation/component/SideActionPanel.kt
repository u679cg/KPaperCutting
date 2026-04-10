// 该文件用于承载创作页两侧的快捷操作按钮组，统一管理清空、撤销、折法切换等侧边操作。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SideActionPanel(
    actions: List<SideActionItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.forEach { action ->
            SideButton(
                icon = action.icon,
                label = action.label,
                enabled = action.enabled,
                onClick = action.onClick
            )
        }
    }
}

data class SideActionItem(
    val icon: String,
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit = {}
)

@Composable
private fun SideButton(
    icon: String,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .shadow(4.dp, CircleShape)
                .clickable(enabled = enabled, onClick = onClick),
            shape = CircleShape,
            color = if (enabled) Color.White else Color(0xFFF1F1F1)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) Color.Gray else Color.LightGray
        )
    }
}
