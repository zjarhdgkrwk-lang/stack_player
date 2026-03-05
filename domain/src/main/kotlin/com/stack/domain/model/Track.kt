package com.stack.domain.model

import com.stack.domain.model.enums.TrackStatus

data class Track(
    val id: Long,
    val contentUri: String,
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
    val status: TrackStatus,
    val albumId: Long?,
    val artistId: Long?,
    val isFavorite: Boolean = false
) {
    val displayArtist: String get() = artist?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
    val displayAlbum: String get() = album?.takeIf { it.isNotBlank() } ?: "Unknown Album"
    val isPlayable: Boolean get() = status == TrackStatus.ACTIVE
}
