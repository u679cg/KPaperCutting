package com.example.kpappercutting.ui.features.creation.component

import android.graphics.Paint
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.features.creation.BuiltinPattern
import com.example.kpappercutting.ui.features.creation.CustomPattern
import com.example.kpappercutting.ui.features.creation.PatternCatalog
import com.example.kpappercutting.ui.features.creation.PatternDefaults
import com.example.kpappercutting.ui.features.creation.PatternSource
import com.example.kpappercutting.ui.theme.PaperRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternBottomSheet(
    builtinPatterns: List<BuiltinPattern>,
    customPatterns: List<CustomPattern>,
    onAddPatternClick: () -> Unit,
    onPatternSelected: (PatternSource) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                text = "选择形状镂刻图案",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF252525),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AddPatternCard(onClick = onAddPatternClick)
                }
                items(builtinPatterns, key = { it.id }) { pattern ->
                    PatternSourceCard(
                        title = pattern.displayName,
                        source = pattern,
                        enabled = true,
                        subtitle = "内置图案",
                        onClick = { onPatternSelected(pattern) }
                    )
                }
                items(customPatterns, key = { it.id }) { pattern ->
                    PatternSourceCard(
                        title = pattern.displayName,
                        source = pattern,
                        enabled = pattern.isCuttable,
                        subtitle = if (pattern.isCuttable) "已提取轮廓" else "提取失败",
                        onClick = { onPatternSelected(pattern) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "内置图案和成功提取轮廓的 PNG 图案都可直接放置并确认镂刻。",
                color = Color(0xFF817970),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddPatternCard(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5EFE8)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = PaperRed,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "新增图案",
                color = Color(0xFF45403B),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "导入 PNG",
                color = Color(0xFF8C8378),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PatternSourceCard(
    title: String,
    source: PatternSource,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(140.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) Color(0xFFF8F4EF) else Color(0xFFF3F0EB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color.White, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                PatternPreview(source = source)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = if (enabled) Color(0xFF45403B) else Color(0xFF8F887F),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color(0xFF8C8378),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PatternPreview(source: PatternSource) {
    val previewPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = PatternDefaults.PREVIEW_COLOR
            style = Paint.Style.FILL
        }
    }
    val path = remember(source) { PatternCatalog.unitPathFor(source) }

    Canvas(modifier = Modifier.size(34.dp)) {
        val previewPath = path ?: return@Canvas
        val matrix = android.graphics.Matrix().apply {
            postScale(size.width * 0.9f, size.height * 0.9f)
            postTranslate(size.width / 2f, size.height / 2f)
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawPath(android.graphics.Path(previewPath).apply { transform(matrix) }, previewPaint)
        }
    }
}
