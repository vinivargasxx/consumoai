package com.example.consumoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.consumoai.presentation.home.HomeRoute
import com.example.consumoai.ui.theme.ConsumoAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConsumoAITheme {
                HomeRoute()
            }
        }
    }
}
