package com.eatwhat.data.repository

import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.TagEntity
import com.eatwhat.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for Tag operations
 */
class TagRepository(private val database: EatWhatDatabase) {

    private val tagDao = database.tagDao()

    fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAllTags().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getTagByName(name: String): Tag? {
        return tagDao.getTagByName(name)?.toDomain()
    }

    suspend fun insertTag(tag: Tag): Long {
        return tagDao.insertTag(tag.toEntity())
    }

    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTag(tagId)
    }

    private fun TagEntity.toDomain(): Tag {
        return Tag(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }

    private fun Tag.toEntity(): TagEntity {
        return TagEntity(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }
}
