package com.eatwhat.data.repository

import com.eatwhat.data.database.dao.AIProviderDao
import com.eatwhat.data.database.entities.AIProviderEntity
import kotlinx.coroutines.flow.Flow

class AIProviderRepository(private val dao: AIProviderDao) {
  val allProviders: Flow<List<AIProviderEntity>> = dao.getAllProviders()

  val activeProvider: Flow<AIProviderEntity?> = dao.getActiveProvider()

  fun getProviderById(id: Long): Flow<AIProviderEntity?> = dao.getProviderById(id)

  suspend fun insert(provider: AIProviderEntity): Long {
    return dao.insert(provider)
  }

  suspend fun update(provider: AIProviderEntity) {
    dao.update(provider)
  }

  suspend fun delete(id: Long) {
    dao.softDelete(id)
  }

  suspend fun setActive(id: Long) {
    dao.setActiveProvider(id)
  }

  suspend fun getActiveProviderSync(): AIProviderEntity? {
    return dao.getActiveProviderSync()
  }
}