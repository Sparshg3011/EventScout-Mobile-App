package com.example.eventfinder.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventfinder.network.RetrofitClient
import com.example.eventfinder.model.EventDetail
import com.example.eventfinder.model.EventItem
import com.example.eventfinder.model.FavoriteEvent
import com.example.eventfinder.model.FavoriteEventPayload
import com.example.eventfinder.model.SpotifyArtistResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val service = RetrofitClient.service

    private companion object {
        const val CURRENT_LOCATION_LABEL = "Current Location"
        // Hardcoded Los Angeles coordinates for "current location"
        const val CURRENT_LOCATION_LAT = 34.052235
        const val CURRENT_LOCATION_LNG = -118.243683
    }

    // Search State
    private val _searchResults = MutableStateFlow<List<EventItem>>(emptyList())
    val searchResults: StateFlow<List<EventItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Details State
    private val _selectedEvent = MutableStateFlow<EventDetail?>(null)
    val selectedEvent: StateFlow<EventDetail?> = _selectedEvent.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    // Spotify artist/albums state for Artist tab
    private val _spotifyData = MutableStateFlow<SpotifyArtistResponse?>(null)
    val spotifyData: StateFlow<SpotifyArtistResponse?> = _spotifyData.asStateFlow()

    private val _isLoadingSpotify = MutableStateFlow(false)
    val isLoadingSpotify: StateFlow<Boolean> = _isLoadingSpotify.asStateFlow()

    // Favorites State
    private val _favorites = MutableStateFlow<List<FavoriteEvent>>(emptyList())
    val favorites: StateFlow<List<FavoriteEvent>> = _favorites.asStateFlow()

    /**
     * Sync search results' favorite state with the current favorites list
     */
    private fun syncSearchFavorites() {
        val favIds = _favorites.value.map { it.id }.toSet()
        _searchResults.value = _searchResults.value.map { item ->
            item.copy(isFavorite = favIds.contains(item.id))
        }
    }

    // Autocomplete State
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Location Suggestions State (API only, no hardcoded list)
    private val _locationSuggestions = MutableStateFlow<List<String>>(emptyList())
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    private val _isLoadingLocationSuggestions = MutableStateFlow(false)
    val isLoadingLocationSuggestions: StateFlow<Boolean> = _isLoadingLocationSuggestions.asStateFlow()

    // Persisted search form state
    private val _searchKeyword = MutableStateFlow("")
    private val _searchLocation = MutableStateFlow(CURRENT_LOCATION_LABEL)
    private val _searchDistance = MutableStateFlow("10")
    private val _searchCategory = MutableStateFlow("All")

    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()
    val searchLocation: StateFlow<String> = _searchLocation.asStateFlow()
    val searchDistance: StateFlow<String> = _searchDistance.asStateFlow()
    val searchCategory: StateFlow<String> = _searchCategory.asStateFlow()

    fun updateSearchKeyword(value: String) {
        _searchKeyword.value = value
    }

    fun updateSearchLocation(value: String) {
        _searchLocation.value = value
    }

    fun updateSearchDistance(value: String) {
        _searchDistance.value = value
    }

    fun updateSearchCategory(value: String) {
        _searchCategory.value = value
    }

    init {
        fetchFavorites()
    }



    fun searchEvents(keyword: String, distance: Int, category: String, location: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            try {
                var lat = 0.0
                var lng = 0.0

                if (location.equals(CURRENT_LOCATION_LABEL, ignoreCase = true) || location.isEmpty()) {
                    // Force "Current Location" to Los Angeles, CA
                    lat = CURRENT_LOCATION_LAT
                    lng = CURRENT_LOCATION_LNG
                } else {
                    // Use backend geocoding endpoint
                    val geoResponse = service.geocodeAddress(location)
                    lat = (geoResponse["lat"] as? Number)?.toDouble() ?: 0.0
                    lng = (geoResponse["lng"] as? Number)?.toDouble() ?: 0.0
                }

                if (lat != 0.0 && lng != 0.0) {
                    val response = service.searchEvents(keyword, category, lat, lng, distance)
                    val events = response.map { event ->
                        val isFav = _favorites.value.any { it.id == event.id }
                        val genre = (event.genre ?: "").ifBlank { "unknown" }
                        EventItem(
                            id = event.id,
                            name = event.name,
                            date = event.date,
                            time = event.time,
                            venue = event.venue,
                            category = genre,
                            imageUrl = event.image,
                            url = event.url,
                            isFavorite = isFav
                        )
                    }
                    _searchResults.value = events
                    // ensure favorites state is synced after fetching events
                    syncSearchFavorites()
                } else {
                    _searchError.value = "Could not determine location"
                }

            } catch (e: Exception) {
                _searchError.value = e.message
                Log.e("SharedViewModel", "Error searching events: ${e.message}")
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun fetchSuggestions(keyword: String) {
        viewModelScope.launch {
            try {
                val response = service.getSuggestions(keyword)
                // Backend returns {"suggestions": ["Ed Sheeran", ...]}
                // Gson deserializes as ArrayList<Any>, so map to String safely
                val rawList = response["suggestions"] as? List<*>
                val suggestionsList = rawList?.mapNotNull { it?.toString() } ?: emptyList()
                _suggestions.value = suggestionsList
                Log.d("SharedViewModel", "Fetched suggestions: $suggestionsList")
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error fetching suggestions: ${e.message}")
            }
        }
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    fun fetchLocationSuggestions(query: String) {
        if (query.isBlank() || query.equals(CURRENT_LOCATION_LABEL, ignoreCase = true)) {
            _locationSuggestions.value = emptyList()
            return
        }

        // Fetch from backend autocomplete API (no hardcoded fallbacks)
        viewModelScope.launch {
            _isLoadingLocationSuggestions.value = true
            try {
                val response = service.getLocationAutocomplete(query)
                val rawList = response["predictions"] as? List<*>
                val predictions = rawList?.mapNotNull { item ->
                    (item as? Map<*, *>)?.get("description")?.toString()
                } ?: emptyList()
                
                if (predictions.isNotEmpty()) {
                    _locationSuggestions.value = predictions.take(5)
                }
                Log.d("SharedViewModel", "Fetched location suggestions: $predictions")
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error fetching location suggestions: ${e.message}")
                // Keep the local filtered results if API fails
            } finally {
                _isLoadingLocationSuggestions.value = false
            }
        }
    }

    fun clearLocationSuggestions() {
        _locationSuggestions.value = emptyList()
        _isLoadingLocationSuggestions.value = false
    }

    fun fetchEventDetails(eventId: String) {
        viewModelScope.launch {
            _isLoadingDetails.value = true
            _isLoadingSpotify.value = true
            _spotifyData.value = null
            try {
                val detail = service.getEventDetails(eventId)
                _selectedEvent.value = detail

                // Fetch Spotify data for first artist (if any)
                val artistName = detail.artists.firstOrNull()?.name
                if (!artistName.isNullOrBlank()) {
                    try {
                        _spotifyData.value = service.getSpotifyArtist(artistName)
                    } catch (e: Exception) {
                        Log.e("SharedViewModel", "Error fetching Spotify: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error fetching event details: ${e.message}")
            } finally {
                _isLoadingDetails.value = false
                _isLoadingSpotify.value = false
            }
        }
    }

    fun toggleFavorite(event: EventDetail) {
        viewModelScope.launch {
            val isFav = _favorites.value.any { it.id == event.id }
            if (isFav) {
                removeFavorite(event.id)
            } else {
                try {
                    val payload = FavoriteEventPayload(
                        id = event.id,
                        name = event.name,
                        date = event.date,
                        time = event.time,
                        venue = event.venue?.name ?: "",
                        genre = event.genres.firstOrNull() ?: "",
                        image = event.seatmapUrl ?: "",
                        url = event.url
                    )
                    service.addFavorite(payload)
                    fetchFavorites()
                    syncSearchFavorites()
                } catch (e: Exception) {
                    Log.e("SharedViewModel", "Error toggling favorite: ${e.message}")
                }
            }
        }
    }

    fun toggleFavorite(event: EventItem) {
        viewModelScope.launch {
            val isFav = _favorites.value.any { it.id == event.id }
            if (isFav) {
                removeFavorite(event.id)
            } else {
                try {
                    val payload = FavoriteEventPayload(
                        id = event.id,
                        name = event.name,
                        date = event.date,
                        time = event.time,
                        venue = event.venue,
                        genre = event.category,
                        image = event.imageUrl,
                        url = event.url
                    )
                    service.addFavorite(payload)
                    fetchFavorites()
                    syncSearchFavorites()
                } catch (e: Exception) {
                    Log.e("SharedViewModel", "Error toggling favorite: ${e.message}")
                }
            }
        }
    }

    fun fetchFavorites() {
        viewModelScope.launch {
            try {
                _favorites.value = service.getFavorites()
                    .sortedByDescending { it.createdAt }
                syncSearchFavorites()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error fetching favorites: ${e.message}")
            }
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            try {
                service.removeFavorite(id)
                fetchFavorites()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error removing favorite: ${e.message}")
            }
        }
    }
}
