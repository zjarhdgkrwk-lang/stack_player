package com.stack.domain.repository

import com.stack.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    fun getLyricsForTrack(trackId: Long): Flow<Lyrics?>
    suspend fun saveLyrics(lyrics: Lyrics)
    suspend fun deleteLyrics(trackId: Long)
    suspend fun hasLyrics(trackId: Long): Boolean
}
