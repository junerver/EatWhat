package com.eatwhat.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.eatwhat.EatWhatApplication
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.repository.SyncRepositoryImpl
import java.util.concurrent.TimeUnit

/**
 * 后台同步 Worker
 * 使用 WorkManager 实现定时自动同步
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
        const val UNIQUE_WORK_NAME = "eatwhat_auto_sync"

        /**
         * 调度自动同步任务
         */
        fun schedule(context: Context, intervalMinutes: Int) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes.toLong(),
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    syncRequest
                )

            Log.d(TAG, "Scheduled auto sync every $intervalMinutes minutes")
        }

        /**
         * 取消自动同步任务
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
            Log.d(TAG, "Cancelled auto sync")
        }

        /**
         * 立即触发一次同步
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueue(syncRequest)

            Log.d(TAG, "Triggered immediate sync")
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting auto sync...")

        return try {
            val database = EatWhatDatabase.getInstance(applicationContext)
          val app = applicationContext as EatWhatApplication
          val exportRepository = app.exportRepository
            val syncRepository = SyncRepositoryImpl(applicationContext, exportRepository)

            val config = syncRepository.getConfig()
            if (config == null || !config.autoSyncEnabled) {
                Log.d(TAG, "Auto sync is disabled or not configured")
                return Result.success()
            }

            // 执行智能合并同步
            val result = performSmartSync(syncRepository, config)

            when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Auto sync completed successfully")
                    Result.success()
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "Auto sync failed: ${result.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Auto sync error", e)
            Result.retry()
        }
    }

    /**
     * 执行智能合并同步
     * 1. 下载云端数据（如果有）
     * 2. 根据时间戳合并本地与云端数据
     * 3. 上传合并后的数据
     */
    private suspend fun performSmartSync(
        syncRepository: SyncRepositoryImpl,
        config: WebDAVConfig
    ): SyncResult {
        val encryptionPassword = if (config.encryptionEnabled) {
            config.encryptionPassword
        } else {
            null
        }

        // 检查云端是否有数据
        val cloudMetadata = syncRepository.getCloudMetadata()

        if (cloudMetadata != null) {
            // 云端有数据，先下载合并
            val downloadResult = syncRepository.downloadFromCloud(encryptionPassword)
            if (downloadResult is SyncResult.Error) {
                // 如果下载失败，但不是因为没有数据，则返回错误
                if (!downloadResult.message.contains("没有备份")) {
                    return downloadResult
                }
            }
        }

        // 上传本地最新数据
        return syncRepository.uploadToCloud(encryptionPassword)
    }
}
