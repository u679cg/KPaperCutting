package com.example.kpappercutting.ui.features.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.example.kpappercutting.ui.theme.ProfileDarkBg
import com.example.kpappercutting.ui.theme.TagGray

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top=12.dp)
            .background(ProfileDarkBg) // 顶部深色背景
    ) {
        // 1. 顶部个人信息区域
        ProfileHeaderSection()

        // 2. 底部内容区域 (圆角卡片)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFFFDF8F2)) // 内容区背景
        ) {
            ProfileContentSection()
        }
    }
}

@Composable
fun ProfileHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .statusBarsPadding()
    ) {
        // 第一行：头像 + 基本信息
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 头像占位 (红色房子图标那个)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f)), // 头像底色
                contentAlignment = Alignment.Center
            ) {
                // 占位图标
                Text("🏠", fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "陈陈 ✨",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "剪纸号：746u679c",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "IP属地：北京",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 简介
        Text(
            text = "点击这里，填写简介",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 标签行
        Row {
            ProfileTag("22岁")
            Spacer(modifier = Modifier.width(8.dp))
            ProfileTag("北京海淀")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 统计数据与按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 统计数据
            Row {
                StatItem("17", "关注")
                Spacer(modifier = Modifier.width(20.dp))
                StatItem("75", "粉丝")
                Spacer(modifier = Modifier.width(20.dp))
                StatItem("602", "获赞与收藏")
            }

            // 编辑资料与设置
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("编辑资料", fontSize = 12.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContentSection() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("作品", "收藏夹")

    Column(modifier = Modifier.fillMaxSize()) {
        // TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF5D4037),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]).padding(horizontal = 60.dp),
                    color = Color(0xFFB02621),
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) Color(0xFF5D4037) else Color.Gray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 只有在“作品”Tab下显示的草稿箱入口
        if (selectedTab == 0) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✂️", fontSize = 18.sp) // 图标占位
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "草稿箱",
                        modifier = Modifier.weight(1f),
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )
                    Text(text = "0", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = ">", color = Color.LightGray)
                }
            }
        }

        // 此处可以放置作品列表 (LazyVerticalGrid)
    }
}

@Composable
fun ProfileTag(text: String) {
    Surface(
        color = TagGray,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun HomeScreenPreview() {
    // 这里包裹你的主题
    MaterialTheme {
        ProfileScreen()
    }
}