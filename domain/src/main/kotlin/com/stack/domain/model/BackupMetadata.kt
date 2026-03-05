package com.stack.domain.model

data class BackupMetadata(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val exportedAt: String,
    val trackCount: Int,
    val playlistCount: Int,
    val tagCount: Int,
    val lyricsCount: Int
)
