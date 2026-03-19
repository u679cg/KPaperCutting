package com.example.kpappercutting.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kpappercutting.ui.navigation.SteeringBottomBar
import com.example.kpappercutting.ui.features.community.CommunityScreen
import com.example.kpappercutting.ui.features.creation.CreateScreen
import com.example.kpappercutting.ui.features.culture.CultureScreen
import com.example.kpappercutting.ui.features.home.HomeScreen
import com.example.kpappercutting.ui.features.profile.ProfileScreen

enum class Screen {
    Home, Culture, Create, Community, Profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSkeleton() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { SteeringBottomBar(currentScreen, onSelect = { currentScreen = it }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen()
                Screen.Culture -> CultureScreen()
                Screen.Create -> CreateScreen()
                Screen.Community -> CommunityScreen()
                Screen.Profile -> ProfileScreen()
            }
        }
    }
}

