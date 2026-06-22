package com.eatwhat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eatwhat.R
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.PrimaryOrange
import xyz.junerver.compose.palette.components.bottomnavigation.BottomNavigationDefaults
import xyz.junerver.compose.palette.components.bottomnavigation.BottomNavigationItem
import xyz.junerver.compose.palette.components.bottomnavigation.PBottomNavigation
import xyz.junerver.compose.palette.components.text.PText

/**
 * Bottom navigation bar with 3 tabs
 * Roll点, 菜谱, 历史
 */
@Composable
fun BottomNavBar(navController: NavController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val items = listOf(
    BottomNavItem(
      route = Destinations.Roll.route,
      navigateRoute = Destinations.Roll.route,
      emoji = "🎲",
      label = stringResource(R.string.nav_roll)
    ),
    BottomNavItem(
      route = Destinations.RecipeList.route,
      navigateRoute = Destinations.RecipeList.route,
      emoji = "📖",
      label = stringResource(R.string.nav_recipes)
    ),
    BottomNavItem(
      route = Destinations.History.route,
      navigateRoute = Destinations.History.routeWithoutArgs,
      emoji = "📜",
      label = stringResource(R.string.nav_history)
    )
  )

  val selectedKey = items
    .firstOrNull { item -> currentRoute?.startsWith(item.route.substringBefore("?")) == true }
    ?.route

  val colors = BottomNavigationDefaults.colors(
    selectedContentColor = PrimaryOrange,
    contentColor = Color.Gray,
    selectedIndicatorColor = PrimaryOrange.copy(alpha = 0.12f)
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(colors.containerColor)
      .navigationBarsPadding()
  ) {
    PBottomNavigation(
      items = items.map { item ->
        BottomNavigationItem(
          key = item.route,
          label = item.label,
          icon = {
            PText(
              text = item.emoji,
              fontSize = 24.sp,
            )
          },
        )
      },
      selectedKey = selectedKey,
      colors = colors,
      onItemClick = { selectedRoute ->
        val item = items.firstOrNull { it.route == selectedRoute } ?: return@PBottomNavigation
        val isSelected = currentRoute?.startsWith(item.route.substringBefore("?")) == true
        if (!isSelected) {
          navController.navigate(item.navigateRoute) {
            // Pop up to the start destination to avoid building up a large stack
            popUpTo(Destinations.Roll.route) {
              saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
          }
        }
      }
    )
  }
}

private data class BottomNavItem(
  val route: String,
  val navigateRoute: String,
  val emoji: String,
  val label: String
)
