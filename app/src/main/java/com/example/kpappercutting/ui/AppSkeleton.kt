package com.example.kpappercutting.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.ui.unit.dp
import com.example.kpappercutting.ui.navigation.CreateNavButton
import com.example.kpappercutting.ui.navigation.SteeringBottomBar
import com.example.kpappercutting.ui.features.community.CommunityScreen
import com.example.kpappercutting.ui.features.creation.CreateScreen
import com.example.kpappercutting.ui.features.culture.CultureScreen
import com.example.kpappercutting.ui.features.home.HomeScreen
import com.example.kpappercutting.ui.features.profile.ProfileScreen

enum class Screen {
    Home, Culture, Create, Community, Profile
}

@Composable
fun AppSkeleton() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var previousScreen by remember { mutableStateOf(Screen.Home) }
    val showBottomBar = currentScreen != Screen.Create

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Create && currentScreen != Screen.Create) {
            previousScreen = currentScreen
        }
        currentScreen = screen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.surface)
                .align(Alignment.TopCenter)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val enterTransition = if (targetState == Screen.Create) {
                        slideInVertically(initialOffsetY = { it / 8 }) + fadeIn()
                    } else {
                        fadeIn()
                    }
                    val exitTransition = if (initialState == Screen.Create) {
                        slideOutVertically(targetOffsetY = { it / 8 }) + fadeOut()
                    } else {
                        fadeOut()
                    }
                    enterTransition togetherWith exitTransition
                },
                label = "screen_transition"
            ) { screen ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(bottom = if (screen == Screen.Create) 0.dp else 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (screen) {
                        Screen.Home -> HomeScreen()
                        Screen.Culture -> CultureScreen()
                        Screen.Create -> CreateScreen(
                            onBack = { currentScreen = previousScreen }
                        )
                        Screen.Community -> CommunityScreen()
                        Screen.Profile -> ProfileScreen()
                    }
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = showBottomBar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            SteeringBottomBar(currentScreen, onSelect = ::navigateTo)
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-44).dp),
            visible = showBottomBar,
            enter = fadeIn() + scaleIn(initialScale = 0.85f) + slideInVertically(initialOffsetY = { it / 3 }),
            exit = fadeOut() + scaleOut(targetScale = 0.85f) + slideOutVertically(targetOffsetY = { it / 3 })
        ) {
            CreateNavButton(onClick = { navigateTo(Screen.Create) })
        }
    }
}

