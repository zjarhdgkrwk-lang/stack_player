package com.stack.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.stack.core.R

@Composable
fun ArtworkImage(
    artworkUri: String?,
    contentDescription: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    if (artworkUri != null) {
        AsyncImage(
            model = artworkUri,
            contentDescription = contentDescription ?: stringResource(R.string.cd_album_art),
            modifier = modifier
                .size(size)
                .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = contentDescription ?: stringResource(R.string.cd_album_art),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
