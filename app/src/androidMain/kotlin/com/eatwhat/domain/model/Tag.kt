package com.eatwhat.domain.model

/**
 * Domain model for Tag
 */
data class Tag(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
