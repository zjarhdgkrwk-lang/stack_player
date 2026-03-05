package com.stack.core.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackStateReducer @Inject constructor() {

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    val currentState: PlaybackState get() = _state.value

    fun updateState(reducer: PlaybackState.() -> PlaybackState) {
        _state.update(reducer)
    }

    fun reset() {
        _state.value = PlaybackState()
    }
}
