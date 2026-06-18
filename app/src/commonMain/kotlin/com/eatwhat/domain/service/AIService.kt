package com.eatwhat.domain.service

import com.eatwhat.domain.model.AIConfig
import com.eatwhat.domain.model.ConnectionTestResult

/**
 * AI provider operations used by the app UI.
 */
interface AIService {
  suspend fun fetchModels(config: AIConfig): Result<List<String>>

  suspend fun testConnection(config: AIConfig): Result<ConnectionTestResult>
}
