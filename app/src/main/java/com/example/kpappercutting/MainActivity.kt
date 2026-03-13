package com.example.kpappercutting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kpappercutting.ui.AppSkeleton
import com.example.kpappercutting.ui.theme.KPappercuttingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KPappercuttingTheme {
                AppSkeleton()
            }
        }
    }
}
