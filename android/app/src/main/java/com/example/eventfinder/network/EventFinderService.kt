package com.example.eventfinder.network

import com.example.eventfinder.model.BackendEvent
import com.example.eventfinder.model.EventDetail
import com.example.eventfinder.model.FavoriteEvent
import com.example.eventfinder.model.FavoriteEventPayload
import com.example.eventfinder.model.SearchResponse
import com.example.eventfinder.model.SpotifyArtistResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface EventFinderService {
    @GET("api/events/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String,
        @Query("category") category: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("distance") distance: Int
    ): List<BackendEvent>


    @GET("api/events/{id}")
    suspend fun getEventDetails(@Path("id") id: String): EventDetail

    @GET("api/events/spotify/artist")
    suspend fun getSpotifyArtist(@Query("name") name: String): SpotifyArtistResponse

    @GET("api/events/suggestions")
    suspend fun getSuggestions(@Query("keyword") keyword: String): Map<String, Any> // Adjust as needed

    @GET("api/favorites")
    suspend fun getFavorites(): List<FavoriteEvent>

    @POST("api/favorites")
    suspend fun addFavorite(@Body favorite: FavoriteEventPayload): FavoriteEvent

    @DELETE("api/favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: String): Map<String, Any>

    @GET("api/geo/ip-location")
    suspend fun getIpLocation(): Map<String, Any>

    @GET("api/geo/geocode")
    suspend fun geocodeAddress(@Query("address") address: String): Map<String, Any>

    @GET("api/geo/autocomplete")
    suspend fun getLocationAutocomplete(@Query("input") input: String): Map<String, Any>
}

interface ExternalService {
    @GET
    suspend fun getIpInfo(@retrofit2.http.Url url: String): Map<String, Any>

    @GET
    suspend fun getGeocoding(@retrofit2.http.Url url: String): Map<String, Any>
}

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // Android Emulator localhost

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: EventFinderService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventFinderService::class.java)
    }

    val externalService: ExternalService by lazy {
        Retrofit.Builder()
            .baseUrl("https://ipinfo.io/") // Base URL placeholder
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExternalService::class.java)
    }
}
