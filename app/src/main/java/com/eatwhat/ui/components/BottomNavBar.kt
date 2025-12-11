package com.eatwhat.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
            emoji = "🎲",
            label = stringResource(R.string.nav_roll)
        ),
        BottomNavItem(
            route = Destinations.RecipeList.route,
            emoji = "📖",
            label = stringResource(R.string.nav_recipes)
        ),
        BottomNavItem(
            route = Destinations.History.route,
            emoji = "📜",
            label = stringResource(R.string.nav_history)
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Text(
                        text = item.emoji,
                        fontSize = 24.sp
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp
                    )
                },
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
    val emoji: String,
    val label: String
)
