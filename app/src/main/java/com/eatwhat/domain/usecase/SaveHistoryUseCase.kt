package com.eatwhat.domain.usecase

import com.eatwhat.data.repository.HistoryRepository
import com.eatwhat.domain.model.RollResult

/**
 * Use case for saving roll result to history
 */
class SaveHistoryUseCase(private val historyRepository: HistoryRepository) {

    /**
     * Save roll result and prep list to history
     * @return History record ID
     */
    suspend operator fun invoke(
        rollResult: RollResult,
        prepItems: List<PrepListItem>
    ): Long {
        return historyRepository.insertHistory(
            config = rollResult.config,
            recipes = rollResult.recipes,
            prepItems = prepItems
        )
    }
}
