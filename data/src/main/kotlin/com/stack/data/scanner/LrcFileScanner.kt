package com.stack.data.scanner

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.stack.core.logging.Logger
import com.stack.data.local.db.entity.LyricsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class LrcFileScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LrcFileScanner"
    }

    /**
     * For a given audio file, check the same SAF directory for a matching .lrc file.
     * Returns a LyricsEntity if found, null otherwise.
     */
    suspend fun findLrcForTrack(
        parentUri: Uri,
        treeUri: Uri,
        audioFileName: String,
        trackId: Long
    ): LyricsEntity? {
        coroutineContext.ensureActive()

        val nameWithoutExt = audioFileName.substringBeforeLast('.')
        val lrcFileName = "$nameWithoutExt.lrc"

        return try {
            val parentDocId = DocumentsContract.getDocumentId(parentUri)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)

            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: continue
                    if (name.equals(lrcFileName, ignoreCase = true)) {
                        val docId = cursor.getString(idIndex)
                        val lrcUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        val content = readLrcContent(lrcUri) ?: return@use null
                        val syncType = if (containsTimestamps(content)) "SYNCED" else "PLAIN"
                        return@use LyricsEntity(
                            trackId = trackId,
                            content = content,
                            syncType = syncType,
                            source = "LRC_FILE",
                            lrcFilePath = lrcUri.toString(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                }
                null
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to scan for LRC file: $lrcFileName", e)
            null
        }
    }

    private fun readLrcContent(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Failed to read LRC file: $uri", e)
            null
        }
    }

    private fun containsTimestamps(content: String): Boolean {
        // LRC timestamp pattern: [mm:ss.xx] or [mm:ss.xxx]
        val pattern = Regex("""\[\d{2}:\d{2}\.\d{2,3}]""")
        return pattern.containsMatchIn(content)
    }
}
