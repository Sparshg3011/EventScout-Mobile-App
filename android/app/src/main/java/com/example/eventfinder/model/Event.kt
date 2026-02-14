package com.example.eventfinder.model

data class SearchResponse(
    val _embedded: EmbeddedEvents
)

data class BackendEvent(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val venue: String,
    val genre: String,
    val image: String,
    val url: String
)

data class EmbeddedEvents(
    val events: List<Event>
)

data class Event(
    val id: String,
    val name: String,
    val dates: DateInfo,
    val images: List<Image>,
    val _embedded: EmbeddedVenues?,
    val classifications: List<Classification>?
)

data class DateInfo(
    val start: StartDate
)

data class StartDate(
    val localDate: String,
    val localTime: String?
)

data class Image(
    val url: String
)

data class EmbeddedVenues(
    val venues: List<Venue>
)

data class Venue(
    val name: String
)

data class Classification(
    val segment: Segment
)

data class Segment(
    val name: String
)

// Simplified model for UI usage
data class EventItem(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val venue: String,
    val category: String, // For icon
    val imageUrl: String,
    val url: String = "", // For favorites
    val isFavorite: Boolean = false
)
