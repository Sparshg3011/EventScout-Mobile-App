package com.example.eventfinder.model

data class EventDetail(
    val id: String,
    val name: String,
    val url: String,
    val date: String,
    val time: String,
    val status: String,
    val venue: VenueDetail?,
    val genres: List<String>,
    val artists: List<Artist>,
    val priceRanges: List<PriceRange>?,
    val seatmapUrl: String?
)

data class VenueDetail(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val location: Location?,
    val url: String?,
    val image: String?,
    val generalRule: String?,
    val childRule: String?,
    val parkingDetail: String?
)

data class Location(
    val latitude: String?,
    val longitude: String?
)

data class Artist(
    val name: String,
    val url: String?,
    val twitter: String?,
    val facebook: String?,
    val image: String?
)

data class PriceRange(
    val type: String?,
    val currency: String?,
    val min: Double?,
    val max: Double?
)
