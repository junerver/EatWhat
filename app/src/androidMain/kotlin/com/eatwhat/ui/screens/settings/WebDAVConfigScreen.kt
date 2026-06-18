package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.data.sync.SyncWorker
import com.eatwhat.data.sync.WebDAVConfig
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation

@Composable
fun WebDAVConfigScreen(navController: NavController) {
  val context = LocalContext.current
  val app = context.applicationContext as EatWhatApplication
  val exportRepository by useCreation { app.exportRepository }
  val syncRepository by useCreation { SyncRepositoryImpl(context, exportRepository) }
  val existingConfig by useCreation { syncRepository.getConfig() }

  WebDAVConfigContent(
    existingConfig = existingConfig,
    onNavigateUp = { navController.popBackStack() },
    onTestConnection = { config -> syncRepository.testConnection(config) },
    onSaveConfig = { config ->
      saveConfig(
        config = config,
        syncRepository = syncRepository,
        onAutoSyncEnabled = { intervalMinutes ->
          SyncWorker.schedule(context, intervalMinutes)
        },
        onAutoSyncDisabled = {
          SyncWorker.cancel(context)
        }
      )
      Toast.makeText(context, "配置已保存", Toast.LENGTH_SHORT).show()
      navController.popBackStack()
    },
    onClearConfig = {
      syncRepository.clearConfig()
      SyncWorker.cancel(context)
      Toast.makeText(context, "配置已清除", Toast.LENGTH_SHORT).show()
      navController.popBackStack()
    }
  )
}

private fun saveConfig(
  config: WebDAVConfig,
  syncRepository: SyncRepositoryImpl,
  onAutoSyncEnabled: (Int) -> Unit,
  onAutoSyncDisabled: () -> Unit
) {
  syncRepository.saveConfig(config)
  if (config.autoSyncEnabled) {
    onAutoSyncEnabled(config.syncIntervalMinutes)
  } else {
    onAutoSyncDisabled()
  }
}
