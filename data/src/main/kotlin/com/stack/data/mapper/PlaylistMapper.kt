package com.stack.data.mapper

import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.domain.model.Playlist

object PlaylistMapper {

    fun toDomain(entity: PlaylistEntity, trackCount: Int = 0, totalDuration: Long = 0): Playlist = Playlist(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        coverArtUri = entity.coverArtUri,
        trackCount = trackCount,
        totalDuration = totalDuration,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    fun toEntity(domain: Playlist): PlaylistEntity = PlaylistEntity(
        id = domain.id,
        name = domain.name,
        description = domain.description,
        coverArtUri = domain.coverArtUri,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt
    )

    fun toDomainList(entities: List<PlaylistEntity>): List<Playlist> = entities.map { toDomain(it) }
}
