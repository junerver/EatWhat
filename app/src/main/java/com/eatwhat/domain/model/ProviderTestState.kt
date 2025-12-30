package com.eatwhat.domain.model

data class ProviderTestState(
  val isTesting: Boolean = false,
  val isSuccess: Boolean = false,
  val latency: Long = 0,
  val message: String = "",
  val lastTestTime: Long = 0
)