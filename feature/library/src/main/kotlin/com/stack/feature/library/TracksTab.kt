package com.stack.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.R
import com.stack.core.ui.components.EmptyState
import com.stack.core.ui.components.LoadingState
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.Track
import com.stack.feature.library.components.AddToPlaylistDialog
import com.stack.feature.library.components.AssignTagDialog
import com.stack.feature.library.components.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksTab(
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    onPlayNext: ((Track) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: TracksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingState(modifier = modifier.fillMaxSize())
        return
    }

    if (state.tracks.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.no_tracks),
            icon = Icons.Default.MusicNote,
            modifier = modifier
        )
        return
    }

    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = { viewModel.dispatch(TracksIntent.Refresh) },
        modifier = modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tag filter chips
            if (state.allTags.isNotEmpty()) {
                TagFilterRow(
                    tags = state.allTags,
                    selectedTagIds = state.selectedTagIds,
                    filterMode = state.tagFilterMode,
                    onToggleTag = { viewModel.dispatch(TracksIntent.ToggleTagFilter(it)) },
                    onToggleMode = {
                        val newMode = if (state.tagFilterMode == TagFilterMode.AND) TagFilterMode.OR else TagFilterMode.AND
                        viewModel.dispatch(TracksIntent.SetTagFilterMode(newMode))
                    },
                    onClear = { viewModel.dispatch(TracksIntent.ClearTagFilter) }
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.filteredTracks, key = { it.id }) { track ->
                    TrackListItem(
                        track = track,
                        onClick = {
                            val index = state.filteredTracks.indexOf(track)
                            onTrackClick(track, state.filteredTracks, index)
                        },
                        onAddToPlaylist = { viewModel.dispatch(TracksIntent.ShowPlaylistPicker(it)) },
                        onAssignTag = { viewModel.dispatch(TracksIntent.ShowTagAssigner(it)) },
                        onPlayNext = onPlayNext,
                        onToggleFavorite = { viewModel.dispatch(TracksIntent.ToggleFavorite(it)) }
                    )
                }
            }
        }
    }

    if (state.showPlaylistPicker) {
        AddToPlaylistDialog(
            playlists = state.playlists,
            onDismiss = { viewModel.dispatch(TracksIntent.DismissPlaylistPicker) },
            onPlaylistSelected = { viewModel.dispatch(TracksIntent.AddToPlaylist(it)) }
        )
    }

    if (state.showTagAssigner) {
        AssignTagDialog(
            tags = state.allTags,
            assignedTagIds = state.contextTrackTagIds,
            onDismiss = { viewModel.dispatch(TracksIntent.DismissTagAssigner) },
            onToggleTag = { viewModel.dispatch(TracksIntent.ToggleTrackTag(it)) }
        )
    }
}

@Composable
private fun TagFilterRow(
    tags: List<com.stack.domain.model.Tag>,
    selectedTagIds: Set<Long>,
    filterMode: TagFilterMode,
    onToggleTag: (Long) -> Unit,
    onToggleMode: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { tag ->
                FilterChip(
                    selected = tag.id in selectedTagIds,
                    onClick = { onToggleTag(tag.id) },
                    label = { Text(tag.name, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(tag.color))
                        )
                    }
                )
            }
        }

        if (selectedTagIds.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onToggleMode) {
                    Text(
                        text = if (filterMode == TagFilterMode.AND) {
                            stringResource(R.string.tag_filter_and)
                        } else {
                            stringResource(R.string.tag_filter_or)
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                TextButton(onClick = onClear) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
