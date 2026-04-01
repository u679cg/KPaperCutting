package com.example.kpappercutting.ui.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.R
import com.example.kpappercutting.ui.theme.BackgroundCream
import com.example.kpappercutting.ui.theme.CreamYellow
import com.example.kpappercutting.ui.theme.PanmenFontFamily

@Composable
fun HomeScreen() {
    // 使用 Column 并开启纵向滚动
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream) // 对应图中的奶油色背景
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. 顶部栏 (标题 + 搜索框 + 通知)
        TopSearchBar()

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 红色主横幅 (非遗剪纸 正在新生)
        PromotionCard()

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 四个快捷功能入口
        QuickActionGrid()

        Spacer(modifier = Modifier.height(30.dp))

        // 4. 我的关注动态 标题
        Text(
            text = "我的关注动态",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )

        // 此处下方可以继续添加动态列表 (LazyColumn 处理)
        Spacer(modifier = Modifier.height(100.dp)) // 留出底部栏空间
    }
}

@Composable
fun TopSearchBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧 Logo 文字
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "指尖",
                color = Color(0xFFB02621),
                fontSize = 18.sp,
                fontFamily = PanmenFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
            Text(
                text = "生花",
                color = Color(0xFFB02621),
                fontSize = 18.sp,
                fontFamily = PanmenFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 中间搜索栏
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(46.dp),
            color = Color(0xFFF3E9DD), // 浅米色
            shape = RoundedCornerShape(23.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "搜索模板和作品",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 右侧通知图标
        Icon(
            painter = painterResource(id = R.drawable.ic_notification),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color(0xFF444444)
        )
    }
}

@Composable
fun PromotionCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CreamYellow) // 主色调红
//            .padding(24.dp)
    ) {
        //bannner
        Image(
            painter = painterResource(id = R.drawable.banner_1),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center),
            contentScale = ContentScale.FillHeight
        )

    }
}

@Composable
fun QuickActionGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionItem("闯关", "ic_game") // 这里的字符串是占位符
        ActionItem("妙想", "ic_greatthink")
        ActionItem("科普", "ic_popscience")
        ActionItem("挑战", "ic_challenge")
    }
}

@Composable
fun ActionItem(label: String, iconPlaceholder: String) {
    val iconRes = when (iconPlaceholder) {
        "ic_game" -> R.drawable.ic_game
        "ic_greatthink" -> R.drawable.ic_greatthink
        "ic_popscience" -> R.drawable.ic_popscience
        "ic_challenge" -> R.drawable.ic_challenge
        else -> R.drawable.ic_game
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(16.dp),
//            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF666666)
        )
    }
}


@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun HomeScreenPreview() {
    // 这里包裹你的主题
    MaterialTheme {
        HomeScreen()
    }
}

