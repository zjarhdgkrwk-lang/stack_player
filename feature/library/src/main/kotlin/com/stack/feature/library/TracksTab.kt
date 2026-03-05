package com.stack.feature.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.R
import com.stack.core.ui.components.EmptyState
import com.stack.core.ui.components.LoadingState
import com.stack.domain.model.Track
import com.stack.feature.library.components.TrackListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksTab(
    onTrackClick: (Track, List<Track>, Int) -> Unit,
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    onClick = {
                        val index = state.tracks.indexOf(track)
                        onTrackClick(track, state.tracks, index)
                    }
                )
            }
        }
    }
}
