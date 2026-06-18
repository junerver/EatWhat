package com.eatwhat.ui.screens.prep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.domain.usecase.GeneratePrepListUseCase
import com.eatwhat.domain.usecase.PrepListItem
import com.eatwhat.domain.usecase.SaveHistoryUseCase
import com.eatwhat.navigation.Destinations
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation

@Composable
fun PrepScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val rollResult = app.currentRollResult
    val scope = rememberCoroutineScope()

    // If no roll result, navigate back
    if (rollResult == null) {
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }

  val useCase by useCreation { GeneratePrepListUseCase() }
  val initialPrepList by useCreation { useCase(rollResult.recipes) }

    // Save history when entering PrepScreen and store the historyId
  val saveHistoryUseCase by useCreation { SaveHistoryUseCase(app.historyRepository) }
  val historyRepository by useCreation { app.historyRepository }
  var historyId by _useState<Long?>(null)
    
    LaunchedEffect(Unit) {
        try {
            val id = saveHistoryUseCase(rollResult, initialPrepList)
            historyId = id
        } catch (e: Exception) {
            // Handle error if needed
        }
    }
    
    // Read prep items from database - only when historyId is available
    val prepItemsFromDb by historyRepository.getPrepItemsByHistoryId(historyId ?: 0L)
        .collectAsState(initial = emptyList())

    // Convert database entities to PrepListItem for display
    val prepList = remember(prepItemsFromDb) {
        prepItemsFromDb.map { entity ->
            PrepListItem(
                name = entity.ingredientName,
                amount = "",
                unit = "",
                isChecked = entity.isChecked
            )
        }
    }

    PrepContent(
        prepList = prepList,
        isStartDisabled = historyId == null,
        onNavigateUp = { navController.navigateUp() },
        onCheckedChange = { index, _, checked ->
            if (prepItemsFromDb.isNotEmpty() && index < prepItemsFromDb.size) {
                scope.launch {
                    historyRepository.updatePrepItemChecked(prepItemsFromDb[index].id, checked)
                }
            }
        },
        onStartCooking = {
            historyId?.let { id ->
                app.highlightHistoryId = id

                navController.navigate(Destinations.History.routeWithoutArgs) {
                    popUpTo(0) {
                        inclusive = true
                    }
                }
                navController.navigate(Destinations.HistoryDetail.createRoute(id))
            }
        }
    )
}
