package com.stack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val contentUri: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumArtist: String?,
    val albumArtUri: String?,
    val duration: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val year: Int?,
    val genre: String?,
    val size: Long,
    val bitrate: Int?,
    val sampleRate: Int?,
    val dateAdded: Long,
    val dateModified: Long,
    val relativePath: String?,
    val fileName: String,
    val status: String = "ACTIVE",
    val albumId: Long? = null,
    val artistId: Long? = null,
    val sourceFolderId: Long? = null
)
