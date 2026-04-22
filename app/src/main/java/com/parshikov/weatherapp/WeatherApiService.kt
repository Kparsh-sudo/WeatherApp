package com.parshikov.weatherapp

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v2/forecast")
    suspend fun getWeather(
        @Header("X-Yandex-Weather-Key") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String = "ru_RU",
        @Query("limit") limit: Int = 2, // сегодня + завтра
        @Query("hours") hours: Boolean = false,
        @Query("extra") extra: Boolean = false
    ): YandexWeatherResponse
}