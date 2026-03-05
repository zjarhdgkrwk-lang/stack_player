package com.stack.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LyricsEntity(
    @PrimaryKey val trackId: Long,
    val content: String,
    val syncType: String,
    val source: String,
    val lrcFilePath: String? = null,
    val updatedAt: Long
)
