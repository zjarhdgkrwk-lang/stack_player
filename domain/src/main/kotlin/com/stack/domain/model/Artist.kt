package com.stack.domain.model

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int = 0,
    val trackCount: Int = 0
)
