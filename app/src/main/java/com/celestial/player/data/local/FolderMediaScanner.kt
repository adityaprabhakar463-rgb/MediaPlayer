package com.celestial.player.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.celestial.player.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale

/**
 * Recursively indexes audio files inside a Storage Access Framework tree.
 *
 * The scanner only reads document metadata and embedded tags; it never copies audio. Embedded
 * album art is extracted into app-private storage so list rendering and the media notification can
 * reuse it without re-reading every file.
 */
class FolderMediaScanner(private val context: Context) {

    data class ScanResult(
        val tracks: List<TrackEntity>,
        val rootName: String?,
    )

    suspend fun displayName(treeUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val rootId = DocumentsContract.getTreeDocumentId(treeUri)
            queryDocumentName(treeUri, rootId)
        }.getOrNull()
    }

    /** Deletes extracted artwork when the user removes the library folder. */
    fun clearArtworkCache() {
        runCatching {
            File(context.filesDir, "artwork").listFiles()?.forEach { it.delete() }
        }
    }

    suspend fun scan(
        treeUri: Uri,
        onProgress: (scannedFiles: Int) -> Unit = {},
    ): ScanResult = withContext(Dispatchers.IO) {
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootName = queryDocumentName(treeUri, rootId)
        val artworkDir = File(context.filesDir, "artwork").apply { mkdirs() }
        val audioFiles = mutableListOf<AudioFile>()
        val visited = mutableSetOf<String>()
        val pending = ArrayDeque<Pair<String, String>>()
        pending.addLast(rootId to "")

        while (pending.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val (documentId, relativePath) = pending.removeFirst()
            if (!visited.add(documentId)) continue

            try {
                collectChildren(treeUri, documentId, relativePath, pending, audioFiles)
            } catch (error: Exception) {
                // A revoked grant or unplugged volume must fail the scan instead of wiping the
                // indexed library with an empty result.
                if (documentId == rootId) {
                    throw IllegalStateException("Could not read the selected folder", error)
                }
                Log.w(TAG, "Could not list $relativePath", error)
            }
        }

        val tracks = ArrayList<TrackEntity>(audioFiles.size)
        val liveArtwork = HashSet<String>()
        audioFiles.forEachIndexed { index, file ->
            currentCoroutineContext().ensureActive()
            val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, file.documentId)
            val track = readTrack(uri, file, artworkDir, liveArtwork)
            if (track != null) tracks += track
            if ((index + 1) % PROGRESS_INTERVAL == 0 || index == audioFiles.lastIndex) {
                onProgress(index + 1)
            }
        }

        // Remove extracted artwork that no longer belongs to a present track.
        artworkDir.listFiles()?.forEach { cached ->
            if (cached.name !in liveArtwork) cached.delete()
        }

        ScanResult(
            tracks = tracks,
            rootName = rootName,
        )
    }

    /** Lists one directory. Throws when the provider denies or fails the query. */
    private fun collectChildren(
        treeUri: Uri,
        documentId: String,
        relativePath: String,
        pending: ArrayDeque<Pair<String, String>>,
        audioFiles: MutableList<AudioFile>,
    ) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
        )
        val cursor = context.contentResolver.query(childrenUri, projection, null, null, null)
            ?: throw IllegalStateException("Provider returned no cursor for $relativePath")
        cursor.use {
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val modifiedIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

            while (cursor.moveToNext()) {
                val childId = cursor.getString(idIndex) ?: continue
                val name = cursor.getString(nameIndex).orEmpty()
                val mime = cursor.getString(mimeIndex).orEmpty()
                val childPath = if (relativePath.isEmpty()) name else "$relativePath/$name"
                when {
                    mime == DocumentsContract.Document.MIME_TYPE_DIR -> {
                        if (!name.startsWith(".")) pending.addLast(childId to childPath)
                    }

                    isAudio(name = name, mimeType = mime) -> {
                        if (!name.startsWith(".")) {
                            audioFiles += AudioFile(
                                documentId = childId,
                                displayName = name,
                                mimeType = mime,
                                relativePath = relativePath,
                                lastModifiedMs = cursor.getLongOrNull(modifiedIndex) ?: 0L,
                                sizeBytes = cursor.getLongOrNull(sizeIndex) ?: 0L,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun readTrack(
        uri: Uri,
        file: AudioFile,
        artworkDir: File,
        liveArtwork: MutableSet<String>,
    ): TrackEntity? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val titleTag = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artistTag = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            val albumTag = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val trackNumber = retriever.metadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                ?.substringBefore('/')?.trim()?.toIntOrNull() ?: 0

            val baseName = file.displayName.substringBeforeLast('.')
            val parsed = parseFileName(baseName)
            val title = titleTag ?: parsed.title
            val artist = artistTag ?: parsed.artist ?: UNKNOWN_ARTIST
            val folderName = file.relativePath.substringAfterLast('/').ifBlank { null }
            val album = albumTag ?: folderName ?: UNKNOWN_ALBUM
            val artworkPath = retriever.embeddedPicture?.let { picture ->
                saveArtwork(picture, artworkDir, liveArtwork)
            }

            val trackId = uri.toString()
            TrackEntity(
                id = trackId,
                title = title,
                artistId = stableId("artist", artist),
                artistName = artist,
                albumId = stableId("album", artist, album),
                albumTitle = album,
                trackIndex = if (trackNumber > 0) trackNumber else parsed.trackNumber,
                durationMs = duration,
                mediaUri = trackId,
                folderPath = file.relativePath,
                artworkPath = artworkPath,
                mimeType = file.mimeType,
                dateAddedMs = file.lastModifiedMs,
                sizeBytes = file.sizeBytes,
            )
        } catch (error: Exception) {
            Log.w(TAG, "Unreadable tags, indexing filename fallback: ${file.displayName}", error)
            fallbackTrack(uri, file)
        } finally {
            runCatching { retriever.release() }
        }
    }

    /** Still lists a file when its tags cannot be read, so a playable file is never silently lost. */
    private fun fallbackTrack(uri: Uri, file: AudioFile): TrackEntity {
        val baseName = file.displayName.substringBeforeLast('.')
        val parsed = parseFileName(baseName)
        val artist = parsed.artist ?: UNKNOWN_ARTIST
        val folderName = file.relativePath.substringAfterLast('/').ifBlank { null }
        val album = folderName ?: UNKNOWN_ALBUM
        return TrackEntity(
            id = uri.toString(),
            title = parsed.title,
            artistId = stableId("artist", artist),
            artistName = artist,
            albumId = stableId("album", artist, album),
            albumTitle = album,
            trackIndex = parsed.trackNumber,
            durationMs = 0L,
            mediaUri = uri.toString(),
            folderPath = file.relativePath,
            artworkPath = null,
            mimeType = file.mimeType,
            dateAddedMs = file.lastModifiedMs,
            sizeBytes = file.sizeBytes,
        )
    }

    private fun saveArtwork(
        bytes: ByteArray,
        artworkDir: File,
        liveArtwork: MutableSet<String>,
    ): String? = runCatching {
        val hash = sha1(bytes)
        val fileName = "$hash.jpg"
        liveArtwork += fileName
        val target = File(artworkDir, fileName)
        if (!target.exists() || target.length() == 0L) {
            FileOutputStream(target).use { it.write(bytes) }
        }
        target.absolutePath
    }.getOrNull()

    private fun queryDocumentName(treeUri: Uri, documentId: String): String? {
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return context.contentResolver.query(
            documentUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    private fun MediaMetadataRetriever.metadata(key: Int): String? =
        extractMetadata(key)?.trim()?.takeIf { it.isNotEmpty() }

    private fun android.database.Cursor.getLongOrNull(index: Int): Long? =
        if (index >= 0 && !isNull(index)) getLong(index) else null

    /** "Artist - Title.mp3" or "01 - Artist - Title.mp3" fallback. Never overrides real tags. */
    private fun parseFileName(baseName: String): ParsedName {
        val leadingNumber = Regex("^(\\d{1,3})[\\s._-]+").find(baseName)
        val trackNumber = leadingNumber?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val withoutTrackNumber = leadingNumber
            ?.let { baseName.removeRange(it.range) }
            ?: baseName
        val parts = withoutTrackNumber.split(" - ", limit = 2)
        return if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
            ParsedName(artist = parts[0].trim(), title = parts[1].trim(), trackNumber = trackNumber)
        } else {
            ParsedName(
                artist = null,
                title = withoutTrackNumber.ifBlank { baseName },
                trackNumber = trackNumber,
            )
        }
    }

    private fun stableId(prefix: String, vararg parts: String): String {
        val raw = parts.joinToString("\u0000").lowercase(Locale.ROOT).trim()
        return "$prefix-${sha1(raw.toByteArray()).take(16)}"
    }

    private fun sha1(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun isAudio(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("audio/")) return true
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return extension in AUDIO_EXTENSIONS
    }

    private data class AudioFile(
        val documentId: String,
        val displayName: String,
        val mimeType: String,
        val relativePath: String,
        val lastModifiedMs: Long,
        val sizeBytes: Long,
    )

    private data class ParsedName(
        val artist: String?,
        val title: String,
        val trackNumber: Int = 0,
    )

    private companion object {
        const val TAG = "FolderMediaScanner"
        const val PROGRESS_INTERVAL = 10
        const val UNKNOWN_ARTIST = "Unknown Artist"
        const val UNKNOWN_ALBUM = "Unknown Album"
        val AUDIO_EXTENSIONS = setOf(
            "mp3", "m4a", "m4b", "aac", "flac", "ogg", "oga", "opus",
            "wav", "wave", "wma", "mka", "aiff", "aif", "amr", "mid", "midi",
        )
    }
}
