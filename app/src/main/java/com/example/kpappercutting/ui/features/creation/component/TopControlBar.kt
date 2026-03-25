// 该文件用于承载创作页顶部控制栏组件，后续负责返回、纸张形状切换和菜单入口。
package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.creation.CreationMenuAction
import com.example.kpappercutting.ui.theme.PaperRed

@Composable
fun CreationTopControlBar(
    currentShape: PaperShape,
    onShapeChange: (PaperShape) -> Unit,
    onMenuAction: (CreationMenuAction) -> Unit,
    onBack: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box{
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF5D4037)
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .padding(horizontal = 12.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.6f)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val tabModifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                ShapeTab(
                    text = "圆形",
                    isSelected = currentShape == PaperShape.CIRCLE,
                    modifier = tabModifier
                ) {
                    onShapeChange(PaperShape.CIRCLE)
                }
                ShapeTab(
                    text = "方形",
                    isSelected = currentShape == PaperShape.SQUARE,
                    modifier = tabModifier
                ) {
                    onShapeChange(PaperShape.SQUARE)
                }
            }
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF5D4037)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                CreationMenuItem(
                    text = "画布初始化",
                    action = CreationMenuAction.INITIALIZE_CANVAS,
                    onMenuAction = onMenuAction,
                    onDismiss = { menuExpanded = false }
                )
                CreationMenuItem(
                    text = "保存草稿",
                    action = CreationMenuAction.SAVE_DRAFT,
                    onMenuAction = onMenuAction,
                    onDismiss = { menuExpanded = false }
                )
                CreationMenuItem(
                    text = "导出相册",
                    action = CreationMenuAction.EXPORT_TO_GALLERY,
                    onMenuAction = onMenuAction,
                    onDismiss = { menuExpanded = false }
                )
                CreationMenuItem(
                    text = "发布社区",
                    action = CreationMenuAction.PUBLISH_TO_COMMUNITY,
                    onMenuAction = onMenuAction,
                    onDismiss = { menuExpanded = false }
                )
            }
        }
    }
}

@Composable
private fun ShapeTab(
    text: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PaperRed else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CreationMenuItem(
    text: String,
    action: CreationMenuAction,
    onMenuAction: (CreationMenuAction) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = {
            onDismiss()
            onMenuAction(action)
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFDF8F2)
@Composable
private fun CreationTopControlBarPreview() {
    CreationTopControlBar(
        currentShape = PaperShape.CIRCLE,
        onShapeChange = {},
        onMenuAction = {},
        onBack = {}
    )
}
