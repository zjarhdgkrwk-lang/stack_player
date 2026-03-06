package com.stack.feature.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stack.core.R
import com.stack.domain.model.Playlist

@Composable
fun PlaylistEditorDialog(
    editingPlaylist: Playlist?,
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit,
    onUpdate: (Long, String, String?) -> Unit
) {
    var name by remember(editingPlaylist) { mutableStateOf(editingPlaylist?.name ?: "") }
    var description by remember(editingPlaylist) { mutableStateOf(editingPlaylist?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (editingPlaylist != null) R.string.edit_playlist else R.string.create_playlist
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.playlist_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.playlist_description)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val desc = description.trim().ifBlank { null }
                        if (editingPlaylist != null) {
                            onUpdate(editingPlaylist.id, name.trim(), desc)
                        } else {
                            onCreate(name.trim(), desc)
                        }
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
