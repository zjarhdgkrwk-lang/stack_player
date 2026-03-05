package com.stack.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artist: String?,
    val albumArtUri: String?,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val year: Int?,
    val dateAdded: Long
)
