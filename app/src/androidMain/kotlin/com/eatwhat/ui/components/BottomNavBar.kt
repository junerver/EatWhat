package com.eatwhat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eatwhat.R
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.PrimaryOrange
import xyz.junerver.compose.palette.components.bottomnavigation.BottomNavigationDefaults
import xyz.junerver.compose.palette.components.bottomnavigation.BottomNavigationItem
import xyz.junerver.compose.palette.components.bottomnavigation.PBottomNavigation

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
      icon = Icons.Default.Casino,
      label = stringResource(R.string.nav_roll)
    ),
    BottomNavItem(
      route = Destinations.RecipeList.route,
      navigateRoute = Destinations.RecipeList.route,
      icon = Icons.AutoMirrored.Filled.MenuBook,
      label = stringResource(R.string.nav_recipes)
    ),
    BottomNavItem(
      route = Destinations.History.route,
      navigateRoute = Destinations.History.routeWithoutArgs,
      icon = Icons.Default.History,
      label = stringResource(R.string.nav_history)
    )
  )

  val selectedKey = items
    .firstOrNull { item -> currentRoute?.startsWith(item.route.substringBefore("?")) == true }
    ?.route

  val colors = BottomNavigationDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surface,
    selectedContentColor = PrimaryOrange,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Icon(
              imageVector = item.icon,
              contentDescription = null,
              modifier = Modifier.size(22.dp),
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
  val icon: ImageVector,
  val label: String
)
