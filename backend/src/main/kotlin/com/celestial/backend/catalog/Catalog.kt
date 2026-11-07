package com.celestial.backend.catalog

import com.celestial.backend.api.AlbumDto
import com.celestial.backend.api.ArtistDto
import com.celestial.backend.api.CatalogSnapshot
import com.celestial.backend.api.TrackDto

/**
 * Curated Audius catalog used by the prototype backend.
 *
 * The catalog is deliberately small and manually approved. Each track keeps a stable Celestial id
 * (used by playlists and favourites) plus an Audius external id (used only for stream resolution).
 * Metadata comes from the Audius API; artwork URLs are cached here for future client artwork work.
 *
 * Audius content is licensed by the uploading artists. Stream URLs are resolved per play request
 * and never stored. See `docs/backend/06_AUDIUS_INTEGRATION_AND_STREAMING.md`.
 */
object Catalog {

    const val VERSION: Long = 2L

    private data class SeedTrack(
        val title: String,
        val durationMs: Long,
        val audiusId: String,
        val artworkUrl: String?,
    )

    private data class SeedAlbum(
        val id: String,
        val title: String,
        val artistId: String,
        val colorSeed: Int,
        val tracks: List<SeedTrack>,
    )

    private val artistNames = linkedMapOf(
        "artist-nebula-pulse" to "Nebula Pulse",
        "artist-otto-tao" to "OttoTAO",
        "artist-lunaress" to "Lunaress",
        "artist-dean-evenson" to "Dean Evenson",
        "artist-tutara-peak" to "Tutara Peak",
        "artist-blue-tree-audio" to "BlueTreeAudio",
        "artist-eddie-edditter" to "eddie edditter",
    )

