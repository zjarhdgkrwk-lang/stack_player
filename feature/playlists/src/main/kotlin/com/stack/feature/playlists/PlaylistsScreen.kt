package com.stack.feature.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.R
import com.stack.core.ui.components.EmptyState
import com.stack.core.ui.components.LoadingState
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.Playlist
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onBack: () -> Unit,
    onPlaylistClick: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.playlists)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.dispatch(PlaylistsIntent.ShowCreateDialog) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_playlist))
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            state.playlists.isEmpty() -> EmptyState(
                message = stringResource(R.string.no_items),
                icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(state.playlists, key = { it.id }) { playlist ->
                        PlaylistListItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist.id) },
                            onEdit = { viewModel.dispatch(PlaylistsIntent.ShowEditDialog(playlist)) },
                            onDelete = { viewModel.dispatch(PlaylistsIntent.DeletePlaylist(playlist.id)) }
                        )
                    }
                }
            }
        }
    }

    if (state.showEditor) {
        PlaylistEditorDialog(
            editingPlaylist = state.editingPlaylist,
            onDismiss = { viewModel.dispatch(PlaylistsIntent.DismissDialog) },
            onCreate = { name, desc -> viewModel.dispatch(PlaylistsIntent.CreatePlaylist(name, desc)) },
            onUpdate = { id, name, desc -> viewModel.dispatch(PlaylistsIntent.UpdatePlaylist(id, name, desc)) }
        )
    }
}

@Composable
private fun PlaylistListItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.PlaylistPlay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"} \u00b7 ${formatDuration(playlist.totalDuration)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onEdit) {
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) {
        String.format(Locale.US, "%dh %dm", hours, minutes)
    } else {
        String.format(Locale.US, "%dm", minutes)
    }
}
