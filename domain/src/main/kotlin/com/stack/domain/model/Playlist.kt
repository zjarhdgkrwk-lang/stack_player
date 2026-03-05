package com.stack.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val description: String?,
    val coverArtUri: String?,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val createdAt: Long,
    val updatedAt: Long
)
