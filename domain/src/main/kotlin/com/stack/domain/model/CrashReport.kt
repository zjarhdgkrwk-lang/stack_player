package com.stack.domain.model

data class CrashReport(
    val id: Long,
    val createdAt: Long,
    val throwableSummary: String,
    val stacktrace: String,
    val appVersion: String,
    val deviceInfo: String
)
