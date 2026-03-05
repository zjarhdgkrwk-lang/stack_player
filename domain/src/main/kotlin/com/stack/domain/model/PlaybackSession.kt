package com.stack.domain.model

import com.stack.domain.model.enums.RepeatMode
import com.stack.domain.model.enums.ShuffleMode

data class PlaybackSession(
    val lastTrackId: Long?,
    val lastPositionMs: Long = 0,
    val lastQueueJson: String? = null,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val updatedAt: Long
)
