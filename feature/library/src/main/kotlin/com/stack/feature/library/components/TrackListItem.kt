package com.stack.feature.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stack.core.R
import com.stack.core.ui.components.ArtworkImage
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.Track
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(
    track: Track,
    onClick: () -> Unit,
    onAddToPlaylist: ((Track) -> Unit)? = null,
    onAssignTag: ((Track) -> Unit)? = null,
    onPlayNext: ((Track) -> Unit)? = null,
    onToggleFavorite: ((Track) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (onAddToPlaylist != null || onAssignTag != null || onPlayNext != null || onToggleFavorite != null) {
                        showMenu = true
                    }
                }
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkImage(artworkUri = track.albumArtUri, size = 48.dp)

        Spacer(modifier = Modifier.width(Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${track.displayArtist} - ${track.displayAlbum}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (onToggleFavorite != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.favorite)) },
                        onClick = {
                            onToggleFavorite(track)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                    )
                }
                if (onPlayNext != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_next)) },
                        onClick = {
                            onPlayNext(track)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.SkipNext, contentDescription = null) }
                    )
                }
                if (onAddToPlaylist != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_to_playlist)) },
                        onClick = {
                            onAddToPlaylist(track)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) }
                    )
                }
                if (onAssignTag != null) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.assign_tag)) },
                        onClick = {
                            onAssignTag(track)
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) }
                    )
                }
            }
        }

        Text(
            text = formatTrackDuration(track.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTrackDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
