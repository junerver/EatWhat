package com.eatwhat.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * AI Configuration
 */
data class AIConfig(
  val baseUrl: String = "https://api.openai.com/v1",
  val apiKey: String = "",
  val model: String = "gpt-3.5-turbo"
)

/**
 * AI Preferences Management
 */
class AIPreferences(private val context: Context) {
  companion object {
    private val BASE_URL_KEY = stringPreferencesKey("ai_base_url")
    private val API_KEY_KEY = stringPreferencesKey("ai_api_key")
    private val MODEL_KEY = stringPreferencesKey("ai_model")
  }

  val aiConfigFlow: Flow<AIConfig> = context.dataStore.data
    .map { preferences ->
      AIConfig(
        baseUrl = preferences[BASE_URL_KEY] ?: "https://api.openai.com/v1",
        apiKey = preferences[API_KEY_KEY] ?: "",
        model = preferences[MODEL_KEY] ?: "gpt-3.5-turbo"
      )
    }

  suspend fun saveConfig(config: AIConfig) {
    context.dataStore.edit { preferences ->
      preferences[BASE_URL_KEY] = config.baseUrl
      preferences[API_KEY_KEY] = config.apiKey
      preferences[MODEL_KEY] = config.model
    }
  }
}
