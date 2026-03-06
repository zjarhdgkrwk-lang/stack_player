package com.stack.feature.playlists

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.R
import com.stack.core.ui.components.ArtworkImage
import com.stack.core.ui.components.EmptyState
import com.stack.core.ui.components.LoadingState
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.PlaylistTrack
import com.stack.domain.model.Track
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.playlist?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                },
                actions = {
                    if (state.playlistTracks.isNotEmpty()) {
                        IconButton(onClick = { onPlayAll(state.playlistTracks.map { it.track }) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.play_all))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.dispatch(PlaylistDetailIntent.ShowTrackPicker) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_tracks))
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            state.playlistTracks.isEmpty() -> EmptyState(
                message = stringResource(R.string.no_tracks),
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                modifier = Modifier.padding(padding)
            )
            else -> {
                Column(modifier = Modifier.padding(padding)) {
                    state.playlist?.let { playlist ->
                        PlaylistHeader(playlist)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.playlistTracks, key = { it.track.id }) { playlistTrack ->
                            SwipeToDismissPlaylistTrack(
                                playlistTrack = playlistTrack,
                                onClick = {
                                    val tracks = state.playlistTracks.map { it.track }
                                    val index = state.playlistTracks.indexOf(playlistTrack)
                                    onTrackClick(playlistTrack.track, tracks, index)
                                },
                                onRemove = {
                                    viewModel.dispatch(PlaylistDetailIntent.RemoveTrack(playlistTrack.track.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showTrackPicker) {
        TrackPickerDialog(
            tracks = state.allTracks,
            onDismiss = { viewModel.dispatch(PlaylistDetailIntent.DismissTrackPicker) },
            onTrackSelected = { trackId -> viewModel.dispatch(PlaylistDetailIntent.AddTrack(trackId)) }
        )
    }
}

@Composable
private fun PlaylistHeader(playlist: com.stack.domain.model.Playlist) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm)
    ) {
        val desc = playlist.description
        if (!desc.isNullOrBlank()) {
            Text(
                text = desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"} \u00b7 ${formatDurationMs(playlist.totalDuration)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissPlaylistTrack(
    playlistTrack: PlaylistTrack,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onRemove()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            ArtworkImage(artworkUri = playlistTrack.track.albumArtUri, size = 48.dp)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlistTrack.track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = playlistTrack.track.displayArtist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatTrackMs(playlistTrack.track.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrackPickerDialog(
    tracks: List<Track>,
    onDismiss: () -> Unit,
    onTrackSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_tracks)) },
        text = {
            LazyColumn {
                items(tracks, key = { it.id }) { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTrackSelected(track.id)
                            }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(artworkUri = track.albumArtUri, size = 40.dp)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.displayArtist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) {
        String.format(Locale.US, "%dh %dm", hours, minutes)
    } else {
        String.format(Locale.US, "%dm", minutes)
    }
}

private fun formatTrackMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
