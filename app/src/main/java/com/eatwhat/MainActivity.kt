package com.eatwhat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.eatwhat.data.preferences.ThemeMode
import com.eatwhat.data.preferences.ThemePreferences
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

      val themePreferences = ThemePreferences(this)

        setContent {
          val themeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

          EatWhatTheme(themeMode = themeMode) {
                EatWhatApp()
            }
        }
    }
}
