package com.stack.player.navigation

sealed class NavRoute(val route: String) {
    data object Gate : NavRoute("gate")
    data object Main : NavRoute("main")
    data object AlbumDetail : NavRoute("album/{albumId}")
    data object ArtistDetail : NavRoute("artist/{artistId}")
    data object PlaylistDetail : NavRoute("playlist/{playlistId}")
    data object TagDetail : NavRoute("tag/{tagId}")
    data object NowPlaying : NavRoute("now_playing")
    data object Search : NavRoute("search")
    data object Settings : NavRoute("settings")
    data object Equalizer : NavRoute("equalizer")
    data object Playlists : NavRoute("playlists")
    data object Tags : NavRoute("tags")
    data object BackupRestore : NavRoute("backup_restore")
    data object ScanFolders : NavRoute("scan_folders")
    data object LyricsEditor : NavRoute("lyrics_editor/{trackId}")
    data object CrashLog : NavRoute("crash_log")
}
