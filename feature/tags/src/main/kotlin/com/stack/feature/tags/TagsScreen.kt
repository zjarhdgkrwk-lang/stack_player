package com.stack.feature.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
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
import com.stack.domain.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    onBack: () -> Unit,
    onTagClick: (Long) -> Unit,
    viewModel: TagsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tags)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.dispatch(TagsIntent.ShowCreateDialog) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_tag))
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            state.tags.isEmpty() -> EmptyState(
                message = stringResource(R.string.no_items),
                icon = Icons.AutoMirrored.Filled.Label,
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(state.tags, key = { it.id }) { tag ->
                        TagListItem(
                            tag = tag,
                            onClick = { onTagClick(tag.id) },
                            onEdit = { viewModel.dispatch(TagsIntent.ShowEditDialog(tag)) },
                            onDelete = { viewModel.dispatch(TagsIntent.DeleteTag(tag.id)) }
                        )
                    }
                }
            }
        }
    }

    if (state.showEditor) {
        TagEditorDialog(
            editingTag = state.editingTag,
            onDismiss = { viewModel.dispatch(TagsIntent.DismissDialog) },
            onCreate = { name, color -> viewModel.dispatch(TagsIntent.CreateTag(name, color)) },
            onUpdate = { id, name, color -> viewModel.dispatch(TagsIntent.UpdateTag(id, name, color)) }
        )
    }
}

@Composable
private fun TagListItem(
    tag: Tag,
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
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(tag.color))
        )

        Spacer(modifier = Modifier.width(Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge
            )
            if (tag.isSystem) {
                Text(
                    text = "System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!tag.isSystem) {
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
}
