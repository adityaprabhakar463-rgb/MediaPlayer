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
        Artist(ArtistId("artist-nova"), "Nova Vale"),
        Artist(ArtistId("artist-echo"), "Echo Kaspar"),
        Artist(ArtistId("artist-lyra"), "Lyra Fenn"),
        Artist(ArtistId("artist-orion"), "Orion Marlow"),
        Artist(ArtistId("artist-solene"), "Solène Dray"),
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
            "album-pulse", "Stellar Pulse", "artist-nova", 0x2F6FEF,
            listOf(
                "Ignition Waltz" to 214_000L,
                "First Light" to 187_500L,
                "Corona Drift" to 246_000L,
                "Solar Wind Sway" to 221_000L,
            ),
        ),
        SeedAlbum(
            "album-singularity", "Singularity", "artist-nova", 0x7773C9,
            listOf(
                "Event Horizon" to 208_000L,
                "Blue Shift" to 249_500L,
                "Escape Velocity" to 187_000L,
            ),
        ),
        SeedAlbum(
            "album-tidlocke", "Tidal Lock", "artist-echo", 0x3F51B5,
            listOf(
                "Same Face Forever" to 236_000L,
                "Gravity Well" to 201_400L,
                "Meridian Hum" to 178_900L,
                "Orbit Decay" to 258_200L,
            ),
        ),
        SeedAlbum(
            "album-mist", "Nebula Mist", "artist-echo", 0x8BAFCB,
            listOf(
                "Dust Lane" to 222_300L,
                "Hydrogen Bloom" to 195_000L,
                "Star Nursery" to 233_800L,
            ),
        ),
        SeedAlbum(
            "album-crystal", "Crystal Coma", "artist-lyra", 0x9C27B0,
            listOf(
                "Silent Aurora" to 241_000L,
                "Frost Giant" to 210_650L,
                "Mirror Moons" to 186_200L,
            ),
        ),
        SeedAlbum(
            "album-frost", "Frozen Halo", "artist-orion", 0x00BCD4,
            listOf(
                "Comet Tail" to 197_400L,
                "Perihelion" to 224_100L,
                "Ice Field" to 213_700L,
                "Long Night" to 262_000L,
            ),
        ),
        SeedAlbum(
            "album-violetdusk", "Violet Dusk", "artist-solene", 0x7E57C2,
            listOf(
                "Dusk Dancer" to 228_000L,
                "Twilight Lantern" to 201_300L,
                "Slow Collapse" to 254_600L,
            ),
        ),
        SeedAlbum(
            "album-ashes", "Ashes of Giants", "artist-solene", 0xEF6C00,
            listOf(
                "Red Giant Rhyme" to 219_000L,
                "Nova Ash" to 186_400L,
                "White Dwarf Lullaby" to 247_200L,
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
                    artistName = artistSeeds.first { it.id == album.artistId }.name,
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
                artistName = artistSeeds.first { a -> a.id == album.artistId }.name,
                colorSeed = album.colorSeed,
                trackIds = album.tracks.indices.map { TrackId("${album.id}-${it + 1}") },
            )
        }
    }

    val artists: List<Artist> by lazy {
        artists.map { artist ->
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
