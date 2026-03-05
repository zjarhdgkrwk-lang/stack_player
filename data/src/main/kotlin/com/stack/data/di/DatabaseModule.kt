package com.stack.data.di

import android.content.Context
import androidx.room.Room
import com.stack.data.local.db.StackDatabase
import com.stack.data.local.db.dao.AlbumDao
import com.stack.data.local.db.dao.ArtistDao
import com.stack.data.local.db.dao.CrashReportDao
import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.local.db.dao.PlayHistoryDao
import com.stack.data.local.db.dao.PlaybackSessionDao
import com.stack.data.local.db.dao.PlaylistDao
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.dao.TagDao
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.dao.TrackFtsDao
import com.stack.data.local.preferences.PreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StackDatabase {
        return Room.databaseBuilder(
            context,
            StackDatabase::class.java,
            "stack_database"
        ).build()
    }

    @Provides
    fun provideTrackDao(database: StackDatabase): TrackDao = database.trackDao()

    @Provides
    fun provideTrackFtsDao(database: StackDatabase): TrackFtsDao = database.trackFtsDao()

    @Provides
    fun provideAlbumDao(database: StackDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideArtistDao(database: StackDatabase): ArtistDao = database.artistDao()

    @Provides
    fun provideTagDao(database: StackDatabase): TagDao = database.tagDao()

    @Provides
    fun providePlaylistDao(database: StackDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideLyricsDao(database: StackDatabase): LyricsDao = database.lyricsDao()

    @Provides
    fun providePlayHistoryDao(database: StackDatabase): PlayHistoryDao = database.playHistoryDao()

    @Provides
    fun provideSourceFolderDao(database: StackDatabase): SourceFolderDao = database.sourceFolderDao()

    @Provides
    fun providePlaybackSessionDao(database: StackDatabase): PlaybackSessionDao = database.playbackSessionDao()

    @Provides
    fun provideCrashReportDao(database: StackDatabase): CrashReportDao = database.crashReportDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore {
        return PreferencesDataStore(context)
    }
}
