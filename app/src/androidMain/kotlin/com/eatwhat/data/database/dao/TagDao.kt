package com.eatwhat.data.database.dao

import androidx.room.*
import com.eatwhat.data.database.entities.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Tag operations
 */
@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getTagById(tagId: Long): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name LIKE :query ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<TagEntity>>

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: Long)
}
