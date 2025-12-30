package com.eatwhat.domain.model

data class ConnectionTestResult(
  val isSuccess: Boolean,
  val message: String,
  val latencyMs: Long,
  val timestamp: Long = System.currentTimeMillis()
)