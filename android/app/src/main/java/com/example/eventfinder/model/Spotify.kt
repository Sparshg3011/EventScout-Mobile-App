package com.example.eventfinder.model

data class SpotifyArtistInfo(
    val id: String,
    val name: String,
    val followers: Int,
    val popularity: Int,
    val genres: List<String>,
    val spotifyUrl: String?,
    val image: String?
)

data class SpotifyAlbumInfo(
    val id: String,
    val name: String,
    val releaseDate: String?,
    val totalTracks: Int?,
    val spotifyUrl: String?,
    val image: String?
)

data class SpotifyArtistResponse(
    val artist: SpotifyArtistInfo?,
    val albums: List<SpotifyAlbumInfo>
)
