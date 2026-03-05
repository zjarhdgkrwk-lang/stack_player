package com.stack.data.mapper

import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.Track
import com.stack.domain.model.enums.TrackStatus

object TrackMapper {

    fun toDomain(entity: TrackEntity): Track = Track(
        id = entity.id,
        contentUri = entity.contentUri,
        title = entity.title,
        artist = entity.artist,
        album = entity.album,
        albumArtist = entity.albumArtist,
        albumArtUri = entity.albumArtUri,
        duration = entity.duration,
        trackNumber = entity.trackNumber,
        discNumber = entity.discNumber,
        year = entity.year,
        genre = entity.genre,
        size = entity.size,
        bitrate = entity.bitrate,
        sampleRate = entity.sampleRate,
        dateAdded = entity.dateAdded,
        dateModified = entity.dateModified,
        relativePath = entity.relativePath,
        fileName = entity.fileName,
        status = TrackStatus.valueOf(entity.status),
        albumId = entity.albumId,
        artistId = entity.artistId
    )

    fun toEntity(domain: Track): TrackEntity = TrackEntity(
        id = domain.id,
        contentUri = domain.contentUri,
        title = domain.title,
        artist = domain.artist,
        album = domain.album,
        albumArtist = domain.albumArtist,
        albumArtUri = domain.albumArtUri,
        duration = domain.duration,
        trackNumber = domain.trackNumber,
        discNumber = domain.discNumber,
        year = domain.year,
        genre = domain.genre,
        size = domain.size,
        bitrate = domain.bitrate,
        sampleRate = domain.sampleRate,
        dateAdded = domain.dateAdded,
        dateModified = domain.dateModified,
        relativePath = domain.relativePath,
        fileName = domain.fileName,
        status = domain.status.name,
        albumId = domain.albumId,
        artistId = domain.artistId
    )

    fun toDomainList(entities: List<TrackEntity>): List<Track> = entities.map { toDomain(it) }
}
