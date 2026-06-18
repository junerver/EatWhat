package com.eatwhat.ui.screens.history

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.navigation.Destinations
import com.eatwhat.ui.theme.LocalDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useState
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryListScreen(
    navController: NavController,
    highlightId: Long? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as EatWhatApplication
    val repository by useCreation { HistoryRepository(app.database) }
    val scope = rememberCoroutineScope()

    val historyList by repository.getAllHistory().collectAsState(initial = emptyList())
    var currentHighlightId by _useState<Long?>(null)

    val view = LocalView.current
    val darkTheme = LocalDarkTheme.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    LaunchedEffect(highlightId) {
        val requestedHighlightId = highlightId ?: app.highlightHistoryId
        Log.d("HistoryListScreen", "=== HistoryListScreen Loaded ===")
        Log.d("HistoryListScreen", "Requested highlightId: $requestedHighlightId")

        if (requestedHighlightId != null && requestedHighlightId > 0) {
            Log.d("HistoryListScreen", "Valid highlightId, starting animation for: $requestedHighlightId")
            currentHighlightId = requestedHighlightId

            delay(2000)
            Log.d("HistoryListScreen", "Clearing highlight after 2 seconds")
            currentHighlightId = null
            if (app.highlightHistoryId == requestedHighlightId) {
                app.highlightHistoryId = null
            }
        } else {
            Log.d("HistoryListScreen", "No valid highlightId, skipping animation")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HistoryListContent(
            historyList = historyList,
            highlightedHistoryId = currentHighlightId,
            formatTimestamp = ::formatTimestamp,
            onSettingsClick = { navController.navigate(Destinations.Settings.route) },
            onHistoryClick = { history -> navController.navigate("history/${history.id}") },
            onLockToggle = { history, locked ->
                scope.launch {
                    repository.toggleHistoryLocked(history.id, locked)
                }
            },
            onDeleteHistory = { history ->
                scope.launch {
                    repository.deleteHistory(history.id)
                }
            },
            onDeleteAllUnlocked = {
                scope.launch {
                    repository.deleteAllUnlockedHistory()
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("M月d日 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
