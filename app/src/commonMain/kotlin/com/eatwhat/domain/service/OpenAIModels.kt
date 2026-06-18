package com.eatwhat.domain.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class OpenAIMessage(
  val role: String,
  val content: JsonElement
)

fun createTextMessage(role: String, text: String): OpenAIMessage {
  return OpenAIMessage(role, JsonPrimitive(text))
}

@Serializable
data class OpenAIRequest(
  val model: String,
  val messages: List<OpenAIMessage>,
  val temperature: Double = 0.7,
  val response_format: ResponseFormat? = null
)

@Serializable
data class ResponseFormat(val type: String)

@Serializable
data class OpenAIResponse(
  val choices: List<Choice>,
  val usage: Usage? = null
)

@Serializable
data class Usage(
  val total_tokens: Int? = null
)

@Serializable
data class ModelListResponse(
  val data: List<ModelData>
)

@Serializable
data class ModelData(
  val id: String
)

@Serializable
data class Choice(
  val message: OpenAIMessage
)
