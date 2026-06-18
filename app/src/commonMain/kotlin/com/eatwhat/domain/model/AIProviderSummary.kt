package com.eatwhat.domain.model

data class AIProviderSummary(
    val id: Long,
    val name: String,
    val baseUrl: String,
    val model: String,
    val isActive: Boolean
)
