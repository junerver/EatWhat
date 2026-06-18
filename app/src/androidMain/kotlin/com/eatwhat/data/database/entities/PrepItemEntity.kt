package com.eatwhat.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Prep item entity for Room database
 * Represents an item in the prep checklist
 */
@Entity(
    tableName = "prep_items",
    foreignKeys = [
        ForeignKey(
            entity = HistoryRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["history_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["history_id"])]
)
data class PrepItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "history_id")
    val historyId: Long,

    @ColumnInfo(name = "ingredient_name")
    val ingredientName: String,

    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)
