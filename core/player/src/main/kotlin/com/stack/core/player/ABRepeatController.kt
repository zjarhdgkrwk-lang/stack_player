package com.stack.core.player

import com.stack.core.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ABRepeatController @Inject constructor(
    private val stateReducer: PlaybackStateReducer
) {
    companion object {
        private const val TAG = "ABRepeatController"
    }

    fun checkAndLoop(currentPositionMs: Long): Long? {
        val state = stateReducer.currentState
        val pointA = state.abRepeatA ?: return null
        val pointB = state.abRepeatB ?: return null

        if (currentPositionMs >= pointB) {
            Logger.d(TAG, "Reached B point ($pointB), seeking to A ($pointA)")
            return pointA
        }
        return null
    }

    fun isActive(): Boolean {
        val state = stateReducer.currentState
        return state.abRepeatA != null && state.abRepeatB != null
    }
}
