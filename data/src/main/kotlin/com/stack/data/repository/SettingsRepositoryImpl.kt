package com.stack.data.repository

import com.stack.data.local.preferences.PreferencesDataStore
import com.stack.data.local.preferences.PreferencesKeys
import com.stack.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : SettingsRepository {

    override fun getThemeMode(): Flow<String> {
        return preferencesDataStore.getValue(PreferencesKeys.THEME_MODE, "SYSTEM")
    }

    override suspend fun setThemeMode(mode: String) {
        preferencesDataStore.setValue(PreferencesKeys.THEME_MODE, mode)
    }

    override fun isDynamicColorEnabled(): Flow<Boolean> {
        return preferencesDataStore.getValue(PreferencesKeys.DYNAMIC_COLOR_ENABLED, true)
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        preferencesDataStore.setValue(PreferencesKeys.DYNAMIC_COLOR_ENABLED, enabled)
    }

    override fun isGaplessEnabled(): Flow<Boolean> {
        return preferencesDataStore.getValue(PreferencesKeys.GAPLESS_ENABLED, true)
    }

    override suspend fun setGaplessEnabled(enabled: Boolean) {
        preferencesDataStore.setValue(PreferencesKeys.GAPLESS_ENABLED, enabled)
    }

    override fun getCrossfadeDurationMs(): Flow<Int> {
        return preferencesDataStore.getValue(PreferencesKeys.CROSSFADE_DURATION_MS, 0)
    }

    override suspend fun setCrossfadeDurationMs(durationMs: Int) {
        preferencesDataStore.setValue(PreferencesKeys.CROSSFADE_DURATION_MS, durationMs)
    }

    override fun getPlaybackSpeed(): Flow<Float> {
        return preferencesDataStore.getValue(PreferencesKeys.PLAYBACK_SPEED, 1.0f)
    }

    override suspend fun setPlaybackSpeed(speed: Float) {
        preferencesDataStore.setValue(PreferencesKeys.PLAYBACK_SPEED, speed)
    }

    override fun isGateCompleted(): Flow<Boolean> {
        return preferencesDataStore.getValue(PreferencesKeys.GATE_COMPLETED, false)
    }

    override suspend fun setGateCompleted(completed: Boolean) {
        preferencesDataStore.setValue(PreferencesKeys.GATE_COMPLETED, completed)
    }

    override fun getShowGhostTracks(): Flow<Boolean> {
        return preferencesDataStore.getValue(PreferencesKeys.SHOW_GHOST_TRACKS, false)
    }

    override suspend fun setShowGhostTracks(show: Boolean) {
        preferencesDataStore.setValue(PreferencesKeys.SHOW_GHOST_TRACKS, show)
    }
}
