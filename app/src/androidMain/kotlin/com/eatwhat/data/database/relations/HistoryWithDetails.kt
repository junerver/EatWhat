package com.eatwhat.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.eatwhat.data.database.entities.HistoryRecipeCrossRef
import com.eatwhat.data.database.entities.HistoryRecordEntity
import com.eatwhat.data.database.entities.PrepItemEntity

/**
 * History record with all its details (recipe snapshots, prep items)
 * Used for querying complete history information
 */
data class HistoryWithDetails(
    @Embedded
    val history: HistoryRecordEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val recipeSnapshots: List<HistoryRecipeCrossRef>,

    @Relation(
        parentColumn = "id",
        entityColumn = "history_id"
    )
    val prepItems: List<PrepItemEntity>
)
