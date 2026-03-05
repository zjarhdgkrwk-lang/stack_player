package com.stack.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_session")
data class PlaybackSessionEntity(
    @PrimaryKey val id: Int = 0,
    val lastTrackId: Long?,
    val lastPositionMs: Long = 0,
    val lastQueueJson: String? = null,
    val repeatMode: String = "OFF",
    val shuffleMode: String = "OFF",
    val updatedAt: Long
)
