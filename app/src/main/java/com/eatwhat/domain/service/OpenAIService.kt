package com.eatwhat.domain.service

import android.util.Log
import com.eatwhat.data.preferences.AIConfig
import com.eatwhat.domain.model.ConnectionTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
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

// Target Result
@Serializable
data class RecipeAIResult(
  val name: String,
  val type: String, // MEAT, VEG, SOUP, STAPLE, OTHER
  val difficulty: String, // EASY, MEDIUM, HARD
  val estimatedTime: Int, // minutes
  val ingredients: List<IngredientAI>,
  val steps: List<String>,
  val tags: List<String>,
  val icon: String, // Emoji
  val isFoodImage: Boolean = false // Whether the input image was a food photo
)

@Serializable
data class IngredientAI(
  val name: String,
  val amount: String,
  val unit: String
)

class OpenAIService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val json = Json { ignoreUnknownKeys = true; isLenient = true }

  suspend fun analyzeRecipe(
    config: AIConfig,
    prompt: String,
    imageBase64: String? = null
  ): Result<RecipeAIResult> =
    withContext(Dispatchers.IO) {
      try {
        val systemPrompt = """
                你是一个专业的菜谱分析助手。请分析用户的输入（菜谱描述、做法、图片等），并输出符合以下 JSON 格式的菜谱数据。

                {
                  "name": "菜名",
                  "type": "MEAT|VEG|SOUP|STAPLE|OTHER",
                  "difficulty": "EASY|MEDIUM|HARD",
                  "estimatedTime": 30,
                  "ingredients": [
                    { "name": "食材名", "amount": "数量", "unit": "G|ML|PIECE|SPOON|MODERATE" }
                  ],
                  "steps": ["步骤1", "步骤2"],
                  "tags": ["标签1", "标签2"],
                  "icon": "🍳",
                  "isFoodImage": false
                }

                注意：
                1. type 必须是 MEAT(荤菜), VEG(素菜), SOUP(汤), STAPLE(主食), OTHER(其他) 之一。
                   注意：OTHER 类型用于蘸汁、酱料、汤底等辅助型配方，或者不能单独作为一道菜品的食谱。
                2. unit 必须是 G(克), ML(毫升), PIECE(个), SPOON(勺), MODERATE(适量) 之一。
                3. icon 请根据菜品内容选择一个最合适的 Emoji。
                4. 如果输入信息不全，请根据经验合理补全。
                5. 请只输出 JSON 内容，不要包含 markdown 标记。
                6. estimatedTime 应在 1-300 之间。
                7. 如果用户上传了图片且该图片是做好的食物照片（成品图），请将 "isFoodImage" 设为 true；如果是纯文字截图或非食物照片，请设为 false。
                8. 如果 "isFoodImage" 为 true，icon 字段仍需生成一个 emoji 作为备用，但前端会优先使用用户上传的图片。
            """.trimIndent()

        val userContent: JsonElement = if (imageBase64 != null) {
          buildJsonArray {
            addJsonObject {
              put("type", "text")
              put("text", prompt.ifBlank { "请分析这张图片中的菜谱" })
            }
            addJsonObject {
              put("type", "image_url")
              putJsonObject("image_url") {
                // WebP is used by ImageUtils
                put("url", "data:image/webp;base64,$imageBase64")
              }
            }
          }
        } else {
          JsonPrimitive(prompt)
        }

        val messages = listOf(
          createTextMessage("system", systemPrompt),
          OpenAIMessage("user", userContent)
        )

        val requestBody = OpenAIRequest(
          model = config.model,
          messages = messages,
          response_format = ResponseFormat(type = "json_object")
        )

        val jsonBody = json.encodeToString(OpenAIRequest.serializer(), requestBody)

        val request = Request.Builder()
          .url("${config.baseUrl}/chat/completions")
          .header("Authorization", "Bearer ${config.apiKey}")
          .header("Content-Type", "application/json")
          .post(jsonBody.toRequestBody("application/json".toMediaType()))
          .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
        Log.d("AI_AN", "analyzeRecipe: $responseBody")

        if (!response.isSuccessful) {
          return@withContext Result.failure(Exception("API Error: ${response.code} $responseBody"))
        }

        if (responseBody == null) {
          return@withContext Result.failure(Exception("Empty response"))
        }

        val openAIResponse = json.decodeFromString(OpenAIResponse.serializer(), responseBody)
        val responseMessage = openAIResponse.choices.firstOrNull()?.message

        val content = when (val c = responseMessage?.content) {
          is JsonPrimitive -> c.content
          else -> c?.toString()
        } ?: return@withContext Result.failure(Exception("No content generated"))

        try {
          val recipeResult = json.decodeFromString(RecipeAIResult.serializer(), content)
          Result.success(recipeResult)
        } catch (e: Exception) {
          Result.failure(Exception("Failed to parse JSON: ${e.message}\nContent: $content"))
        }
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

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
