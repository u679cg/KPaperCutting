package com.example.kpappercutting.ui.features.culture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kpappercutting.ui.theme.BackgroundCream
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun CultureScreen(
    viewModel: CultureViewModel = viewModel()
) {
    CultureScreenContent(
        uiState = viewModel.uiState,
        onEraSettled = viewModel::selectEra
    )
}

@Composable
private fun CultureScreenContent(
    uiState: CultureUiState,
    onEraSettled: (Int) -> Unit
) {
    val headerHeight = 200.dp
    val panelOverlap = 28.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        CultureHeader(
            bannerResId = uiState.bannerResId,
            modifier = Modifier.align(Alignment.TopCenter),
            headerHeight = headerHeight
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = headerHeight - panelOverlap)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    ambientColor = Color(0xFF000000),
                    spotColor = Color(0xFF000000)
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFFFDFDFC))
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            PaperCutHistorySection(
                eras = uiState.eras,
                selectedEra = uiState.selectedEra,
                initialSelectedIndex = uiState.selectedEraIndex,
                onEraSettled = onEraSettled
            )
            Spacer(modifier = Modifier.height(28.dp))
            SectionTitle("剪纸技法")
            Spacer(modifier = Modifier.height(14.dp))
            TechniqueSection(uiState.techniques)
            Spacer(modifier = Modifier.height(28.dp))
            SectionTitle("经典纹样")
            Spacer(modifier = Modifier.height(20.dp))
            ClassicPatternsSection(uiState.patterns)
            Spacer(modifier = Modifier.height(26.dp))
            BottomEndMark()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CultureHeader(
    bannerResId: Int,
    modifier: Modifier = Modifier,
    headerHeight: androidx.compose.ui.unit.Dp = 200.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(Color(0xFFF7DF9A))
    ) {
        Image(
            painter = painterResource(id = bannerResId),
            contentDescription = "剪纸文化 Banner",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun PaperCutHistorySection(
    eras: List<CultureEraUiModel>,
    selectedEra: CultureEraUiModel,
    initialSelectedIndex: Int,
    onEraSettled: (Int) -> Unit
) {
    if (eras.isEmpty()) return

    val eraCount = eras.size
    val normalizedInitialIndex = initialSelectedIndex.coerceIn(0, eraCount - 1)
    val virtualPageCount = Int.MAX_VALUE
    val pageWidth = 64.dp
    val pageSpacing = 10.dp
    val cardHeight = 102.dp
    val availableSectionWidth = LocalConfiguration.current.screenWidthDp.dp - 32.dp
    val horizontalContentPadding = (availableSectionWidth - pageWidth) / 2
    val startPage = remember(eraCount) {
        val midpoint = virtualPageCount / 2
        midpoint - (midpoint % eraCount) + normalizedInitialIndex
    }
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState, eraCount, onEraSettled) {
        snapshotFlow { pagerState.settledPage }
            .map { it.floorMod(eraCount) }
            .distinctUntilChanged()
            .collect(onEraSettled)
    }

    Column {
        SectionTitle("剪纸史")
        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            pageSize = PageSize.Fixed(pageWidth),
            pageSpacing = pageSpacing,
            beyondViewportPageCount = 1,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = horizontalContentPadding)
        ) { page ->
            val eraIndex = page.floorMod(eraCount)
            val era = eras[eraIndex]
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)
            val selectedFraction = 1f - pageOffset
            val cardScale = lerp(0.9f, 1.08f, selectedFraction)
            val cardAlpha = lerp(0.72f, 1f, selectedFraction)
            val containerColor = lerp(Color.White, Color(0xFFFFF8F0), selectedFraction)
            val borderColor = lerp(Color(0xFF5F5A53), Color(0xFF7F4E1E), selectedFraction)
            val textColor = lerp(Color(0xFF8E8E8E), Color(0xFF7F4E1E), selectedFraction)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    }
                    .clickable {
                        coroutineScope.launch { pagerState.animateScrollToPage(page) }
                    },
                shape = RoundedCornerShape(16.dp),
                color = containerColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = era.title,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontWeight = if (selectedFraction > 0.6f) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = era.subtitle,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        fontWeight = if (selectedFraction > 0.6f) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )
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

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
private fun TechniqueSection(
    items: List<CultureTechniqueUiModel>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items.forEach { item ->
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
private fun ClassicPatternsSection(
    items: List<CulturePatternUiModel>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        items.forEach { item ->
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
        CultureScreenContent(
            uiState = CultureUiState(),
            onEraSettled = {}
        )
    }
}
