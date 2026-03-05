package com.stack.data.mapper

import com.stack.data.local.db.entity.AlbumEntity
import com.stack.domain.model.Album

object AlbumMapper {

    fun toDomain(entity: AlbumEntity): Album = Album(
        id = entity.id,
        name = entity.name,
        artist = entity.artist,
        albumArtUri = entity.albumArtUri,
        trackCount = entity.trackCount,
        totalDuration = entity.totalDuration,
        year = entity.year,
        dateAdded = entity.dateAdded
    )

    fun toEntity(domain: Album): AlbumEntity = AlbumEntity(
        id = domain.id,
        name = domain.name,
        artist = domain.artist,
        albumArtUri = domain.albumArtUri,
        trackCount = domain.trackCount,
        totalDuration = domain.totalDuration,
        year = domain.year,
        dateAdded = domain.dateAdded
    )

    fun toDomainList(entities: List<AlbumEntity>): List<Album> = entities.map { toDomain(it) }
}
