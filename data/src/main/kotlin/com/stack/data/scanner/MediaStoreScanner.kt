package com.stack.data.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import com.stack.core.logging.Logger
import com.stack.data.local.db.entity.TrackEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "MediaStoreScanner"

        private val AUDIO_EXTENSIONS = setOf(
            "mp3", "flac", "m4a", "ogg", "opus", "wav", "aac", "wma", "aiff"
        )

        private val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )
    }

    data class ScannedFile(
        val uri: Uri,
        val fileName: String,
        val size: Long,
        val lastModified: Long,
        val parentUri: Uri
    )

    /**
     * Recursively discover all audio files under the given SAF tree URI.
     */
    suspend fun discoverAudioFiles(treeUri: Uri): List<ScannedFile> {
        val results = mutableListOf<ScannedFile>()
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        traverseDirectory(treeUri, docId, results)
        return results
    }

    private suspend fun traverseDirectory(
        treeUri: Uri,
        parentDocId: String,
        results: MutableList<ScannedFile>
    ) {
        coroutineContext.ensureActive()

        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentDocId)

        context.contentResolver.query(
            childrenUri,
            DOCUMENT_PROJECTION,
            null,
            null,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
            val modifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (cursor.moveToNext()) {
                coroutineContext.ensureActive()

                val docId = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: continue
                val mimeType = cursor.getString(mimeIndex) ?: ""
                val size = cursor.getLong(sizeIndex)
                val lastModified = cursor.getLong(modifiedIndex)

                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    traverseDirectory(treeUri, docId, results)
                } else if (isAudioFile(name)) {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    results.add(
                        ScannedFile(
                            uri = fileUri,
                            fileName = name,
                            size = size,
                            lastModified = lastModified,
                            parentUri = parentUri
                        )
                    )
                }
            }
        }
    }

    /**
     * Extract metadata from an audio file URI using MediaMetadataRetriever.
     * Returns a TrackEntity with extracted fields, or a minimal fallback on failure.
     */
    fun extractMetadata(
        file: ScannedFile,
        sourceFolderId: Long?,
        relativePath: String?
    ): TrackEntity {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: getNameWithoutExtension(file.fileName)

            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.split("/")?.firstOrNull()?.trim()?.toIntOrNull()

            val discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                ?.split("/")?.firstOrNull()?.trim()?.toIntOrNull()

            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?.toIntOrNull()

            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()?.let { it / 1000 } // convert bps to kbps

            val sampleRate = retriever.extractMetadata(24) // METADATA_KEY_SAMPLERATE = 24 (API 28+ constant)
                ?.toIntOrNull()

            val now = System.currentTimeMillis()

            TrackEntity(
                contentUri = file.uri.toString(),
                title = title,
                artist = artist,
                album = album,
                albumArtist = albumArtist,
                albumArtUri = null, // Album art URI handled separately via content URI
                duration = duration,
                trackNumber = trackNumber,
                discNumber = discNumber,
                year = year,
                genre = genre,
                size = file.size,
                bitrate = bitrate,
                sampleRate = sampleRate,
                dateAdded = now,
                dateModified = file.lastModified,
                relativePath = relativePath,
                fileName = file.fileName,
                status = "ACTIVE",
                sourceFolderId = sourceFolderId
            )
        } catch (e: Exception) {
            Logger.w(TAG, "Metadata extraction failed for ${file.fileName}", e)
            // Fallback: create track with minimum fields
            val now = System.currentTimeMillis()
            TrackEntity(
                contentUri = file.uri.toString(),
                title = getNameWithoutExtension(file.fileName),
                artist = null,
                album = null,
                albumArtist = null,
                albumArtUri = null,
                duration = 0L,
                trackNumber = null,
                discNumber = null,
                year = null,
                genre = null,
                size = file.size,
                bitrate = null,
                sampleRate = null,
                dateAdded = now,
                dateModified = file.lastModified,
                relativePath = relativePath,
                fileName = file.fileName,
                status = "ACTIVE",
                sourceFolderId = sourceFolderId
            )
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Build a relative path string from the SAF document URI for display.
     */
    fun buildRelativePath(treeUri: Uri, documentUri: Uri): String? {
        return try {
            val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
            val docId = DocumentsContract.getDocumentId(documentUri)
            if (docId.startsWith(treeDocId)) {
                docId.removePrefix(treeDocId).trimStart('/', ':')
            } else {
                docId.substringAfter(':', "")
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isAudioFile(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in AUDIO_EXTENSIONS
    }

    private fun getNameWithoutExtension(fileName: String): String {
        return fileName.substringBeforeLast('.')
    }
}
