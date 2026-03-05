package com.stack.domain.model

data class PlaylistTrack(
    val track: Track,
    val orderIndex: Int,
    val addedAt: Long
)
