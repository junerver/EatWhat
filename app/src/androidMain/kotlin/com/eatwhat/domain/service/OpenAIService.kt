package com.eatwhat.domain.service

import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.domain.model.ConnectionTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

// Request/Response models
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

class OpenAIService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val json = Json { ignoreUnknownKeys = true; isLenient = true }

  suspend fun fetchModels(config: AIConfig): Result<List<String>> =
    withContext(Dispatchers.IO) {
      try {
        val request = Request.Builder()
          .url("${config.baseUrl.trimEnd('/')}/models")
          .header("Authorization", "Bearer ${config.apiKey}")
          .get()
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("Failed to fetch models: ${response.code} $responseBody"))
        }

        if (responseBody == null) {
          return@withContext Result.failure(Exception("Empty response"))
        }

        val listResponse = json.decodeFromString(ModelListResponse.serializer(), responseBody)
        Result.success(listResponse.data.map { it.id }.sorted())
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun testConnection(config: AIConfig): Result<ConnectionTestResult> =
    withContext(Dispatchers.IO) {
      try {
        val messages = listOf(createTextMessage("user", "Hello"))
        val requestBody = OpenAIRequest(
          model = config.model,
          messages = messages,
          response_format = null
        )

        val jsonBody = json.encodeToString(OpenAIRequest.serializer(), requestBody)

        val startTime = System.currentTimeMillis()
        val request = Request.Builder()
          .url("${config.baseUrl.trimEnd('/')}/chat/completions")
          .header("Authorization", "Bearer ${config.apiKey}")
          .header("Content-Type", "application/json")
          .post(jsonBody.toRequestBody("application/json".toMediaType()))
          .build()

        val response = client.newCall(request).execute()
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        val responseBody = response.body?.string()

        if (!response.isSuccessful) {
          return@withContext Result.success(
            ConnectionTestResult(
              isSuccess = false,
              message = "Error (${response.code}): $responseBody",
              latencyMs = duration
            )
          )
        }

        if (responseBody == null) {
          return@withContext Result.success(
            ConnectionTestResult(
              isSuccess = false,
              message = "Empty response",
              latencyMs = duration
            )
          )
        }

        // Just verify we can parse it as OpenAI response
        val openAIResponse = json.decodeFromString(OpenAIResponse.serializer(), responseBody)
        val responseMessage = openAIResponse.choices.firstOrNull()?.message
        val content = when (val c = responseMessage?.content) {
          is JsonPrimitive -> c.content
          else -> c?.toString()
        } ?: ""

        Result.success(
          ConnectionTestResult(
            isSuccess = true,
            message = content.take(100),
            latencyMs = duration
          )
        )
      } catch (e: Exception) {
        Result.success(
          ConnectionTestResult(
            isSuccess = false,
            message = e.message ?: "Unknown error",
            latencyMs = 0
          )
        )
      }
    }
}
