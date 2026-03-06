package com.stack.feature.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stack.core.R
import com.stack.domain.model.Tag

private val TAG_COLORS = listOf(
    Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8),
    Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
    Color(0xFF4FC3F7), Color(0xFF4DD0E1), Color(0xFF4DB6AC),
    Color(0xFF81C784), Color(0xFFAED581), Color(0xFFDCE775),
    Color(0xFFFFD54F), Color(0xFFFFB74D), Color(0xFFFF8A65),
    Color(0xFFA1887F), Color(0xFF90A4AE), Color(0xFFE0E0E0)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditorDialog(
    editingTag: Tag?,
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit,
    onUpdate: (Long, String, Int) -> Unit
) {
    var name by remember(editingTag) { mutableStateOf(editingTag?.name ?: "") }
    var selectedColor by remember(editingTag) {
        mutableIntStateOf(editingTag?.color ?: TAG_COLORS.first().toArgb())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (editingTag != null) R.string.edit_tag else R.string.create_tag)
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.tag_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.tag_color),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TAG_COLORS.forEach { color ->
                        val argb = color.toArgb()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (argb == selectedColor) {
                                        Modifier.border(
                                            3.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                                .clickable { selectedColor = argb }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        if (editingTag != null) {
                            onUpdate(editingTag.id, name.trim(), selectedColor)
                        } else {
                            onCreate(name.trim(), selectedColor)
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