    private val seedAlbums = listOf(
        SeedAlbum(
            "album-zero-gravity", "Zero Gravity", "artist-nebula-pulse", 0x3F51B5,
            listOf(
                SeedTrack(
                    "Hibernation Chamber", 239_000L, "dGON5PJ",
                    "https://val011.open-audio-validator.com/content/01JZ41W7GRTFHEZRPJB7ZZVG63/480x480.jpg",
                ),
                SeedTrack(
                    "Lost Transmission", 233_000L, "2PWPRN3",
                    "https://audius-creator-17.theblueprint.xyz/content/01JZ41W9KMPJA14VJPSQ4GEJQ7/480x480.jpg",
                ),
                SeedTrack(
                    "Orbital Lullaby", 227_000L, "WY99aBZ",
                    "https://val001.open-audio-validator.com/content/01JZ41W7YM0TKVJ065MNZMJZMJ/480x480.jpg",
                ),
                SeedTrack(
                    "Echoes Beyond Neptune", 240_000L, "73WMowW",
                    "https://val011.open-audio-validator.com/content/01JZ41WZWPG6ZT5JMKZ48WZPZ4/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-tao", "Meditation in the Tao", "artist-otto-tao", 0x8BAFCB,
            listOf(
                SeedTrack(
                    "The Stillness Between the Stars", 240_000L, "91Ax2Vk",
                    "https://validator.stuffisup.com/content/01JYGKJXMFVWSDE6E2KJ7BSV7N/480x480.jpg",
                ),
                SeedTrack(
                    "Whispers of the Tao", 240_000L, "mlGxoR5",
                    "https://audius-content-7.figment.io/content/01JYGKJXFJE59TTXGVDSHZXB2D/480x480.jpg",
                ),
                SeedTrack(
                    "Emptiness of the Mountain", 239_000L, "Mo3qE4Z",
                    "https://v.monophonic.digital/content/01JYGKJXMDAT7XW824HG4QHCFX/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-celestial-drift", "Celestial Drift", "artist-lunaress", 0x9C27B0,
            listOf(
                SeedTrack(
                    "Stellar Veil", 240_000L, "pl2wmkY",
                    "https://v.monophonic.digital/content/01K3X38M5Y1HPCP1KA312YM306/480x480.jpg",
                ),
                SeedTrack(
                    "Starlight Fade", 137_000L, "9O8dyQw",
                    "https://audius-creator-16.theblueprint.xyz/content/01K3X38M481QX5S4GCS67AWFKQ/480x480.jpg",
                ),
                SeedTrack(
                    "Nebula Nights", 147_000L, "mgvpdEv",
                    "https://creatornode.audius.co/content/01K3X37JJ5DRDK4FVV4TBG2FXA/480x480.jpg",
                ),
                SeedTrack(
                    "Galactic Haze", 220_000L, "B5zO4z9",
                    "https://v.monophonic.digital/content/01K3X37JX7M3PT0ET3R7FEZFAK/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-dream-space", "Dream Space", "artist-dean-evenson", 0x00BCD4,
            listOf(
                SeedTrack(
                    "Touching Infinity", 438_000L, "zzaz5",
                    "https://audius-creator-8.theblueprint.xyz/content/QmRV6GCkj3X1iwnoCEvX2isEFw8UY7o4RWq1Lb4tH4NMGe/480x480.jpg",
                ),
                SeedTrack(
                    "Inner Light", 339_000L, "0XbXV",
                    "https://val005.open-audio-validator.com/content/QmRV6GCkj3X1iwnoCEvX2isEFw8UY7o4RWq1Lb4tH4NMGe/480x480.jpg",
                ),
                SeedTrack(
                    "Dream Space", 333_000L, "vqPq6",
                    "https://v.monophonic.digital/content/QmRV6GCkj3X1iwnoCEvX2isEFw8UY7o4RWq1Lb4tH4NMGe/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-nescient-mind", "Of the Nescient Mind", "artist-tutara-peak", 0x7773C9,
            listOf(
                SeedTrack(
                    "Gold In Autumn", 294_000L, "QVP0VmR",
                    "https://audius-nodes.com/content/01HD0WEX4MGHWR5H9AV5D9NSC1/480x480.jpg",
                ),
                SeedTrack(
                    "Orbit w/ Winifred", 227_000L, "NY88j4",
                    "https://v.monophonic.digital/content/01HMB9A6C29Y3Z5T4XYQS7XHED/480x480.jpg",
                ),
                SeedTrack(
                    "Float", 284_000L, "y9EKBMw",
                    "https://v.monophonic.digital/content/01HPSZZMASHXZZB93MKM8ZAQTV/480x480.jpg",
                ),
                SeedTrack(
                    "Wind Atlas w/ Flaws", 241_000L, "N66gdk8",
                    "https://val010.open-audio-validator.com/content/01HFARBV8WMNSSXDV04YQ8507S/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-chill-lounge", "Chill Lounge", "artist-blue-tree-audio", 0x2F6FEF,
            listOf(
                SeedTrack(
                    "Lounge", 224_000L, "lvBPA",
                    "https://audius-nodes.com/content/QmNUWfkUwW22FQiqUfgvYug1CVfuuV4YGd2zBPQj5bwPf1/480x480.jpg",
                ),
                SeedTrack(
                    "Relaxing", 246_000L, "VQGWW",
                    "https://audius-content-5.figment.io/content/QmNUWfkUwW22FQiqUfgvYug1CVfuuV4YGd2zBPQj5bwPf1/480x480.jpg",
                ),
                SeedTrack(
                    "Deep Relax", 480_000L, "95VAa",
                    "https://audius-02.staked.cloud/content/QmNUWfkUwW22FQiqUfgvYug1CVfuuV4YGd2zBPQj5bwPf1/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-centauria", "Centauria Skyline", "artist-eddie-edditter", 0x7E57C2,
            listOf(
                SeedTrack(
                    "044B Adventures", 127_000L, "l4voKZO",
                    "https://audius-creator-12.theblueprint.xyz/content/baeaaaiqsebyb2ewj7mj2656xlt7tbul6s5b2sqvhc5bde4hbkqjo2oykcjndi/480x480.jpg",
                ),
                SeedTrack(
                    "Bossa di Orione", 137_000L, "83MZbEJ",
                    "https://v.audius.rickyrombo.com/content/baeaaaiqseda7a7q5hpdpco24xvkrqvj2hddvqtgcgmygidrwap5ewjnzldpky/480x480.jpg",
                ),
                SeedTrack(
                    "OKB Retrofuturistic Version", 199_000L, "NPW7ZRN",
                    "https://val008.open-audio-validator.com/content/baeaaaiqsebyb2ewj7mj2656xlt7tbul6s5b2sqvhc5bde4hbkqjo2oykcjndi/480x480.jpg",
                ),
                SeedTrack(
                    "Les Pionniers de l’Espace", 167_000L, "xy2WGG7",
                    "https://val008.open-audio-validator.com/content/baeaaaiqsebyb2ewj7mj2656xlt7tbul6s5b2sqvhc5bde4hbkqjo2oykcjndi/480x480.jpg",
                ),
            ),
        ),
        SeedAlbum(
            "album-by-yourself", "By Yourself, Boy", "artist-tutara-peak", 0xEF6C00,
            listOf(
                SeedTrack(
                    "Crop Circles", 170_000L, "BKJoW9J",
                    "https://v.monophonic.digital/content/01JCBG05R63CKWV8YTHBCVH97K/480x480.jpg",
                ),
                SeedTrack(
                    "Telescope", 185_000L, "P9A4W8k",
                    "https://v.monophonic.digital/content/01JCBG063TDAXBE8EQCA8YBYD1/480x480.jpg",
                ),
                SeedTrack(
                    "Picture of Me", 214_000L, "9bxJ8GO",
                    "https://cn2.mainnet.audiusindex.org/content/01JCBG062F5ECB9K2Z0GEGWA39/480x480.jpg",
                ),
                SeedTrack(
                    "Bonfire", 218_000L, "gvwrYbx",
                    "https://audius-creator-9.theblueprint.xyz/content/01JCBGA9EW8P5KJKV9GH15QQSA/480x480.jpg",
                ),
            ),
        ),
    )

    val tracks: List<TrackDto> = seedAlbums.flatMap { album ->
        album.tracks.mapIndexed { index, seed ->
            TrackDto(
                id = "${album.id}-${index + 1}",
                title = seed.title,
                artistId = album.artistId,
                artistName = artistNames.getValue(album.artistId),
                albumId = album.id,
                albumTitle = album.title,
                trackIndex = index + 1,
                durationMs = seed.durationMs,
                artworkUrl = seed.artworkUrl,
            )
        }
    }

    /** Celestial track id -> Audius external id. The only provider-specific data in the catalog. */
    val externalIds: Map<String, String> = seedAlbums.flatMap { album ->
        album.tracks.mapIndexed { index, seed -> "${album.id}-${index + 1}" to seed.audiusId }
    }.toMap()

    val albums: List<AlbumDto> = seedAlbums.map { album ->
        AlbumDto(
            id = album.id,
            title = album.title,
            artistId = album.artistId,
            artistName = artistNames.getValue(album.artistId),
            colorSeed = album.colorSeed,
            trackIds = album.tracks.indices.map { "${album.id}-${it + 1}" },
        )
    }

    val artists: List<ArtistDto> = artistNames.map { (artistId, name) ->
        val artistAlbums = albums.filter { it.artistId == artistId }
        ArtistDto(
            id = artistId,
            name = name,
            albumIds = artistAlbums.map { it.id },
            trackIds = artistAlbums.flatMap { it.trackIds },
        )
    }

    private val tracksById = tracks.associateBy { it.id }

    fun snapshot(): CatalogSnapshot = CatalogSnapshot(
        version = VERSION,
        tracks = tracks,
        albums = albums,
        artists = artists,
    )

    fun trackById(id: String): TrackDto? = tracksById[id]

    fun search(query: String): CatalogSnapshot {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return snapshot()
        val matchingTracks = tracks.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.artistName.contains(trimmed, ignoreCase = true) ||
                it.albumTitle.contains(trimmed, ignoreCase = true)
        }
        val matchingAlbums = albums.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.artistName.contains(trimmed, ignoreCase = true)
        }
        val matchingArtists = artists.filter { it.name.contains(trimmed, ignoreCase = true) }
        return CatalogSnapshot(VERSION, matchingTracks, matchingAlbums, matchingArtists)
    }

    /** Default playlists mirror the offline sample so first sync feels continuous. */
    fun defaultPlaylists(): List<Pair<String, List<String>>> = listOf(
        "pl-1" to tracks.slice(4..8).map { it.id },
        "pl-2" to tracks.filterIndexed { index, _ -> index % 3 == 0 }.map { it.id },
        "pl-3" to tracks.slice(0..5).map { it.id },
        "pl-4" to tracks.slice(9..14).map { it.id },
        "pl-5" to tracks.slice(15..21).map { it.id },
    )
}
