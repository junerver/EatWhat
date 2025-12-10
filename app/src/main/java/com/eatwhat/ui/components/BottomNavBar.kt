package com.eatwhat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.eatwhat.R
import com.eatwhat.navigation.Destinations

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
            icon = Icons.Filled.Shuffle,
            label = stringResource(R.string.nav_roll)
        ),
        BottomNavItem(
            route = Destinations.RecipeList.route,
            icon = Icons.Filled.Restaurant,
            label = stringResource(R.string.nav_recipes)
        ),
        BottomNavItem(
            route = Destinations.History.route,
            icon = Icons.Filled.History,
            label = stringResource(R.string.nav_history)
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
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
}

private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)
