package com.eatwhat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.eatwhat.navigation.EatWhatApp
import com.eatwhat.ui.theme.EatWhatTheme

/**
 * Main activity for EatWhat app
 * Entry point for the Compose UI
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EatWhatTheme {
                EatWhatApp()
            }
        }
    }
}
