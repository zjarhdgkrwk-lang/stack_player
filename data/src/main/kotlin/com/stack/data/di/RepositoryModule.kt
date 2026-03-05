package com.stack.data.di

import com.stack.data.repository.AlbumRepositoryImpl
import com.stack.data.repository.ArtistRepositoryImpl
import com.stack.data.repository.CrashReportRepositoryImpl
import com.stack.data.repository.LyricsRepositoryImpl
import com.stack.data.repository.PlayHistoryRepositoryImpl
import com.stack.data.repository.PlaybackSessionRepositoryImpl
import com.stack.data.repository.PlaylistRepositoryImpl
import com.stack.data.repository.SettingsRepositoryImpl
import com.stack.data.repository.SourceFolderRepositoryImpl
import com.stack.data.repository.TagRepositoryImpl
import com.stack.data.repository.TrackRepositoryImpl
import com.stack.domain.repository.AlbumRepository
import com.stack.domain.repository.ArtistRepository
import com.stack.domain.repository.CrashReportRepository
import com.stack.domain.repository.LyricsRepository
import com.stack.domain.repository.PlayHistoryRepository
import com.stack.domain.repository.PlaybackSessionRepository
import com.stack.domain.repository.PlaylistRepository
import com.stack.domain.repository.SettingsRepository
import com.stack.domain.repository.SourceFolderRepository
import com.stack.domain.repository.TagRepository
import com.stack.domain.repository.TrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    @Singleton
    abstract fun bindAlbumRepository(impl: AlbumRepositoryImpl): AlbumRepository

    @Binds
    @Singleton
    abstract fun bindArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository

    @Binds
    @Singleton
    abstract fun bindPlayHistoryRepository(impl: PlayHistoryRepositoryImpl): PlayHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSourceFolderRepository(impl: SourceFolderRepositoryImpl): SourceFolderRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPlaybackSessionRepository(impl: PlaybackSessionRepositoryImpl): PlaybackSessionRepository

    @Binds
    @Singleton
    abstract fun bindCrashReportRepository(impl: CrashReportRepositoryImpl): CrashReportRepository
}
