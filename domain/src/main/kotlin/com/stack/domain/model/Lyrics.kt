package com.stack.domain.model

import com.stack.domain.model.enums.LyricsSyncType

data class Lyrics(
    val trackId: Long,
    val content: String,
    val syncType: LyricsSyncType,
    val source: String,
    val lrcFilePath: String? = null,
    val lines: List<LyricsLine> = emptyList(),
    val updatedAt: Long
)
