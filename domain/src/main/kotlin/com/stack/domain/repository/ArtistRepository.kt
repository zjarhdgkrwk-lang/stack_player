package com.stack.domain.repository

import com.stack.domain.model.Artist
import com.stack.domain.model.enums.SortOrder
import kotlinx.coroutines.flow.Flow

interface ArtistRepository {
    fun getAllArtists(sortOrder: SortOrder = SortOrder.TITLE_ASC): Flow<List<Artist>>
    suspend fun getArtistById(id: Long): Artist?
    suspend fun insertArtist(artist: Artist): Long
    suspend fun updateArtist(artist: Artist)
    suspend fun deleteArtist(id: Long)
}
