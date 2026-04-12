package com.example.kpappercutting.ui.features.creation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.features.creation.ContinuousFoldLayerOptions
import com.example.kpappercutting.ui.features.creation.FoldTechniqueCategory
import com.example.kpappercutting.ui.features.creation.FoldTechniqueOption
import com.example.kpappercutting.ui.theme.PaperRed
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldTechniqueBottomSheet(
    currentTechnique: FoldTechniqueOption,
    continuousFoldLayerCount: Int,
    onTechniqueSelected: (FoldTechniqueOption) -> Unit,
    onContinuousLayerCountChange: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by rememberSaveable(currentTechnique) {
        mutableStateOf(currentTechnique.category)
    }

    LaunchedEffect(currentTechnique) {
        selectedCategory = currentTechnique.category
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFFFFFBF7),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(42.dp)
                    .height(5.dp)
                    .background(Color(0xFFD7D0C9), CircleShape)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "选择剪纸折法",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF252525),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FoldTechniqueCategory.entries.forEach { category ->
                    FoldTechniqueTab(
                        category = category,
                        selected = category == selectedCategory,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedCategory = category }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE1DBD4), thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            when (selectedCategory) {
                FoldTechniqueCategory.CONTINUOUS_TWO_PART -> {
                    ContinuousFoldTechniqueContent(
                        selected = currentTechnique == FoldTechniqueOption.CONTINUOUS,
                        layerCount = continuousFoldLayerCount,
                        onTechniqueSelected = { onTechniqueSelected(FoldTechniqueOption.CONTINUOUS) },
                        onLayerCountChange = onContinuousLayerCountChange
                    )
                }

                else -> {
                    val options = remember(selectedCategory) {
                        FoldTechniqueOption.selectableOptionsByCategory.getValue(selectedCategory)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEach { option ->
                            FoldTechniqueCard(
                                option = option,
                                selected = option == currentTechnique,
                                modifier = Modifier.weight(1f),
                                onClick = { onTechniqueSelected(option) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun FoldTechniqueTab(
    category: FoldTechniqueCategory,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = category.title,
            color = if (selected) PaperRed else Color(0xFF7B756F),
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(
                    color = if (selected) PaperRed else Color.Transparent,
                    shape = RoundedCornerShape(999.dp)
                )
        )
    }
}

@Composable
private fun ContinuousFoldTechniqueContent(
    selected: Boolean,
    layerCount: Int,
    onTechniqueSelected: () -> Unit,
    onLayerCountChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FoldTechniqueCard(
                option = FoldTechniqueOption.CONTINUOUS,
                selected = selected,
                modifier = Modifier.width(120.dp),
                onClick = onTechniqueSelected
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "连续层数：$layerCount",
            color = Color(0xFF4C4A47),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        val sliderIndex = ContinuousFoldLayerOptions.indexOf(layerCount).coerceAtLeast(0)
        Slider(
            value = sliderIndex.toFloat(),
            onValueChange = { raw ->
                val mappedIndex = raw.roundToInt().coerceIn(0, ContinuousFoldLayerOptions.lastIndex)
                onLayerCountChange(ContinuousFoldLayerOptions[mappedIndex])
            },
            valueRange = 0f..ContinuousFoldLayerOptions.lastIndex.toFloat(),
            steps = max(ContinuousFoldLayerOptions.size - 2, 0),
            colors = SliderDefaults.colors(
                thumbColor = PaperRed,
                activeTrackColor = PaperRed,
                inactiveTrackColor = Color(0xFFE6DED7)
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ContinuousFoldLayerOptions.forEach { value ->
                Text(
                    text = value.toString(),
                    color = if (value == layerCount) PaperRed else Color(0xFF9A938C),
                    fontSize = 12.sp,
                    fontWeight = if (value == layerCount) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "二分连续法当前仅提供界面占位，后续将在引擎中补充真实连续折叠几何。",
            color = Color(0xFF8B837B),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun FoldTechniqueCard(
    option: FoldTechniqueOption,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = if (selected) Color(0xFFFFF1E6) else Color(0xFFF8F4EF),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    color = if (selected) Color(0xFFFFE3CF) else Color.White,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            FoldTechniqueIcon(option = option, selected = selected)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = option.label,
            color = if (selected) PaperRed else Color(0xFF53504C),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun FoldTechniqueIcon(
    option: FoldTechniqueOption,
    selected: Boolean
) {
    val strokeColor = if (selected) PaperRed else Color(0xFF8E857D)
    Canvas(modifier = Modifier.size(30.dp)) {
        val strokeWidth = 2.dp.toPx()
        drawRoundRect(
            color = strokeColor.copy(alpha = 0.18f),
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
        drawRoundRect(
            color = strokeColor,
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        when (option) {
            FoldTechniqueOption.CONTINUOUS -> {
                val yPositions = listOf(7f, 13f, 19f, 25f).map { it.dp.toPx() }
                yPositions.forEach { y ->
                    drawLine(
                        color = strokeColor,
                        start = Offset(6.dp.toPx(), y),
                        end = Offset(size.width - 6.dp.toPx(), y),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }

            else -> {
                val segments = when (option) {
                    FoldTechniqueOption.TWO_PART -> 1
                    FoldTechniqueOption.FOUR_PART -> 2
                    FoldTechniqueOption.EIGHT_PART -> 3
                    FoldTechniqueOption.SIXTEEN_PART -> 4
                    FoldTechniqueOption.THREE_PART -> 2
                    FoldTechniqueOption.SIX_PART -> 3
                    FoldTechniqueOption.TWELVE_PART -> 4
                    FoldTechniqueOption.FIVE_PART -> 3
                    FoldTechniqueOption.TEN_PART -> 4
                    FoldTechniqueOption.CONTINUOUS -> 0
                }
                repeat(segments) { index ->
                    val fraction = (index + 1f) / (segments + 1f)
                    val x = size.width * fraction
                    drawLine(
                        color = strokeColor,
                        start = Offset(x, 5.dp.toPx()),
                        end = Offset(x, size.height - 5.dp.toPx()),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
