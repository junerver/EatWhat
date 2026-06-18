package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eatwhat.data.preferences.AIConfig
import java.util.UUID

@Entity(
  tableName = "ai_providers",
  indices = [
    Index(value = ["is_active"]),
    Index(value = ["is_deleted"])
  ]
)
data class AIProviderEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,

  @ColumnInfo(name = "sync_id")
  val syncId: String = UUID.randomUUID().toString(),

  val name: String,

  @ColumnInfo(name = "base_url")
  val baseUrl: String,

  @ColumnInfo(name = "api_key")
  val apiKey: String,

  val model: String,

  @ColumnInfo(name = "is_active")
  val isActive: Boolean = false,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "last_modified")
  val lastModified: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "is_deleted")
  val isDeleted: Boolean = false
) {
  fun toAIConfig(): AIConfig {
    return AIConfig(
      baseUrl = baseUrl,
      apiKey = apiKey,
      model = model
    )
  }
}