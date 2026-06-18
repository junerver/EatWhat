package com.eatwhat.domain.model

/**
 * AI provider connection configuration.
 */
data class AIConfig(
  val baseUrl: String = "https://api.openai.com/v1",
  val apiKey: String = "",
  val model: String = "gpt-3.5-turbo"
)
