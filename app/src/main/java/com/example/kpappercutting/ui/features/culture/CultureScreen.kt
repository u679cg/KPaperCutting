package com.example.kpappercutting.ui.features.culture

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.R
import com.example.kpappercutting.ui.theme.BackgroundCream
import com.example.kpappercutting.ui.theme.CreamYellow
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

private data class EraStory(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val imageTitle: String,
    val description: String
)

private data class TechniqueItem(
    val emoji: String,
    val title: String
)

private data class PatternItem(
    val emoji: String,
    val title: String
)

private val eraStories = listOf(
    EraStory(
        title = "春秋至",
        subtitle = "秦汉",
        emoji = "🏺",
        imageTitle = "早期纹样",
        description = "剪纸在这一时期更多以镂空饰片和礼俗装饰的形态出现，材料与工艺仍在发展，但已经能看到对对称、吉祥纹样和节庆用途的审美偏好。"
    ),
    EraStory(
        title = "魏晋南",
        subtitle = "北朝",
        emoji = "🕊️",
        imageTitle = "佛影窗花",
        description = "随着宗教文化与民间装饰艺术交织，剪纸逐渐从礼仪附属走向更明确的观赏表达，纹样更轻逸，人物与禽鸟题材也开始丰富起来。"
    ),
    EraStory(
        title = "隋唐至",
        subtitle = "五代",
        emoji = "🐉",
        imageTitle = "盛唐祥瑞",
        description = "如果把中华剪纸史比作一幅长卷，那隋唐五代几乎是最舒展的一段。社会繁荣、节俗兴盛，剪纸在婚嫁、节庆与祈福场景中广泛流行，图案也更饱满华丽。"
    ),
    EraStory(
        title = "宋辽元",
        subtitle = "时代",
        emoji = "🏮",
        imageTitle = "市井雅趣",
        description = "宋元时期市民文化活跃，剪纸更贴近日常生活，窗花、灯彩和岁时装饰的应用明显增加，构图更加讲究层次与趣味，形成了细腻又生活化的面貌。"
    ),
    EraStory(
        title = "明代至",
        subtitle = "清代",
        emoji = "🦋",
        imageTitle = "民俗高峰",
        description = "到了明清，剪纸真正进入民间普及与地方风格繁盛的高峰期。各地题材、刀法和构图语言逐渐分化成鲜明流派，喜庆吉祥与戏曲故事题材尤为常见。"
    )
)

private val techniqueItems = listOf(
    TechniqueItem("✂️", "剪刻技法"),
    TechniqueItem("📐", "折剪技法"),
    TechniqueItem("🧠", "创作方法")
)

private val patternItems = listOf(
    PatternItem("🪢", "盘长纹"),
    PatternItem("🔶", "方胜纹"),
    PatternItem("🌸", "连环纹"),
    PatternItem("💮", "联珠纹")
)

@Composable
fun CultureScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
            .verticalScroll(rememberScrollState())
    ) {
        CultureHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-28).dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFFDFDFC))
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            PaperCutHistorySection()
            Spacer(modifier = Modifier.height(28.dp))
            SectionTitle("剪纸技法")
            Spacer(modifier = Modifier.height(14.dp))
            TechniqueSection()
            Spacer(modifier = Modifier.height(28.dp))
            SectionTitle("经典纹样")
            Spacer(modifier = Modifier.height(20.dp))
            ClassicPatternsSection()
            Spacer(modifier = Modifier.height(26.dp))
            BottomEndMark()
//            Spacer(modifier = Modifier.height(24.dp))
//            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun CultureHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(CreamYellow)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner_1),
            contentDescription = "剪纸文化 Banner",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PaperCutHistorySection() {
    val eraCount = eraStories.size
    val initialIndex = 2
    val virtualPageCount = Int.MAX_VALUE
    val pageWidth = 64.dp
    val pageSpacing = 10.dp
    val startPage = remember {
        val midpoint = virtualPageCount / 2
        midpoint - (midpoint % eraCount) + initialIndex
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )
    val coroutineScope = rememberCoroutineScope()
    val selectedEra = eraStories[pagerState.currentPage.floorMod(eraCount)]

    Column {
        SectionTitle("剪纸史")
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val horizontalContentPadding = (maxWidth - pageWidth) / 2

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                pageSize = PageSize.Fixed(pageWidth),
                pageSpacing = pageSpacing,
                beyondViewportPageCount = 2,
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = horizontalContentPadding)
            ) { page ->
                val eraIndex = page.floorMod(eraCount)
                val era = eraStories[eraIndex]
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val selectedFraction = (1f - pageOffset.coerceIn(0f, 1f))
                val isSelected = pageOffset < 0.5f
                val scaleTarget = 0.82f + (selectedFraction * 0.32f)
                val heightTarget = 78.dp + (24.dp * selectedFraction)

                val cardScale by animateFloatAsState(
                    targetValue = scaleTarget,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "eraCardScale"
                )
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFFFF8F0) else Color.White,
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "eraCardColor"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFE7C38E) else Color(0x00000000),
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "eraCardBorder"
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightTarget)
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                        }
                        .shadow(
                            elevation = if (isSelected) 10.dp else 5.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x1F000000),
                            spotColor = Color(0x1F000000)
                        )
                        .clickable {
                            coroutineScope.launch { pagerState.animateScrollToPage(page) }
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = containerColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = era.title,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = if (isSelected) Color(0xFF7F4E1E) else Color(0xFF8E8E8E),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = era.subtitle,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = if (isSelected) Color(0xFF7F4E1E) else Color(0xFF8E8E8E),
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF8F1E7),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(
                text = selectedEra.description,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                color = Color(0xFF8A7A67),
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
        }
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

@Composable
private fun TechniqueSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        techniqueItems.forEach { item ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = 34.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        color = Color(0xFF5A5A5A)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicPatternsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        patternItems.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCEEEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = 28.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    color = Color(0xFF616161)
                )
            }
        }
    }
}

@Composable
private fun BottomEndMark() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(1.dp)
                .background(Color(0xFFE0DED7))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "到底了呦",
            color = Color(0xFFBDB7AE),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(1.dp)
                .background(Color(0xFFE0DED7))
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFF3D3D3D),
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
private fun CultureScreenPreview() {
    MaterialTheme {
        CultureScreen()
    }
}
