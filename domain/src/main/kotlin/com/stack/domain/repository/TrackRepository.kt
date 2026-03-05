package com.stack.domain.repository

import com.stack.domain.model.Track
import com.stack.domain.model.enums.SortOrder
import com.stack.domain.model.enums.TrackStatus
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun getAllTracks(sortOrder: SortOrder = SortOrder.DATE_ADDED_DESC): Flow<List<Track>>
    fun getTracksByStatus(status: TrackStatus): Flow<List<Track>>
    fun getTracksByAlbumId(albumId: Long): Flow<List<Track>>
    fun getTracksByArtistId(artistId: Long): Flow<List<Track>>
    fun getTracksByFolder(relativePath: String): Flow<List<Track>>
    suspend fun getTrackById(id: Long): Track?
    suspend fun getTrackByContentUri(contentUri: String): Track?
    suspend fun insertTrack(track: Track): Long
    suspend fun insertTracks(tracks: List<Track>)
    suspend fun updateTrack(track: Track)
    suspend fun deleteTrack(id: Long)
    suspend fun updateTrackStatus(id: Long, status: TrackStatus)
    suspend fun getTrackCount(): Int
}
