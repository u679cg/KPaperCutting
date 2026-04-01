package com.example.kpappercutting.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kpappercutting.ui.Screen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import com.example.kpappercutting.R
import com.example.kpappercutting.ui.theme.CreamYellow
import com.example.kpappercutting.ui.theme.KPappercuttingTheme


@Composable
fun SteeringBottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    // visual dimensions
    val barHeight: Dp = 58.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // 圆角背景板
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
//            tonalElevation = 0.dp

        ) {
            // Empty container for the bar background.
        }

        // Row of 4 side items with a spacer for the center helm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallNavItem(
                label = "首页",
                iconResId= R.drawable.ic_home,
                selected = current == Screen.Home,
                onClick = { onSelect(Screen.Home) })
            SmallNavItem(
                label = "文化",
                iconResId = R.drawable.ic_culture,
                selected = current == Screen.Culture,
                onClick = { onSelect(Screen.Culture) })

            // placeholder space for center circular button
            Spacer(modifier = Modifier.width(80.dp))

            SmallNavItem(
                label = "社区",
                iconResId = R.drawable.ic_community,
                selected = current == Screen.Community, onClick = { onSelect(Screen.Community) })
            SmallNavItem(
                label = "我的",
                iconResId = R.drawable.ic_profile,
                selected = current == Screen.Profile,
                onClick = { onSelect(Screen.Profile) })
        }
    }
}

@Composable
fun CreateNavButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(62.dp)
            .border(4.dp,CreamYellow,CircleShape)
            .shadow(8.dp, CircleShape)
            .background(color = colorResource(id = R.color.primary), shape = CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_create),
            contentDescription = "Create",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SmallNavItem(
    label: String,
    iconResId: Int, //接收图标资源ID
    selected: Boolean,
    onClick: () -> Unit
) {
    // 选中时的颜色与未选中时的颜色
    val itemColor = if (selected) colorResource(id = R.color.primary) else Color.Gray
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id =iconResId),
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier.size(24.dp)//控制图标大小

        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = itemColor
        )
    }
}

// Preview helpers - 便于在 Android Studio 中使用 Compose Preview
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SteeringBottomBarPreviewLight() {
    // 使用原项目的 MaterialTheme
    KPappercuttingTheme(darkTheme = false) {
        SteeringBottomBar(current = Screen.Home, onSelect = {})
    }
}

@Preview(showBackground = true)
@Composable
fun SteeringBottomBarPreviewDark() {
    KPappercuttingTheme() {
        SteeringBottomBar(current = Screen.Create, onSelect = {})
    }
}

