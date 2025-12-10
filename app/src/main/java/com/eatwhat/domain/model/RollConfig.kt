package com.eatwhat.domain.model

/**
 * Domain model for Roll configuration
 * Represents user's Roll preferences
 */
data class RollConfig(
    val meatCount: Int = 0,
    val vegCount: Int = 0,
    val soupCount: Int = 0,
    val stapleCount: Int = 0,
    val autoBalance: Boolean = false
) {
    val totalCount: Int
        get() = meatCount + vegCount + soupCount + stapleCount

    fun isValid(): Boolean {
        return totalCount > 0
    }

    /**
     * Apply auto-balance logic
     * If enabled, automatically balance meat and veg counts
     */
    fun withAutoBalance(): RollConfig {
        if (!autoBalance || totalCount == 0) return this

        val total = totalCount
        val meatTarget = total / 2
        val vegTarget = total - meatTarget

        return copy(
            meatCount = meatTarget,
            vegCount = vegTarget
        )
    }
}
