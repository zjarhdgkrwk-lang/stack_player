package com.stack.domain.repository

import com.stack.domain.model.Playlist
import com.stack.domain.model.PlaylistTrack
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getPlaylistTracks(playlistId: Long): Flow<List<PlaylistTrack>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun insertPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(id: Long)
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long, orderIndex: Int)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun reorderPlaylistTrack(playlistId: Long, fromIndex: Int, toIndex: Int)
    suspend fun getMaxOrderIndex(playlistId: Long): Int
}
