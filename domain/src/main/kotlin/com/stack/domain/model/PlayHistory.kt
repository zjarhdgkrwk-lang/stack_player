package com.stack.domain.model

data class PlayHistory(
    val id: Long,
    val trackId: Long,
    val playedAt: Long,
    val durationPlayed: Long
)
