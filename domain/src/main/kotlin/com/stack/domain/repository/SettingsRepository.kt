package com.stack.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)
    fun isDynamicColorEnabled(): Flow<Boolean>
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    fun isGaplessEnabled(): Flow<Boolean>
    suspend fun setGaplessEnabled(enabled: Boolean)
    fun getCrossfadeDurationMs(): Flow<Int>
    suspend fun setCrossfadeDurationMs(durationMs: Int)
    fun getPlaybackSpeed(): Flow<Float>
    suspend fun setPlaybackSpeed(speed: Float)
    fun isGateCompleted(): Flow<Boolean>
    suspend fun setGateCompleted(completed: Boolean)
    fun getShowGhostTracks(): Flow<Boolean>
    suspend fun setShowGhostTracks(show: Boolean)
}
