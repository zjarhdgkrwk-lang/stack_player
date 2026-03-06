package com.stack.feature.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.stack.core.ui.components.LoadingState
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.Track
import com.stack.feature.library.components.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onBack: () -> Unit,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onShuffleAll: (List<Track>) -> Unit,
    onPlayNext: ((Track) -> Unit)? = null,
    onAddToPlaylist: ((Track) -> Unit)? = null,
    onAssignTag: ((Track) -> Unit)? = null,
    onToggleFavorite: ((Track) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.album?.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArtworkImage(
                        artworkUri = state.album?.albumArtUri,
                        size = 200.dp,
                        modifier = Modifier.aspectRatio(1f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = state.album?.artist ?: stringResource(R.string.unknown_artist),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.tracks_count, state.tracks.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Row {
                        Button(onClick = { onPlayAll(state.tracks) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.xxs))
                            Text(stringResource(R.string.play_all))
                        }
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        OutlinedButton(onClick = { onShuffleAll(state.tracks) }) {
                            Icon(Icons.Default.Shuffle, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.xxs))
                            Text(stringResource(R.string.play_all_shuffled))
                        }
                    }
                }
            }

            // Track list
            items(state.tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    onClick = {
                        val index = state.tracks.indexOf(track)
                        onTrackClick(track, state.tracks, index)
                    },
                    onPlayNext = onPlayNext,
                    onAddToPlaylist = onAddToPlaylist,
                    onAssignTag = onAssignTag,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}
