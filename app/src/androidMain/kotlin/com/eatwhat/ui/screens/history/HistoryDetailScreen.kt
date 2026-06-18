package com.eatwhat.ui.screens.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import java.util.Calendar

@Composable
fun HistoryDetailScreen(
    navController: NavController,
    historyId: Long
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository by useCreation { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val history by repository.getHistoryById(historyId)
        .collectAsState(initial = null)

    HistoryDetailContent(
        history = history,
        formatTimestamp = ::formatTimestamp,
        onNavigateUp = { navController.navigateUp() },
        onRecipeClick = { recipeId -> navController.navigate("recipe/$recipeId") },
        onPrepItemCheckedChange = { item, checked ->
            scope.launch {
                repository.updatePrepItemChecked(item.id, checked)
            }
        },
        onSaveCustomName = { customName ->
            scope.launch {
                repository.updateHistoryCustomName(historyId, customName)
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return "${month}月${day}日 ${hour}:${String.format("%02d", minute)}"
}
