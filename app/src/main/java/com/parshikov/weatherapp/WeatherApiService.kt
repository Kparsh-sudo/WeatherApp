package com.parshikov.weatherapp

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApiService {
    // Получение Location Key
    @GET("locations/v1/cities/geoposition/search")
    suspend fun getLocationKey(
        @Query("apikey") apiKey: String,
        @Query("q") coords: String,
        @Query("language") lang: String = "ru",
        @Query("details") details: Boolean = false,
        @Query("toplevel") topLevel: Boolean = true
    ): LocationKeyResponse

    // 5-дневный прогноз (исправленный порядок!)
    @GET("forecasts/v1/daily/5day/{locationKey}")
    suspend fun get5DayForecast(
        @Path("locationKey") locationKey: String,      // ← сначала Path
        @Query("apikey") apiKey: String,               // ← потом Query
        @Query("language") lang: String = "ru",
        @Query("details") details: Boolean = true,
        @Query("metric") metric: Boolean = true
    ): Forecast5DayResponse
}