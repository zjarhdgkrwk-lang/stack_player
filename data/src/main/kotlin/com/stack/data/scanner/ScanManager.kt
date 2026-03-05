package com.stack.data.scanner

import android.net.Uri
import com.stack.core.logging.Logger
import com.stack.core.util.CoroutineDispatchers
import com.stack.data.local.db.dao.AlbumDao
import com.stack.data.local.db.dao.ArtistDao
import com.stack.data.local.db.dao.LyricsDao
import com.stack.data.local.db.dao.SourceFolderDao
import com.stack.data.local.db.dao.TrackDao
import com.stack.data.local.db.entity.AlbumEntity
import com.stack.data.local.db.entity.ArtistEntity
import com.stack.data.local.db.entity.TrackEntity
import com.stack.domain.model.ScanProgress
import com.stack.domain.repository.ScanService
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanManager @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val mediaStoreScanner: MediaStoreScanner,
    private val lrcFileScanner: LrcFileScanner,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val lyricsDao: LyricsDao,
    private val sourceFolderDao: SourceFolderDao
) : ScanService {
    companion object {
        private const val TAG = "ScanManager"
    }

    private val scanMutex = Mutex()

    private val _scanProgress = MutableStateFlow(ScanProgress())
    override val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private var currentScanJob: Job? = null

    /**
     * Full scan: re-scans all registered source folders.
     * Marks missing tracks as GHOST.
     */
    override suspend fun fullScan() {
        scanMutex.withLock {
            withContext(dispatchers.io) {
                coroutineScope {
                    try {
                        _scanProgress.value = ScanProgress(isScanning = true)
                        Logger.i(TAG, "Starting full scan")

                        val folders = sourceFolderDao.getAllFolders().first()
                        if (folders.isEmpty()) {
                            Logger.i(TAG, "No source folders registered, skipping scan")
                            return@coroutineScope
                        }

                        // Phase 1: Discover all audio files
                        val allScannedFiles = mutableListOf<Pair<MediaStoreScanner.ScannedFile, Long?>>()
                        for (folder in folders) {
                            ensureActive()
                            val treeUri = Uri.parse(folder.treeUri)
                            val files = mediaStoreScanner.discoverAudioFiles(treeUri)
                            files.forEach { file -> allScannedFiles.add(file to folder.id) }
                            sourceFolderDao.updateLastScanAt(folder.id, System.currentTimeMillis())
                        }

                        val totalFiles = allScannedFiles.size
                        _scanProgress.value = ScanProgress(
                            totalFiles = totalFiles,
                            scannedFiles = 0,
                            isScanning = true
                        )
                        Logger.i(TAG, "Discovered $totalFiles audio files")

                        // Phase 2: Extract metadata and upsert tracks
                        val scannedContentUris = mutableSetOf<String>()
                        var scannedCount = 0

                        for ((file, folderId) in allScannedFiles) {
                            ensureActive()

                            val contentUri = file.uri.toString()
                            scannedContentUris.add(contentUri)

                            // Check if track already exists and hasn't changed
                            val existing = trackDao.getTrackByContentUri(contentUri)
                            if (existing != null &&
                                existing.size == file.size &&
                                existing.dateModified == file.lastModified
                            ) {
                                // Track unchanged — re-activate if GHOST
                                if (existing.status == "GHOST") {
                                    trackDao.updateTrackStatus(existing.id, "ACTIVE")
                                }
                                scannedCount++
                                _scanProgress.value = ScanProgress(
                                    totalFiles = totalFiles,
                                    scannedFiles = scannedCount,
                                    isScanning = true,
                                    currentFile = file.fileName
                                )
                                continue
                            }

                            // Build relative path from the first matching folder
                            val treeUri = Uri.parse(
                                sourceFolderDao.getFolderById(folderId ?: 0)?.treeUri ?: ""
                            )
                            val relativePath = mediaStoreScanner.buildRelativePath(treeUri, file.parentUri)

                            // Extract metadata
                            val trackEntity = mediaStoreScanner.extractMetadata(file, folderId, relativePath)

                            // Normalize album/artist
                            val albumId = normalizeAlbum(trackEntity)
                            val artistId = normalizeArtist(trackEntity)

                            if (existing != null) {
                                // Update existing track
                                trackDao.updateTrack(
                                    trackEntity.copy(
                                        id = existing.id,
                                        dateAdded = existing.dateAdded,
                                        albumId = albumId,
                                        artistId = artistId,
                                        status = "ACTIVE"
                                    )
                                )

                                // Scan for LRC file
                                scanLrcForTrack(existing.id, treeUri, file)
                            } else {
                                // Insert new track
                                val newId = trackDao.insertTrack(
                                    trackEntity.copy(
                                        albumId = albumId,
                                        artistId = artistId
                                    )
                                )

                                // Scan for LRC file
                                scanLrcForTrack(newId, treeUri, file)
                            }

                            scannedCount++
                            _scanProgress.value = ScanProgress(
                                totalFiles = totalFiles,
                                scannedFiles = scannedCount,
                                isScanning = true,
                                currentFile = file.fileName
                            )
                        }

                        // Phase 3: Ghost detection — mark tracks not found in scan
                        markGhostTracks(scannedContentUris)

                        Logger.i(TAG, "Full scan completed: $scannedCount tracks processed")
                    } finally {
                        _scanProgress.value = ScanProgress(isScanning = false)
                    }
                }
            }
        }
    }

    /**
     * Incremental scan: only scans files that are new or modified.
     * Runs the same pipeline but skips unchanged files efficiently.
     */
    override suspend fun incrementalScan() {
        // Incremental scan uses the same logic as full scan;
        // the existing-track check at the top of the loop makes it incremental.
        fullScan()
    }

    /**
     * Scan a single newly-added folder.
     */
    override suspend fun scanFolder(folderId: Long) {
        scanMutex.withLock {
            withContext(dispatchers.io) {
                coroutineScope {
                    try {
                        _scanProgress.value = ScanProgress(isScanning = true)

                        val folder = sourceFolderDao.getFolderById(folderId) ?: return@coroutineScope
                        val treeUri = Uri.parse(folder.treeUri)
                        val files = mediaStoreScanner.discoverAudioFiles(treeUri)

                        val totalFiles = files.size
                        _scanProgress.value = ScanProgress(
                            totalFiles = totalFiles,
                            scannedFiles = 0,
                            isScanning = true
                        )

                        var scannedCount = 0
                        for (file in files) {
                            ensureActive()

                            val contentUri = file.uri.toString()
                            val existing = trackDao.getTrackByContentUri(contentUri)

                            if (existing != null &&
                                existing.size == file.size &&
                                existing.dateModified == file.lastModified
                            ) {
                                if (existing.status == "GHOST") {
                                    trackDao.updateTrackStatus(existing.id, "ACTIVE")
                                }
                                scannedCount++
                                _scanProgress.value = ScanProgress(
                                    totalFiles = totalFiles,
                                    scannedFiles = scannedCount,
                                    isScanning = true,
                                    currentFile = file.fileName
                                )
                                continue
                            }

                            val relativePath = mediaStoreScanner.buildRelativePath(treeUri, file.parentUri)
                            val trackEntity = mediaStoreScanner.extractMetadata(file, folderId, relativePath)

                            val albumId = normalizeAlbum(trackEntity)
                            val artistId = normalizeArtist(trackEntity)

                            if (existing != null) {
                                trackDao.updateTrack(
                                    trackEntity.copy(
                                        id = existing.id,
                                        dateAdded = existing.dateAdded,
                                        albumId = albumId,
                                        artistId = artistId,
                                        status = "ACTIVE"
                                    )
                                )
                                scanLrcForTrack(existing.id, treeUri, file)
                            } else {
                                val newId = trackDao.insertTrack(
                                    trackEntity.copy(albumId = albumId, artistId = artistId)
                                )
                                scanLrcForTrack(newId, treeUri, file)
                            }

                            scannedCount++
                            _scanProgress.value = ScanProgress(
                                totalFiles = totalFiles,
                                scannedFiles = scannedCount,
                                isScanning = true,
                                currentFile = file.fileName
                            )
                        }

                        sourceFolderDao.updateLastScanAt(folderId, System.currentTimeMillis())
                        Logger.i(TAG, "Folder scan completed: $scannedCount tracks in ${folder.displayName}")
                    } finally {
                        _scanProgress.value = ScanProgress(isScanning = false)
                    }
                }
            }
        }
    }

    /**
     * Clean up GHOST tracks — delete them from the database.
     */
    override suspend fun cleanGhostTracks() {
        withContext(dispatchers.io) {
            val ghostTracks = trackDao.getTracksByStatus("GHOST").first()
            for (track in ghostTracks) {
                trackDao.deleteTrack(track.id)
            }
            Logger.i(TAG, "Cleaned ${ghostTracks.size} ghost tracks")
        }
    }

    override fun cancelScan() {
        currentScanJob?.cancel()
        currentScanJob = null
    }

    private suspend fun normalizeAlbum(trackEntity: TrackEntity): Long? {
        val albumName = trackEntity.album ?: return null
        val albumArtist = trackEntity.albumArtist ?: trackEntity.artist

        val existing = albumDao.getAlbumByNameAndArtist(albumName, albumArtist)
        return if (existing != null) {
            // Update track count
            albumDao.updateAlbum(
                existing.copy(
                    trackCount = existing.trackCount + 1,
                    totalDuration = existing.totalDuration + trackEntity.duration
                )
            )
            existing.id
        } else {
            albumDao.insertAlbum(
                AlbumEntity(
                    name = albumName,
                    artist = albumArtist,
                    albumArtUri = trackEntity.albumArtUri,
                    trackCount = 1,
                    totalDuration = trackEntity.duration,
                    year = trackEntity.year,
                    dateAdded = trackEntity.dateAdded
                )
            )
        }
    }

    private suspend fun normalizeArtist(trackEntity: TrackEntity): Long? {
        val artistName = trackEntity.artist ?: return null

        val existing = artistDao.getArtistByName(artistName)
        return if (existing != null) {
            artistDao.updateArtist(
                existing.copy(trackCount = existing.trackCount + 1)
            )
            existing.id
        } else {
            artistDao.insertArtist(
                ArtistEntity(
                    name = artistName,
                    albumCount = 0,
                    trackCount = 1
                )
            )
        }
    }

    private suspend fun markGhostTracks(scannedContentUris: Set<String>) {
        // Get all active tracks and mark those not found as GHOST
        val allActiveTracks = trackDao.getTracksByStatus("ACTIVE").first()
        var ghostCount = 0
        for (track in allActiveTracks) {
            if (track.contentUri !in scannedContentUris) {
                trackDao.updateTrackStatus(track.id, "GHOST")
                ghostCount++
            }
        }
        if (ghostCount > 0) {
            Logger.i(TAG, "Marked $ghostCount tracks as GHOST")
        }
    }

    private suspend fun scanLrcForTrack(
        trackId: Long,
        treeUri: Uri,
        file: MediaStoreScanner.ScannedFile
    ) {
        // Only scan if no lyrics exist yet
        if (lyricsDao.hasLyrics(trackId) > 0) return

        val lyricsEntity = lrcFileScanner.findLrcForTrack(
            parentUri = file.parentUri,
            treeUri = treeUri,
            audioFileName = file.fileName,
            trackId = trackId
        )
        if (lyricsEntity != null) {
            lyricsDao.insertLyrics(lyricsEntity)
            Logger.d(TAG, "Found LRC for track $trackId: ${file.fileName}")
        }
    }
}
