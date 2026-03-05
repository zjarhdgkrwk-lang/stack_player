package com.stack.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import com.stack.core.ui.components.EmptyState
import com.stack.core.ui.components.LoadingState
import com.stack.core.ui.theme.Spacing
import com.stack.domain.model.Track
import com.stack.feature.library.components.TrackListItem

@Composable
fun FoldersTab(
    tracks: List<Track>,
    isLoading: Boolean,
    onTrackClick: (Track, List<Track>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        LoadingState(modifier = modifier.fillMaxSize())
        return
    }

    if (tracks.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.no_folders),
            icon = Icons.Default.Folder,
            modifier = modifier
        )
        return
    }

    // Group tracks by folder
    val folderMap = remember(tracks) {
        tracks.groupBy { it.relativePath ?: "" }
    }

    var currentPath by remember { mutableStateOf("") }

    // Get subfolders and files at current path
    val (subfolders, currentTracks) = remember(currentPath, folderMap) {
        val folders = mutableSetOf<String>()
        val tracksInFolder = mutableListOf<Track>()

        folderMap.forEach { (path, pathTracks) ->
            if (currentPath.isEmpty()) {
                val topLevel = path.split("/").firstOrNull { it.isNotEmpty() }
                if (topLevel != null) folders.add(topLevel)
                if (path.isEmpty()) tracksInFolder.addAll(pathTracks)
            } else {
                if (path == currentPath) {
                    tracksInFolder.addAll(pathTracks)
                } else if (path.startsWith("$currentPath/")) {
                    val remainder = path.removePrefix("$currentPath/")
                    val nextPart = remainder.split("/").firstOrNull { it.isNotEmpty() }
                    if (nextPart != null) folders.add(nextPart)
                }
            }
        }

        folders.sorted() to tracksInFolder
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Breadcrumb
        if (currentPath.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val parts = currentPath.split("/")
                        currentPath = parts.dropLast(1).joinToString("/")
                    }
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = ".. / $currentPath",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(subfolders) { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentPath = if (currentPath.isEmpty()) folder
                            else "$currentPath/$folder"
                        }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = folder,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            items(currentTracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    onClick = {
                        val index = currentTracks.indexOf(track)
                        onTrackClick(track, currentTracks, index)
                    }
                )
            }
        }
    }
}
