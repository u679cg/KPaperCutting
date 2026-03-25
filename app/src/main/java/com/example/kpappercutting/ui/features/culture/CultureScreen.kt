package com.example.kpappercutting.ui.features.culture

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CultureScreen() {
    // 整个页面可滚动
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F2)) // 统一的杏色背景
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        // 1. 顶部红色背景标题区
        CultureHeader()

        // 2. 剪纸时光机卡片 (向上偏移盖住页眉)
//        Spacer(modifier = Modifier.height((-40).dp))
        TimeMachineCard()

        // 3. 剪纸基本技法部分
        SectionTitle("剪纸基本技法")
        TechniqueRow()

        // 4. 每日剪纸科普
        SectionTitle("每日剪纸科普")
        DailyKnowledgeCard()

        Spacer(modifier = Modifier.height(88.dp)) // 为悬浮按钮留出空间
    }
}

@Composable
fun CultureHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFF8B1A1A)) // 深红色
            .padding(start = 24.dp, top = 40.dp)
    ) {
        // 背景装饰圆 (半透明)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .size(180.dp)
                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(90.dp))
        )

        Column {
            Text(
                text = "剪纸文化",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "镂空之美",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "PAPERCUT",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TimeMachineCard() {
    // 当前选中的年份状态
    var selectedEra by remember { mutableStateOf("明") }
    val eras = listOf("唐", "宋", "明", "清", "现代")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("剪纸时光机", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            // 时光机选择器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                eras.forEach { era ->
                    val isSelected = era == selectedEra
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedEra = era }
                    ) {
                        Text(
                            text = era,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // 图标占位符
                        Box(
                            modifier = Modifier
                                .size(50.dp, 70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF8B1A1A) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(if (isSelected) Color.White else Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🎨", fontSize = 16.sp) // 占位图
                                Text(
                                    text = if(era=="唐") "宗教\n祈福" else "极致\n精细",
                                    fontSize = 8.sp,
                                    lineHeight = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 介绍文字框
            Surface(
                color = Color(0xFFF7F7F7),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${selectedEra}代剪纸：技艺达到高峰，婚俗窗花盛行。点此查看【${selectedEra}代画卷】详细介绍...",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TechniqueRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val techniques = listOf("阴阳剪", "折叠剪", "多角折法")
        techniques.forEach { name ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📖", fontSize = 24.sp) // 图标占位
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("点击了解", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DailyKnowledgeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "你知道我国最早的剪纸出土于哪里吗？",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "我国最早的剪纸出土于新疆吐鲁番阿斯塔那古墓群，至今已有六角折法纹的剪纸塌裂染...",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun HomeScreenPreview() {
    // 这里包裹你的主题
    MaterialTheme {
        CultureScreen()
    }
}