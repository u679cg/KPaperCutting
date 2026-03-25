package com.example.kpappercutting.ui.features.creation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.community.CommunityScreen
import com.example.kpappercutting.ui.theme.PaperRed

@Composable
fun CreateScreen() {
    // 状态管理
    var selectedShape by remember { mutableStateOf(PaperShape.CIRCLE) }
    var selectedTool by remember { mutableStateOf(EditTool.SCISSORS) }

    // 简单的绘图路径记录（用于模拟剪纸线）
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F2)) // 背景杏色
    ) {
        // --- 1. 中央画布层 ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(320.dp) // 纸张大小
                    .pointerInput(selectedTool) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                currentPath?.let { paths.add(it) }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentPath?.lineTo(change.position.x, change.position.y)
                            },
                            onDragEnd = { currentPath = null }
                        )
                    }
            ) {
                // 绘制底纸
                if (selectedShape == PaperShape.CIRCLE) {
                    drawCircle(color = PaperRed)
                } else {
                    drawRect(color = PaperRed)
                }

                // 绘制剪出的路径（模拟效果）
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFFFDF8F2), // 使用背景色模拟“剪断”效果
                        style = Stroke(width = 8f)
                    )
                }
            }
        }

        // --- 2. 顶部工具栏 ---
        TopControlBar(
            currentShape = selectedShape,
            onShapeChange = { selectedShape = it }
        )

        // --- 3. 左侧操作栏 (清空、撤销、恢复) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SideButton("🗑️", "清空") { paths.clear() }
            SideButton("↩️", "撤销") { paths.removeLastOrNull()}
            SideButton("↪️", "恢复")
        }

        // --- 4. 右侧操作栏 (折叠选择、展开) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SideButton("⌛", "五角")
            SideButton("🛡️", "八角")
            SideButton("✨", "展开")
        }

        // --- 5. 底部切换笔刷 ---
        BottomToolPalette(
            activeTool = selectedTool,
            onToolSelect = { selectedTool = it }
        )
    }
}

@Composable
fun TopControlBar(currentShape: PaperShape, onShapeChange: (PaperShape) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp,0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF5D4037))

        // 圆形/方形 切换器
        Surface(
            modifier = Modifier.height(40.dp).width(180.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.6f)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val modifier = Modifier.weight(1f).fillMaxHeight()
                ShapeTab("圆形", currentShape == PaperShape.CIRCLE, modifier) { onShapeChange(
                    PaperShape.CIRCLE) }
                ShapeTab("方形", currentShape == PaperShape.SQUARE, modifier) { onShapeChange(
                    PaperShape.SQUARE) }
            }
        }

        Icon(Icons.Default.Menu, contentDescription = null, tint = Color(0xFF5D4037))
    }
}

@Composable
fun ShapeTab(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PaperRed else Color.Transparent)
            .clickable { onClick() },
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
fun SideButton(icon: String, label: String, onClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(50.dp).shadow(4.dp, CircleShape).clickable { onClick() },
            shape = CircleShape,
            color = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 20.sp) // 占位图标
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun BottomToolPalette(activeTool: EditTool, onToolSelect: (EditTool) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.height(80.dp).width(240.dp).shadow(10.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolItem("✂️", "剪刀", activeTool == EditTool.SCISSORS) { onToolSelect(EditTool.SCISSORS) }
                ToolItem("🖌️", "铅笔", activeTool == EditTool.PENCIL) { onToolSelect(EditTool.PENCIL) }
                ToolItem("🧽", "橡皮", activeTool == EditTool.ERASER) { onToolSelect(EditTool.ERASER) }
            }
        }
    }
}

@Composable
fun ToolItem(icon: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .border(if(isActive) 2.dp else 0.dp, PaperRed, CircleShape)
                .background(if(isActive) PaperRed.copy(alpha = 0.1f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 24.sp)
        }
        Text(label, fontSize = 10.sp, color = if(isActive) PaperRed else Color.Gray)
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun HomeScreenPreview() {
    // 这里包裹你的主题
    MaterialTheme {
        CreateScreen()
    }
}

