package com.stack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stack.data.local.db.converter.Converters
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
import com.stack.data.local.db.entity.AlbumEntity
import com.stack.data.local.db.entity.ArtistEntity
import com.stack.data.local.db.entity.CrashReportEntity
import com.stack.data.local.db.entity.LyricsEntity
import com.stack.data.local.db.entity.PlayHistoryEntity
import com.stack.data.local.db.entity.PlaybackSessionEntity
import com.stack.data.local.db.entity.PlaylistEntity
import com.stack.data.local.db.entity.PlaylistTrackCrossRef
import com.stack.data.local.db.entity.SourceFolderEntity
import com.stack.data.local.db.entity.TagEntity
import com.stack.data.local.db.entity.TrackEntity
import com.stack.data.local.db.entity.TrackFtsEntity
import com.stack.data.local.db.entity.TrackTagCrossRef

@Database(
    entities = [
        TrackEntity::class,
        TrackFtsEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        TagEntity::class,
        TrackTagCrossRef::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        LyricsEntity::class,
        PlayHistoryEntity::class,
        SourceFolderEntity::class,
        PlaybackSessionEntity::class,
        CrashReportEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class StackDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackFtsDao(): TrackFtsDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun tagDao(): TagDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun sourceFolderDao(): SourceFolderDao
    abstract fun playbackSessionDao(): PlaybackSessionDao
    abstract fun crashReportDao(): CrashReportDao
}
