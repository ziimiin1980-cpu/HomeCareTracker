package com.example.homecaretracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.homecaretracker.navigation.HomeCareApp
import com.example.homecaretracker.ui.theme.HomeCareTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeCareTrackerTheme {
                HomeCareApp()
            }
        }
    }
}