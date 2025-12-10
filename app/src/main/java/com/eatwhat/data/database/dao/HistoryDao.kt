package com.eatwhat.data.database.dao

import androidx.room.*
import com.eatwhat.data.database.entities.HistoryRecipeCrossRef
import com.eatwhat.data.database.entities.HistoryRecordEntity
import com.eatwhat.data.database.entities.PrepItemEntity
import com.eatwhat.data.database.relations.HistoryWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO for History operations
 */
@Dao
interface HistoryDao {

    // ========== History Record CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(history: HistoryRecordEntity): Long

    @Update
    suspend fun updateHistoryRecord(history: HistoryRecordEntity)

    @Query("UPDATE history_records SET is_deleted = 1, last_modified = :timestamp WHERE id = :historyId")
    suspend fun softDeleteHistory(historyId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM history_records WHERE id = :historyId AND is_deleted = 0")
    fun getHistoryById(historyId: Long): Flow<HistoryRecordEntity?>

    @Query("SELECT * FROM history_records WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryRecordEntity>>

    @Transaction
    @Query("SELECT * FROM history_records WHERE id = :historyId AND is_deleted = 0")
    fun getHistoryWithDetails(historyId: Long): Flow<HistoryWithDetails?>

    @Transaction
    @Query("SELECT * FROM history_records WHERE is_deleted = 0 ORDER BY timestamp DESC")
    fun getAllHistoryWithDetails(): Flow<List<HistoryWithDetails>>

    // ========== History-Recipe Cross Reference ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecipeCrossRefs(crossRefs: List<HistoryRecipeCrossRef>)

    @Query("SELECT * FROM history_recipe_cross_ref WHERE history_id = :historyId")
    fun getRecipeSnapshotsByHistoryId(historyId: Long): Flow<List<HistoryRecipeCrossRef>>

    // ========== Prep Item CRUD ==========

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrepItems(prepItems: List<PrepItemEntity>)

    @Update
    suspend fun updatePrepItem(prepItem: PrepItemEntity)

    @Query("SELECT * FROM prep_items WHERE history_id = :historyId ORDER BY order_index ASC")
    fun getPrepItemsByHistoryId(historyId: Long): Flow<List<PrepItemEntity>>

    @Query("UPDATE prep_items SET is_checked = :isChecked WHERE id = :prepItemId")
    suspend fun updatePrepItemChecked(prepItemId: Long, isChecked: Boolean)
}
