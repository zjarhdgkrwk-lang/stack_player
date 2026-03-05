package com.stack.domain.repository

import com.stack.domain.model.PlaybackSession

interface PlaybackSessionRepository {
    suspend fun getSession(): PlaybackSession?
    suspend fun saveSession(session: PlaybackSession)
    suspend fun clearSession()
}
