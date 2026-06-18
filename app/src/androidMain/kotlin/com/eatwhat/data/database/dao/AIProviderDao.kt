package com.eatwhat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eatwhat.data.database.entities.AIProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AIProviderDao {
  @Query("SELECT * FROM ai_providers WHERE is_deleted = 0 ORDER BY created_at DESC")
  fun getAllProviders(): Flow<List<AIProviderEntity>>

  @Query("SELECT * FROM ai_providers WHERE id = :id AND is_deleted = 0")
  fun getProviderById(id: Long): Flow<AIProviderEntity?>

  @Query("SELECT * FROM ai_providers WHERE is_active = 1 AND is_deleted = 0 LIMIT 1")
  fun getActiveProvider(): Flow<AIProviderEntity?>

  @Query("SELECT * FROM ai_providers WHERE is_active = 1 AND is_deleted = 0 LIMIT 1")
  suspend fun getActiveProviderSync(): AIProviderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(provider: AIProviderEntity): Long

  @Update
  suspend fun update(provider: AIProviderEntity)

  @Query("UPDATE ai_providers SET is_deleted = 1, last_modified = :timestamp WHERE id = :id")
  suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE ai_providers SET is_active = 0 WHERE is_active = 1")
  suspend fun deactivateAll()

  @Transaction
  suspend fun setActiveProvider(id: Long) {
    deactivateAll()
    val provider = getProviderByIdSync(id)
    if (provider != null) {
      update(provider.copy(isActive = true, lastModified = System.currentTimeMillis()))
    }
  }

  @Query("SELECT * FROM ai_providers WHERE id = :id LIMIT 1")
  suspend fun getProviderByIdSync(id: Long): AIProviderEntity?

  @Query("SELECT COUNT(*) FROM ai_providers WHERE is_deleted = 0")
  suspend fun getProviderCount(): Int
}