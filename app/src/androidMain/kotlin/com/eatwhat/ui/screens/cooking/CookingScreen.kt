package com.eatwhat.ui.screens.cooking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.relations.toDomain

@Composable
fun CookingScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val recipes = app.currentCookingRecipes?.map { it.toDomain() }.orEmpty()

    if (recipes.isEmpty()) {
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }

    CookingContent(
        recipes = recipes,
        onNavigateUp = { navController.navigateUp() },
        onFinish = { navController.navigateUp() }
    )
}
