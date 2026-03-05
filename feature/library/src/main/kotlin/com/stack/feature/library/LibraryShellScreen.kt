package com.stack.feature.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.stack.core.R
import com.stack.core.player.PlaybackState
import com.stack.domain.model.Track
import com.stack.feature.player.MiniPlayer

enum class LibraryTab(val labelRes: Int, val icon: ImageVector) {
    TRACKS(R.string.nav_tracks, Icons.Default.MusicNote),
    ALBUMS(R.string.nav_albums, Icons.Default.Album),
    ARTISTS(R.string.nav_artists, Icons.Default.Person),
    FOLDERS(R.string.nav_folders, Icons.Default.Folder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryShellScreen(
    playbackState: PlaybackState,
    allTracks: List<Track>,
    allTracksLoading: Boolean,
    isExpanded: Boolean,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onAlbumClick: (Long) -> Unit,
    onArtistClick: (Long) -> Unit,
    onMiniPlayerTap: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = LibraryTab.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.library)) }
            )
        },
        bottomBar = {
            Column {
                // MiniPlayer - only in Compact mode, when a track is playing
                if (!isExpanded && playbackState.currentTrack != null) {
                    MiniPlayer(
                        state = playbackState,
                        onTap = onMiniPlayerTap,
                        onPlayPause = onPlayPause,
                        onSkipNext = onSkipNext,
                        onSkipPrevious = onSkipPrevious
                    )
                }

                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { padding ->
        when (tabs[selectedTab]) {
            LibraryTab.TRACKS -> TracksTab(
                onTrackClick = onTrackClick,
                modifier = Modifier.padding(padding)
            )
            LibraryTab.ALBUMS -> AlbumsTab(
                onAlbumClick = onAlbumClick,
                modifier = Modifier.padding(padding)
            )
            LibraryTab.ARTISTS -> ArtistsTab(
                onArtistClick = onArtistClick,
                modifier = Modifier.padding(padding)
            )
            LibraryTab.FOLDERS -> FoldersTab(
                tracks = allTracks,
                isLoading = allTracksLoading,
                onTrackClick = onTrackClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}
