package com.eatwhat.ui.screens.roll

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.LocalDarkTheme
import com.eatwhat.ui.theme.PrimaryOrange

@Composable
fun RollScreen(navController: NavController) {
  val isDarkTheme = LocalDarkTheme.current
  val view = LocalView.current

  SideEffect {
    val window = (view.context as Activity).window
    window.statusBarColor = if (isDarkTheme) {
      android.graphics.Color.TRANSPARENT
    } else {
      PrimaryOrange.toArgb()
    }
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
  }

  RollPlannerContent(
    onRoll = { config ->
      navController.navigate(
        Destinations.RollResult.createRoute(
          meatCount = config.meatCount,
          vegCount = config.vegCount,
          soupCount = config.soupCount,
          stapleCount = config.stapleCount,
          randomCount = config.randomCount
        )
      )
    }
  )
}
