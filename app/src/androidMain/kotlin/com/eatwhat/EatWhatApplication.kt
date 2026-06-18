package com.eatwhat

import android.app.Application
import androidx.room.Room
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.AIProviderEntity
import com.eatwhat.data.preferences.AIPreferences
import com.eatwhat.data.repository.ExportRepository
import com.eatwhat.data.repository.ExportRepositoryImpl
import com.eatwhat.data.repository.RecipeRepository
import com.eatwhat.data.repository.RollRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application class for EatWhat app
 * Initializes database and other app-level dependencies
 */
class EatWhatApplication : Application() {

    lateinit var database: EatWhatDatabase
        private set

    lateinit var recipeRepository: RecipeRepository
        private set

    lateinit var rollRepository: RollRepository
        private set

    lateinit var historyRepository: com.eatwhat.data.repository.HistoryRepository
        private set

  lateinit var aiPreferences: AIPreferences
    private set

  lateinit var exportRepository: ExportRepository
    private set

  // Application scope for background operations
  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Temporary storage for current roll result (for navigation)
    var currentRollResult: com.eatwhat.domain.model.RollResult? = null

    // Temporary storage for current cooking recipes (for navigation)
    var currentCookingRecipes: List<com.eatwhat.data.database.relations.RecipeWithDetails>? = null
    
    // Store the history ID that should be highlighted when returning to history list
    var highlightHistoryId: Long? = null
        set(value) {
            field = value
            android.util.Log.d("EatWhatApplication", "Setting highlightHistoryId to: $value")
        }

    override fun onCreate() {
        super.onCreate()

      // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            EatWhatDatabase::class.java,
            "eatwhat.db"
        )
            .addMigrations(
                EatWhatDatabase.MIGRATION_1_2,
                EatWhatDatabase.MIGRATION_2_3,
                EatWhatDatabase.MIGRATION_3_4,
              EatWhatDatabase.MIGRATION_4_5,
              EatWhatDatabase.MIGRATION_5_6
            )
            .build()

        // Initialize repositories
        recipeRepository = RecipeRepository(database)
        rollRepository = RollRepository(recipeRepository)
        historyRepository = com.eatwhat.data.repository.HistoryRepository(database)
      aiPreferences = AIPreferences(applicationContext)
      exportRepository = ExportRepositoryImpl(applicationContext, database, aiPreferences)

      // Migrate AI Config to Database
      migrateAIConfig()
    }

  private fun migrateAIConfig() {
    applicationScope.launch(Dispatchers.IO) {
      try {
        val aiProviderDao = database.aiProviderDao()
        val existingProviders = aiProviderDao.getAllProviders().first()

        // Only migrate if no providers exist
        if (existingProviders.isEmpty()) {
          val config = aiPreferences.aiConfigFlow.first()
          // Only migrate if we have a valid config (at least API Key)
          if (config.apiKey.isNotBlank()) {
            val provider = AIProviderEntity(
              name = "Migrated Config",
              baseUrl = config.baseUrl,
              apiKey = config.apiKey,
              model = config.model,
              isActive = true,
              createdAt = System.currentTimeMillis(),
              lastModified = System.currentTimeMillis()
            )
            aiProviderDao.insert(provider)
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    }
}
