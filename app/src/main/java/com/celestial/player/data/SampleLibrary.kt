package com.celestial.player.data

import com.celestial.player.domain.model.Album
import com.celestial.player.domain.model.AlbumId
import com.celestial.player.domain.model.Artist
import com.celestial.player.domain.model.ArtistId
import com.celestial.player.domain.model.Playlist
import com.celestial.player.domain.model.PlaylistId
import com.celestial.player.domain.model.Track
import com.celestial.player.domain.model.TrackId

object SampleLibrary {

    private val artistSeeds = listOf(
        Artist(ArtistId("artist-nebula-pulse"), "Nebula Pulse"),
        Artist(ArtistId("artist-otto-tao"), "OttoTAO"),
        Artist(ArtistId("artist-lunaress"), "Lunaress"),
        Artist(ArtistId("artist-dean-evenson"), "Dean Evenson"),
        Artist(ArtistId("artist-tutara-peak"), "Tutara Peak"),
        Artist(ArtistId("artist-blue-tree-audio"), "BlueTreeAudio"),
        Artist(ArtistId("artist-eddie-edditter"), "eddie edditter"),
    )

    private data class SeedAlbum(
        val id: String,
        val title: String,
        val artistId: String,
        val colorSeed: Int,
        val tracks: List<Pair<String, Long>>,
    )

    private val seedAlbums = listOf(
        SeedAlbum(
            "album-zero-gravity", "Zero Gravity", "artist-nebula-pulse", 0x3F51B5,
            listOf(
                "Hibernation Chamber" to 239_000L,
                "Lost Transmission" to 233_000L,
                "Orbital Lullaby" to 227_000L,
                "Echoes Beyond Neptune" to 240_000L,
            ),
        ),
        SeedAlbum(
            "album-tao", "Meditation in the Tao", "artist-otto-tao", 0x8BAFCB,
            listOf(
                "The Stillness Between the Stars" to 240_000L,
                "Whispers of the Tao" to 240_000L,
                "Emptiness of the Mountain" to 239_000L,
            ),
        ),
        SeedAlbum(
            "album-celestial-drift", "Celestial Drift", "artist-lunaress", 0x9C27B0,
            listOf(
                "Stellar Veil" to 240_000L,
                "Starlight Fade" to 137_000L,
                "Nebula Nights" to 147_000L,
                "Galactic Haze" to 220_000L,
            ),
        ),
        SeedAlbum(
            "album-dream-space", "Dream Space", "artist-dean-evenson", 0x00BCD4,
            listOf(
                "Touching Infinity" to 438_000L,
                "Inner Light" to 339_000L,
                "Dream Space" to 333_000L,
            ),
        ),
        SeedAlbum(
            "album-nescient-mind", "Of the Nescient Mind", "artist-tutara-peak", 0x7773C9,
            listOf(
                "Gold In Autumn" to 294_000L,
                "Orbit w/ Winifred" to 227_000L,
                "Float" to 284_000L,
                "Wind Atlas w/ Flaws" to 241_000L,
            ),
        ),
        SeedAlbum(
            "album-chill-lounge", "Chill Lounge", "artist-blue-tree-audio", 0x2F6FEF,
            listOf(
                "Lounge" to 224_000L,
                "Relaxing" to 246_000L,
                "Deep Relax" to 480_000L,
            ),
        ),
        SeedAlbum(
            "album-centauria", "Centauria Skyline", "artist-eddie-edditter", 0x7E57C2,
            listOf(
                "044B Adventures" to 127_000L,
                "Bossa di Orione" to 137_000L,
                "OKB Retrofuturistic Version" to 199_000L,
                "Les Pionniers de l’Espace" to 167_000L,
            ),
        ),
        SeedAlbum(
            "album-by-yourself", "By Yourself, Boy", "artist-tutara-peak", 0xEF6C00,
            listOf(
                "Crop Circles" to 170_000L,
                "Telescope" to 185_000L,
                "Picture of Me" to 214_000L,
                "Bonfire" to 218_000L,
            ),
        ),
    )

    val tracks: List<Track> by lazy {
        seedAlbums.flatMap { album ->
            album.tracks.mapIndexed { index, (title, duration) ->
                Track(
                    id = TrackId("${album.id}-${index + 1}"),
                    title = title,
                    artistId = ArtistId(album.artistId),
                    artistName = artistSeeds.first { it.id.value == album.artistId }.name,
                    albumId = AlbumId(album.id),
                    albumTitle = album.title,
                    trackIndex = index + 1,
                    durationMs = duration,
                )
            }
        }
    }

    val albums: List<Album> by lazy {
        seedAlbums.map { album ->
            Album(
                id = AlbumId(album.id),
                title = album.title,
                artistId = ArtistId(album.artistId),
                artistName = artistSeeds.first { a -> a.id.value == album.artistId }.name,
                colorSeed = album.colorSeed,
                trackIds = album.tracks.indices.map { TrackId("${album.id}-${it + 1}") },
            )
        }
    }

    val artists: List<Artist> by lazy {
        artistSeeds.map { artist ->
            val artistAlbums = albums.filter { it.artistId == artist.id }
            Artist(
                id = artist.id,
                name = artist.name,
                albumIds = artistAlbums.map { it.id },
                trackIds = artistAlbums.flatMap { it.trackIds },
            )
        }
    }

    private fun playlist(id: String, name: String, trackIds: List<TrackId>) =
        Playlist(PlaylistId(id), name, trackIds)

    val playlists: List<Playlist> by lazy {
        val t = tracks
        listOf(
            playlist("pl-1", "Night Drive", t.slice(4..8).map { it.id }),
            playlist("pl-2", "Deep Focus", t.filterIndexed { i, _ -> i % 3 == 0 }.map { it.id }),
            playlist("pl-3", "Slow Orbit", t.slice(0..5).map { it.id }),
            playlist("pl-4", "Dawn Commute", t.slice(9..14).map { it.id }),
            playlist("pl-5", "Starfield Study", t.slice(15..21).map { it.id }),
        )
    }

    fun trackById(id: TrackId): Track? = tracks.firstOrNull { it.id == id }
}
