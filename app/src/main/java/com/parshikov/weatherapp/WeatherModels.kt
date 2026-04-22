package com.parshikov.weatherapp

import com.google.gson.annotations.SerializedName

// Ответ от API Яндекс.Погоды (сокращённая версия под наши нужды)
data class YandexWeatherResponse(
    val fact: Fact,
    val forecasts: List<Forecast>
)

data class Fact(
    val temp: Int,                // температура в °C
    @SerializedName("feels_like") val feelsLike: Int,
    val condition: String,        // описание погоды (например, "cloudy")
    @SerializedName("wind_speed") val windSpeed: Double,
    @SerializedName("pressure_mm") val pressureMm: Int, // давление в мм рт. ст.
    val humidity: Int,
    val daytime: String,          // "d" (день) или "n" (ночь)
    val icon: String              // иконка, например "ovc" (можно использовать для отображения)
)

data class Forecast(
    val date: String,             // "2025-04-14"
    val parts: Parts
)

data class Parts(
    val day: Part,
    val night: Part
)

data class Part(
    @SerializedName("temp_min") val tempMin: Int,
    @SerializedName("temp_max") val tempMax: Int,
    val condition: String,
    val icon: String
)

// UI-состояние
data class WeatherUiState(
    val cityName: String = "Борисоглебск",
    val currentTemp: String = "--°",
    val weatherDesc: String = "Загрузка...",
    val todayMinMax: String = "--° / --°",
    val yesterdayTemp: String = "--° / --°",
    val tomorrowTemp: String = "--° / --°",
    val humidity: String = "--%",
    val pressure: String = "-- мм рт. ст.",
    val aqi: String = "--",
    val iconCode: String = "ovc", // для Яндекс.Погоды используем иконку из Fact.icon
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)