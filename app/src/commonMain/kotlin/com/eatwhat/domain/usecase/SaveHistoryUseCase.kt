package com.eatwhat.domain.usecase

import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.domain.model.RollResult

interface HistorySaver {
    suspend fun insertHistory(
        config: RollConfig,
        recipes: List<Recipe>,
        prepItems: List<PrepListItem>
    ): Long
}

/**
 * Use case for saving roll result to history
 */
class SaveHistoryUseCase(private val historySaver: HistorySaver) {

    /**
     * Save roll result and prep list to history
     * @return History record ID
     */
    suspend operator fun invoke(
        rollResult: RollResult,
        prepItems: List<PrepListItem>
    ): Long {
        return historySaver.insertHistory(
            config = rollResult.config,
            recipes = rollResult.recipes,
            prepItems = prepItems
        )
    }
}
