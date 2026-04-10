package com.example.kpappercutting.ui.features.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.features.home.HomeScreen

@Composable
fun CommunityScreen() {
    // 状态管理：当前选中的标签页索引 (0: 动态圈, 1: 云市)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val titles = listOf("动态圈", "云市")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F2)) // 背景色
    ) {
        // 1. 顶部自定义 TabBar
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent, // 设置为透明以露出背景色
            contentColor = Color(0xFFB02621),   // 选中的文字/指示器颜色
            divider = {}, // 去掉默认的底部细线（或者根据需要保留）
            indicator = { tabPositions ->
                // 自定义指示器：短红线
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .padding(horizontal = 60.dp), // 让指示器比文字短一点
                        color = Color(0xFFB02621),
                        height = 3.dp
                    )
                }
            },
            modifier = Modifier
        ) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) Color(0xFF333333) else Color.Gray
                        )
                    }
                )
            }
        }

        // 2. 内容区域：根据 Tab 切换不同的页面内容
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> TrendsList() // 动态圈列表
                1 -> MarketPlaceholder() // 云市占位
            }
        }
    }
}

@Composable
fun TrendsList() {
    // 模拟社区动态数据
    val postList = listOf(1, 2, 3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(postList) {
            PostItemCard()
        }
        // 底部留空，防止被悬浮按钮挡住
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun PostItemCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 用户信息行
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 头像占位
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("巧手艺人", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("2小时前", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 动态文字
            Text(
                text = "今天尝试剪了一个‘五福临门’，感觉线条处理得还不够圆润，大家有什么建议吗？",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 动态图片预览区 (占位)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text("作品图片展示区", color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 互动栏 (点赞、评论、分享占位)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InteractionButton("💬 12") // 评论
                InteractionButton("❤️ 45") // 点赞
                InteractionButton("📤 分享") // 分享
            }
        }
    }
}

@Composable
fun InteractionButton(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color.Gray)
}

@Composable
fun MarketPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "云市板块开发中...", color = Color.Gray)
    }
}


@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun HomeScreenPreview() {
    // 这里包裹你的主题
    MaterialTheme {
        CommunityScreen()
    }
}