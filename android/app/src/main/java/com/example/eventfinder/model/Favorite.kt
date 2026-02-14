package com.example.eventfinder.model

data class FavoriteEventPayload(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val venue: String,
    val genre: String, // used for category icon
    val image: String,
    val url: String
)

data class FavoriteEvent(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val venue: String,
    val genre: String,
    val image: String,
    val url: String,
    val createdAt: String
) {
    fun toPayload(): FavoriteEventPayload {
        return FavoriteEventPayload(
            id = id,
            name = name,
            date = date,
            time = time,
            venue = venue,
            genre = genre,
            image = image,
            url = url
        )
    }
}
