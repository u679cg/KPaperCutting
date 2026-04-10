package com.example.kpappercutting.ui.features.creation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.R
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.creation.component.FoldTechniqueWheelSelector
import com.example.kpappercutting.ui.features.creation.component.PaperCanvas
import com.example.kpappercutting.ui.features.creation.engine.PaperCutEngine
import com.example.kpappercutting.ui.theme.PaperRed
import kotlin.math.roundToInt

@Composable
fun CreateScreen(
    uiState: CreateUiState,
    engine: PaperCutEngine,
    onAction: (CreateUiAction) -> Unit,
    onMenuAction: (CreationMenuAction) -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isColorPaletteVisible by rememberSaveable { mutableStateOf(false) }
    var isEraserSliderVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F2))
            .padding(top = 12.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CreateHeader(
            isTraditionalSelected = uiState.selectedShape == PaperShape.SQUARE,
            onTraditionalClick = { onAction(CreateUiAction.SelectShape(PaperShape.SQUARE)) },
            onFreeClick = { onAction(CreateUiAction.SelectShape(PaperShape.CIRCLE)) },
            onBack = onBack,
            onShareClick = { onMenuAction(CreationMenuAction.EXPORT_TO_GALLERY) }
        )
        TopToolbar(
            activeTool = uiState.selectedTool,
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            onUndo = { onAction(CreateUiAction.Undo) },
            onRedo = { onAction(CreateUiAction.Redo) },
            onSelectTool = {
                isEraserSliderVisible = false
                onAction(CreateUiAction.SelectTool(it))
            },
            onEraserToolClick = {
                if (uiState.selectedTool == EditTool.ERASER) {
                    isEraserSliderVisible = !isEraserSliderVisible
                } else {
                    isEraserSliderVisible = false
                    onAction(CreateUiAction.SelectTool(EditTool.ERASER))
                }
            },
            isColorControlSelected = isColorPaletteVisible,
            onColorControlClick = { isColorPaletteVisible = !isColorPaletteVisible }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            PaperCanvas(
                uiState = uiState,
                engine = engine,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp),
                onAction = onAction
            )

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 14.dp, start = 12.dp),
                visible = uiState.selectedTool == EditTool.ERASER && isEraserSliderVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 })
            ) {
                EraserSizeSliderCard(
                    selectedSize = uiState.selectedEraserSize,
                    onSelectSize = { onAction(CreateUiAction.SelectEraserSize(it)) }
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 12.dp),
                visible = isColorPaletteVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 })
            ) {
                ColorPaperStrip(
                    selectedColor = uiState.selectedPaperColor,
                    onSelectColor = { color ->
                        onAction(CreateUiAction.SelectPaperColor(color))
                    }
                )
            }

            BottomActionBar(
                canToggleFold = uiState.foldMode != FoldMode.NONE,
                isFolded = uiState.isFolded,
                currentFoldMode = uiState.foldMode,
                availableFoldModes = uiState.availableFoldModes,
                onClear = { onAction(CreateUiAction.ClearCanvas) },
                onSelectFoldMode = { onAction(CreateUiAction.SelectFoldMode(it)) },
                onExpand = { onAction(CreateUiAction.ToggleFold) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CreateHeader(
    isTraditionalSelected: Boolean,
    onTraditionalClick: () -> Unit,
    onFreeClick: () -> Unit,
    onBack: () -> Unit,
    onShareClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        SimpleIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack
        ) {
            Image(
                painter = painterResource(R.drawable.ic_home),
                contentDescription = "Home",
                modifier = Modifier.size(22.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(170.dp)
                .height(36.dp),
            shape = RoundedCornerShape(0.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeTab(
                    text = "传统",
                    selected = isTraditionalSelected,
                    modifier = Modifier.weight(1f),
                    onClick = onTraditionalClick
                )
                ModeTab(
                    text = "自由",
                    selected = !isTraditionalSelected,
                    modifier = Modifier.weight(1f),
                    onClick = onFreeClick
                )
            }
        }

        SimpleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onShareClick
        ) {
            Text(
                text = "↗",
                color = Color(0xFFD8D8D8),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ModeTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp,8.dp,0.dp,0.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .shadow(
                if (selected) 0.2.dp else 0.dp,
                RoundedCornerShape(8.dp,8.dp,0.dp,0.dp),
                clip = false),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF1F1F1F) else Color(0xFF4E4E4E),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SimpleIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun BottomActionBar(
    canToggleFold: Boolean,
    isFolded: Boolean,
    currentFoldMode: FoldMode,
    availableFoldModes: List<FoldMode>,
    onClear: () -> Unit,
    onSelectFoldMode: (FoldMode) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val compactLayout = remember(configuration.screenWidthDp) {
        configuration.screenWidthDp.dp - 32.dp < 360.dp
    }
    val sideButtonSize = if (compactLayout) 52.dp else 56.dp
    val selectorWidth = if (compactLayout) 200.dp else 240.dp
    val selectorHeight = if (compactLayout) 56.dp else 60.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleActionButton(
                text = "清空",
                enabled = true,
                size = sideButtonSize,
                onClick = onClear
            )
            Spacer(modifier = Modifier.width(12.dp))
            FoldTechniqueWheelSelector(
                modifier = Modifier
                    .width(selectorWidth)
                    .height(selectorHeight),
                compact = compactLayout,
                currentMode = currentFoldMode,
                availableModes = availableFoldModes,
                onModeSelected = onSelectFoldMode
            )
            Spacer(modifier = Modifier.width(12.dp))
            CircleActionButton(
                text = if (isFolded) "展开" else "折叠",
                enabled = canToggleFold,
                size = sideButtonSize,
                onClick = onExpand
            )
        }
    }
}

@Composable
private fun CircleActionButton(
    text: String,
    enabled: Boolean,
    size: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .shadow(4.dp, CircleShape, clip = false)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (enabled) Color.White else Color(0xFFF1ECE6)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (enabled) Color(0xFF6C6C6C) else Color(0xFFBBB1A7),
                fontSize = if (size < 56.dp) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TopToolbar(
    activeTool: EditTool,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectTool: (EditTool) -> Unit,
    onEraserToolClick: () -> Unit,
    isColorControlSelected: Boolean,
    onColorControlClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(horizontal = 2.dp)
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false   // 
            ),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarIcon(
                drawableRes = R.drawable.ic_undo,
                enabled = canUndo,
                selected = false,
                onClick = onUndo
            )
            Spacer(modifier = Modifier.weight(0.5f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_redo,
                enabled = canRedo,
                selected = false,
                onClick = onRedo
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_scissors,
                enabled = true,
                selected = activeTool == EditTool.SCISSORS,
                onClick = { onSelectTool(EditTool.SCISSORS) }
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_pencil,
                enabled = true,
                selected = activeTool == EditTool.PENCIL,
                onClick = { onSelectTool(EditTool.PENCIL) }
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_eraser,
                enabled = true,
                selected = activeTool == EditTool.ERASER,
                onClick = onEraserToolClick
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_pattern,
                enabled = true,
                selected = false,
                onClick = {}
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_ai,
                enabled = true,
                selected = false,
                onClick = {}
            )
            Spacer(modifier = Modifier.weight(1f))
            ToolbarIcon(
                drawableRes = R.drawable.ic_color_control,
                enabled = true,
                selected = isColorControlSelected,
                onClick = onColorControlClick
            )
        }
    }
}

@Composable
private fun ColorPaperStrip(
    selectedColor: Int,
    onSelectColor: (Int) -> Unit
) {
    val paperColors = listOf(
        0xFFB02621.toInt(),
        0xFFC74B2A.toInt(),
        0xFFD8892B.toInt(),
        0xFF3F8C54.toInt(),
        0xFF4A6FA8.toInt(),
        0xFF6E4AA8.toInt(),
        0xFF2E2E2E.toInt()
    )

    Surface(
        modifier = Modifier.shadow(10.dp, RoundedCornerShape(24.dp), clip = false),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            paperColors.forEachIndexed { index, colorInt ->
                ColorPaperChip(
                    color = Color(colorInt),
                    selected = selectedColor == colorInt,
                    onClick = { onSelectColor(colorInt) }
                )
                if (index != paperColors.lastIndex) {
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EraserSizeSliderCard(
    selectedSize: EraserSize,
    onSelectSize: (EraserSize) -> Unit
) {
    val sliderValue = selectedSize.ordinal.toFloat()

    Surface(
        modifier = Modifier
            .width(214.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp), clip = false),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "橡皮大小",
                color = Color(0xFF6A6A6A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Slider(
                value = sliderValue,
                onValueChange = { raw ->
                    val index = raw.roundToInt().coerceIn(0, EraserSize.entries.lastIndex)
                    onSelectSize(EraserSize.entries[index])
                },
                modifier = Modifier.width(180.dp),
                valueRange = 0f..EraserSize.entries.lastIndex.toFloat(),
                steps = EraserSize.entries.size - 2,
                colors = SliderDefaults.colors(
                    thumbColor = PaperRed,
                    activeTrackColor = PaperRed,
                    inactiveTrackColor = Color(0xFFE7E1DA)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                EraserSize.entries.forEach { size ->
                    Text(
                        text = size.label,
                        modifier = Modifier.weight(1f),
                        color = if (size == selectedSize) PaperRed else Color(0xFF9A9A9A),
                        fontSize = 12.sp,
                        fontWeight = if (size == selectedSize) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPaperChip(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val lift by animateDpAsState(
        targetValue = if (selected) (-10).dp else 0.dp,
        animationSpec = spring(stiffness = 420f),
        label = "paper_chip_lift"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(stiffness = 380f),
        label = "paper_chip_scale"
    )

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(72.dp)
            .graphicsLayer {
                translationY = lift.toPx()
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 8.dp else 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false,
                ambientColor = Color(0x12000000),
                spotColor = Color(0x16000000)
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.98f),
                        color.copy(alpha = 0.94f)
                    )
                )
            )
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    val xStep = size.width / 5f
                    val yStep = size.height / 7f
                    for (x in 0..5) {
                        for (y in 0..7) {
                            val offsetX = x * xStep + ((y % 2) * xStep * 0.22f)
                            val offsetY = y * yStep
                            val alpha = if ((x + y) % 3 == 0) 0.06f else 0.035f
                            drawCircle(
                                color = Color.White.copy(alpha = alpha),
                                radius = 1.1.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(offsetX, offsetY)
                            )
                        }
                    }
                    drawRect(Color.Black.copy(alpha = 0.04f))
                }
            }
            .clickable(onClick = onClick)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .width(22.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.72f))
            )
        }
    }
}

@Composable
private fun ToolbarIcon(
    drawableRes: Int,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = when {
        selected -> PaperRed
        enabled -> Color(0xFF7D7D7D)
        else -> Color(0xFFD2D2D2)
    }

    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0xFFFCF0E1) else Color.Transparent)
//            .border(
//                width = if (selected) 1.dp else 0.dp,
//                color = if (selected) Color(0xFFE5CBB0) else Color.Transparent,
//                shape = CircleShape
//            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(drawableRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1080px,height=2340px,dpi=440")
@Composable
fun CreateScreenPreview() {
    MaterialTheme {
        CreateScreen(
            uiState = CreateUiState(
                selectedShape = PaperShape.SQUARE,
                selectedTool = EditTool.SCISSORS,
                canUndo = true
            ),
            engine = PaperCutEngine(),
            onAction = {},
            onMenuAction = {}
        )
    }
}
