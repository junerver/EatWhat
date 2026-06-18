package com.eatwhat.ui.screens.settings

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.repository.SyncRepositoryImpl
import com.eatwhat.navigation.Destinations
import xyz.junerver.compose.hooks.getValue
import xyz.junerver.compose.hooks.useCreation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncScreen(navController: NavController) {
  val context = LocalContext.current
  val app = context.applicationContext as EatWhatApplication
  val exportRepository by useCreation { app.exportRepository }
  val syncRepository by useCreation { SyncRepositoryImpl(context, exportRepository) }
  val config by useCreation { syncRepository.getConfig() }

  SyncContent(
    config = config,
    formatTimestamp = ::formatTime,
    onNavigateUp = { navController.popBackStack() },
    onConfigureClick = { navController.navigate(Destinations.WebDAVConfig.route) },
    onLoadCloudMetadata = { syncRepository.getCloudMetadata() },
    onUploadToCloud = { password -> syncRepository.uploadToCloud(password) },
    onDownloadFromCloud = { password -> syncRepository.downloadFromCloud(password) },
    onSyncComplete = { _, message ->
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  )
}

private fun formatTime(timestamp: Long): String {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
  return dateFormat.format(Date(timestamp))
}
