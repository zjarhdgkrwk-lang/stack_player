package com.stack.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val artist: String?,
    val albumArtUri: String?,
    val trackCount: Int = 0,
    val totalDuration: Long = 0,
    val year: Int?,
    val dateAdded: Long
)
