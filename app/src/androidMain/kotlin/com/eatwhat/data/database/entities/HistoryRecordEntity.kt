package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * History record entity for Room database
 * Represents a Roll history record
 */
@Entity(
    tableName = "history_records",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["sync_id"], unique = true),
        Index(value = ["is_locked"])
    ]
)
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "sync_id")
    val syncId: String = UUID.randomUUID().toString(),

    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_count")
    val totalCount: Int,

    @ColumnInfo(name = "meat_count")
    val meatCount: Int,

    @ColumnInfo(name = "veg_count")
    val vegCount: Int,

    @ColumnInfo(name = "soup_count")
    val soupCount: Int,

    val summary: String,

    @ColumnInfo(name = "custom_name", defaultValue = "")
    val customName: String = "",

    @ColumnInfo(name = "is_locked", defaultValue = "0")
    val isLocked: Boolean = false,

    @ColumnInfo(name = "last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
