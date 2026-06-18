package com.eatwhat.domain.model

data class AIProviderEditData(
    val id: Long? = null,
    val name: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val isActive: Boolean = false
)
