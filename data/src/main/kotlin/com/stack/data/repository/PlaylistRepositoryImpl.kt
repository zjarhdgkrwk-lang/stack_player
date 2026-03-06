package com.stack.data.repository

import com.stack.data.local.db.dao.PlaylistDao
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.mapper.PlaylistMapper
import com.stack.data.mapper.TrackMapper
import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistTrack
import com.stack.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { PlaylistMapper.toDomainList(it) }
    }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<PlaylistTrack>> {
        return combine(
            playlistDao.getPlaylistTracks(playlistId),
            playlistDao.getPlaylistTrackCrossRefs(playlistId)
        ) { tracks, crossRefs ->
            val crossRefMap = crossRefs.associateBy { it.trackId }
            tracks.mapNotNull { trackEntity ->
                val crossRef = crossRefMap[trackEntity.id] ?: return@mapNotNull null
                PlaylistTrack(
                    track = TrackMapper.toDomain(trackEntity),
                    orderIndex = crossRef.orderIndex,
                    addedAt = crossRef.addedAt
                )
            }.sortedBy { it.orderIndex }
        }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)?.let { entity ->
            val trackCount = playlistDao.getPlaylistTrackCount(id)
            val totalDuration = playlistDao.getPlaylistTotalDuration(id)
            PlaylistMapper.toDomain(entity, trackCount, totalDuration)
        }
    }

    override suspend fun insertPlaylist(playlist: Playlist): Long {
        return playlistDao.insertPlaylist(PlaylistMapper.toEntity(playlist))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(PlaylistMapper.toEntity(playlist))
    }

    override suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long, orderIndex: Int) {
        playlistDao.insertPlaylistTrackCrossRef(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = trackId,
                orderIndex = orderIndex,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deletePlaylistTrackCrossRef(playlistId, trackId)
    }

    override suspend fun reorderPlaylistTrack(playlistId: Long, fromIndex: Int, toIndex: Int) {
        val crossRefs = playlistDao.getPlaylistTrackCrossRefsList(playlistId)
            .sortedBy { it.orderIndex }
            .toMutableList()

        if (fromIndex < 0 || fromIndex >= crossRefs.size || toIndex < 0 || toIndex >= crossRefs.size) return

        val item = crossRefs.removeAt(fromIndex)
        crossRefs.add(toIndex, item)

        crossRefs.forEachIndexed { index, crossRef ->
            playlistDao.updateOrderIndex(crossRef.playlistId, crossRef.trackId, index)
        }
    }

    override suspend fun getMaxOrderIndex(playlistId: Long): Int {
        return playlistDao.getMaxOrderIndex(playlistId)
    }
}
