package com.example.kpappercutting.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PaperRed,             //主色，App 中出现频率最高的颜色。用于最核心的组件，如高亮按钮（Button）、选中的状态、或者悬浮操作按钮（FAB）。
    secondary = TagGray,       //次用色，用于不那么突出的 UI 元素，比如过滤标签（Filter Chips）、较小的控件或辅助性装饰。
    tertiary = SearchBarBg,              //强调色/三级色,用作对比或平衡。常用于输入框的强调、某些特定的通知提醒，或者为了打破主色调单调感的地方。
    background = BackgroundCream, //整个屏幕最底层的颜色。像是一张大画布，所有的卡片和文字都放在它上面。
    surface = BackgroundCream,    //卡片、菜单、底部导航栏等组件的背景色。它们通常会有阴影或边框来区分于背景色。
    onPrimary = Color.White,        //当 primary 颜色被用作背景色时，文本或图标的颜色。确保在 primary 上的内容有足够的对比度。
    onBackground = TagGray,//直接写在 background（大背景）上的文字或图标颜色。
    onSurface = TagGray   //直接写在 surface（卡片背景）上的文字或图标颜色。
)

private val LightColorScheme = lightColorScheme(
    primary = PaperRed,             //主色，App 中出现频率最高的颜色。用于最核心的组件，如高亮按钮（Button）、选中的状态、或者悬浮操作按钮（FAB）。
    secondary = TagGray,       //次用色，用于不那么突出的 UI 元素，比如过滤标签（Filter Chips）、较小的控件或辅助性装饰。
    tertiary = SearchBarBg,              //强调色/三级色,用作对比或平衡。常用于输入框的强调、某些特定的通知提醒，或者为了打破主色调单调感的地方。
    background = BackgroundCream, //整个屏幕最底层的颜色。像是一张大画布，所有的卡片和文字都放在它上面。
    surface = BackgroundCream,    //卡片、菜单、底部导航栏等组件的背景色。它们通常会有阴影或边框来区分于背景色。
    onPrimary = Color.White,        //当 primary 颜色被用作背景色时，文本或图标的颜色。确保在 primary 上的内容有足够的对比度。
    onBackground = TagGray,//直接写在 background（大背景）上的文字或图标颜色。
    onSurface = TagGray   //直接写在 surface（卡片背景）上的文字或图标颜色。
)

@Composable
fun KPappercuttingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
