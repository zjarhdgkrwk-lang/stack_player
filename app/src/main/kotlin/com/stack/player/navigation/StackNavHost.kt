package com.stack.player.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stack.domain.model.Track
import com.stack.domain.model.enums.ShuffleMode
import com.stack.feature.gate.GateScreen
import com.stack.feature.library.AlbumDetailScreen
import com.stack.feature.library.ArtistDetailScreen
import com.stack.feature.library.LibraryShellScreen
import com.stack.feature.library.TracksViewModel
import com.stack.feature.library.components.AddToPlaylistDialog
import com.stack.feature.library.components.AssignTagDialog
import com.stack.feature.player.NowPlayingScreen
import com.stack.feature.player.PlaybackViewModel
import com.stack.feature.player.QueueSheet
import com.stack.feature.playlists.PlaylistDetailScreen
import com.stack.feature.playlists.PlaylistsScreen
import com.stack.feature.tags.TagDetailScreen
import com.stack.feature.tags.TagsScreen

@Composable
fun StackNavHost(
    isExpanded: Boolean = false
) {
    val navController = rememberNavController()
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val playbackState by playbackViewModel.playbackState.collectAsStateWithLifecycle()
    val isFavorite by playbackViewModel.isFavorite.collectAsStateWithLifecycle()
    val contextMenuState by playbackViewModel.contextMenuState.collectAsStateWithLifecycle()

    // All tracks for folders tab (shared)
    val tracksViewModel: TracksViewModel = hiltViewModel()
    val tracksState by tracksViewModel.state.collectAsStateWithLifecycle()

    var showQueue by remember { mutableStateOf(false) }

    val onTrackClick: (Track, List<Track>, Int) -> Unit = { track, queue, index ->
        playbackViewModel.play(track, queue, index)
    }

    val onPlayAll: (List<Track>) -> Unit = { tracks ->
        if (tracks.isNotEmpty()) {
            playbackViewModel.play(tracks.first(), tracks, 0)
        }
    }

    val onShuffleAll: (List<Track>) -> Unit = { tracks ->
        if (tracks.isNotEmpty()) {
            playbackViewModel.setShuffleMode(ShuffleMode.ON)
            playbackViewModel.play(tracks.first(), tracks, 0)
        }
    }

    // Shared context menu handlers for detail screens
    val onPlayNext: (Track) -> Unit = { track -> playbackViewModel.addNext(track) }
    val onAddToPlaylist: (Track) -> Unit = { track -> playbackViewModel.showPlaylistPicker(track) }
    val onAssignTag: (Track) -> Unit = { track -> playbackViewModel.showTagAssigner(track) }
    val onToggleFavoriteForTrack: (Track) -> Unit = { track -> playbackViewModel.toggleFavoriteForTrack(track) }

    NavHost(
        navController = navController,
        startDestination = NavRoute.Gate.route
    ) {
        composable(NavRoute.Gate.route) {
            GateScreen(
                onGateCompleted = {
                    navController.navigate(NavRoute.Main.route) {
                        popUpTo(NavRoute.Gate.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.Main.route) {
            if (isExpanded) {
                // Dual-pane layout
                Row(modifier = Modifier.fillMaxSize()) {
                    LibraryShellScreen(
                        playbackState = playbackState,
                        allTracks = tracksState.tracks,
                        allTracksLoading = tracksState.isLoading,
                        isExpanded = true,
                        onTrackClick = onTrackClick,
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onMiniPlayerTap = { },
                        onPlayPause = { playbackViewModel.togglePlayPause() },
                        onSkipNext = { playbackViewModel.skipNext() },
                        onSkipPrevious = { playbackViewModel.skipPrevious() },
                        onTagsClick = { navController.navigate(NavRoute.Tags.route) },
                        onPlaylistsClick = { navController.navigate(NavRoute.Playlists.route) },
                        isFavorite = isFavorite,
                        onToggleFavorite = { playbackViewModel.toggleFavorite() },
                        onPlayNext = onPlayNext,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.5f)
                    )
                    // Right pane - NowPlaying always visible
                    NowPlayingScreen(
                        state = playbackState,
                        isFavorite = isFavorite,
                        onCollapse = { },
                        onPlayPause = { playbackViewModel.togglePlayPause() },
                        onSkipNext = { playbackViewModel.skipNext() },
                        onSkipPrevious = { playbackViewModel.skipPrevious() },
                        onSeek = { playbackViewModel.seekTo(it) },
                        onToggleShuffle = { playbackViewModel.toggleShuffle() },
                        onToggleRepeat = { playbackViewModel.toggleRepeatMode() },
                        onToggleFavorite = { playbackViewModel.toggleFavorite() },
                        onOpenQueue = { showQueue = true },
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                    )
                }
            } else {
                // Compact layout
                LibraryShellScreen(
                    playbackState = playbackState,
                    allTracks = tracksState.tracks,
                    allTracksLoading = tracksState.isLoading,
                    isExpanded = false,
                    onTrackClick = onTrackClick,
                    onAlbumClick = { albumId ->
                        navController.navigate("album/$albumId")
                    },
                    onArtistClick = { artistId ->
                        navController.navigate("artist/$artistId")
                    },
                    onMiniPlayerTap = {
                        navController.navigate(NavRoute.NowPlaying.route)
                    },
                    onPlayPause = { playbackViewModel.togglePlayPause() },
                    onSkipNext = { playbackViewModel.skipNext() },
                    onSkipPrevious = { playbackViewModel.skipPrevious() },
                    onTagsClick = { navController.navigate(NavRoute.Tags.route) },
                    onPlaylistsClick = { navController.navigate(NavRoute.Playlists.route) },
                    isFavorite = isFavorite,
                    onToggleFavorite = { playbackViewModel.toggleFavorite() },
                    onPlayNext = onPlayNext
                )
            }
        }

        composable(NavRoute.NowPlaying.route) {
            NowPlayingScreen(
                state = playbackState,
                isFavorite = isFavorite,
                onCollapse = { navController.popBackStack() },
                onPlayPause = { playbackViewModel.togglePlayPause() },
                onSkipNext = { playbackViewModel.skipNext() },
                onSkipPrevious = { playbackViewModel.skipPrevious() },
                onSeek = { playbackViewModel.seekTo(it) },
                onToggleShuffle = { playbackViewModel.toggleShuffle() },
                onToggleRepeat = { playbackViewModel.toggleRepeatMode() },
                onToggleFavorite = { playbackViewModel.toggleFavorite() },
                onOpenQueue = { showQueue = true }
            )
        }

        composable(
            route = NavRoute.AlbumDetail.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) {
            AlbumDetailScreen(
                onBack = { navController.popBackStack() },
                onTrackClick = onTrackClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
                onPlayNext = onPlayNext,
                onAddToPlaylist = onAddToPlaylist,
                onAssignTag = onAssignTag,
                onToggleFavorite = onToggleFavoriteForTrack
            )
        }

        composable(
            route = NavRoute.ArtistDetail.route,
            arguments = listOf(navArgument("artistId") { type = NavType.LongType })
        ) {
            ArtistDetailScreen(
                onBack = { navController.popBackStack() },
                onTrackClick = onTrackClick,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
                onPlayNext = onPlayNext,
                onAddToPlaylist = onAddToPlaylist,
                onAssignTag = onAssignTag,
                onToggleFavorite = onToggleFavoriteForTrack
            )
        }

        composable(NavRoute.Tags.route) {
            TagsScreen(
                onBack = { navController.popBackStack() },
                onTagClick = { tagId -> navController.navigate("tag/$tagId") }
            )
        }

        composable(
            route = NavRoute.TagDetail.route,
            arguments = listOf(navArgument("tagId") { type = NavType.LongType })
        ) {
            TagDetailScreen(
                onBack = { navController.popBackStack() },
                onTrackClick = onTrackClick
            )
        }

        composable(NavRoute.Playlists.route) {
            PlaylistsScreen(
                onBack = { navController.popBackStack() },
                onPlaylistClick = { playlistId -> navController.navigate("playlist/$playlistId") }
            )
        }

        composable(
            route = NavRoute.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) {
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onTrackClick = onTrackClick,
                onPlayAll = onPlayAll
            )
        }
    }

    // Shared context menu dialogs
    if (contextMenuState.showPlaylistPicker) {
        AddToPlaylistDialog(
            playlists = contextMenuState.playlists,
            onDismiss = { playbackViewModel.dismissPlaylistPicker() },
            onPlaylistSelected = { playbackViewModel.addTrackToPlaylist(it) }
        )
    }

    if (contextMenuState.showTagAssigner) {
        AssignTagDialog(
            tags = contextMenuState.allTags,
            assignedTagIds = contextMenuState.contextTrackTagIds,
            onDismiss = { playbackViewModel.dismissTagAssigner() },
            onToggleTag = { playbackViewModel.toggleTrackTag(it) }
        )
    }

    // Queue sheet overlay
    if (showQueue) {
        QueueSheet(
            queue = playbackState.queue,
            currentIndex = playbackState.currentIndex,
            onDismiss = { showQueue = false },
            onTrackTap = { index ->
                playbackViewModel.playAt(index)
                showQueue = false
            },
            onClear = {
                playbackViewModel.stop()
                showQueue = false
            }
        )
    }
}
