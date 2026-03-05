package com.stack.data.repository

import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.mapper.LyricsMapper
import com.stack.domain.model.Lyrics
import com.stack.domain.repository.LyricsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lyricsDao: LyricsDao
) : LyricsRepository {

    override fun getLyricsForTrack(trackId: Long): Flow<Lyrics?> {
        return lyricsDao.getLyricsForTrack(trackId).map { entity ->
            entity?.let { LyricsMapper.toDomain(it) }
        }
    }

    override suspend fun saveLyrics(lyrics: Lyrics) {
        lyricsDao.insertLyrics(LyricsMapper.toEntity(lyrics))
    }

    override suspend fun deleteLyrics(trackId: Long) {
        lyricsDao.deleteLyrics(trackId)
    }

    override suspend fun hasLyrics(trackId: Long): Boolean {
        return lyricsDao.hasLyrics(trackId) > 0
    }
}
