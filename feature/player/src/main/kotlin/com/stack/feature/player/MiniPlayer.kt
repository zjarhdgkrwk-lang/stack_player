package com.stack.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stack.core.R
import com.stack.core.player.PlaybackState
import com.stack.core.ui.components.ArtworkImage
import com.stack.core.ui.theme.Spacing

@Composable
fun MiniPlayer(
    state: PlaybackState,
    isFavorite: Boolean = false,
    onTap: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: return
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset > swipeThreshold -> onSkipPrevious()
                            dragOffset < -swipeThreshold -> onSkipNext()
                        }
                        dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onHorizontalDrag = { _, amount -> dragOffset += amount }
                )
            },
        tonalElevation = 3.dp
    ) {
        Column {
            val progress = if (state.durationMs > 0) {
                (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
            } else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkImage(
                    artworkUri = track.albumArtUri,
                    size = 40.dp
                )

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

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_favorite_toggle),
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.cd_play_pause)
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.cd_skip_next)
                    )
                }
            }
        }
    }
}
