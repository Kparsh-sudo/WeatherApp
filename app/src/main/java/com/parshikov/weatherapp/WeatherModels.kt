package com.parshikov.weatherapp

import com.google.gson.annotations.SerializedName

// ----- AccuWeather Location -----
data class LocationKeyResponse(
    @SerializedName("Key") val key: String,
    @SerializedName("LocalizedName") val localizedName: String
)

// ----- 5 Day Forecast -----
data class Forecast5DayResponse(
    val DailyForecasts: List<DailyForecast>
)

data class DailyForecast(
    val Temperature: Temperature,
    val Day: DayNight,
    val Night: DayNight,
    val EpochDate: Long,
    @SerializedName("AirAndPollen") val airAndPollen: List<AirQualityItem>?
)

data class Temperature(
    val Minimum: TempValue,
    val Maximum: TempValue
)

data class TempValue(
    val Value: Double,
    val Unit: String
)

data class DayNight(
    @SerializedName("Icon") val icon: Int,
    @SerializedName("IconPhrase") val iconPhrase: String,
    @SerializedName("RelativeHumidity") val relativeHumidity: Humidity?,
    @SerializedName("Wind") val wind: Wind?
)

data class Wind(
    @SerializedName("Speed") val speed: Speed,
    @SerializedName("Direction") val direction: Direction
)

data class Speed(
    @SerializedName("Value") val value: Double,
    @SerializedName("Unit") val unit: String
)

data class Direction(
    @SerializedName("Localized") val localized: String,
    @SerializedName("English") val english: String
)

data class Humidity(
    val Minimum: Int,
    val Maximum: Int,
    val Average: Int
)

data class AirQualityItem(
    @SerializedName("Name") val name: String,
    @SerializedName("Value") val value: Double,
    @SerializedName("Category") val category: String
)

// ----- UI State -----
data class WeatherUiState(
    val cityName: String = "Борисоглебск",
    val currentTemp: String = "--°",
    val weatherDesc: String = "Загрузка...",
    val todayMinMax: String = "--° / --°",
    val yesterdayTemp: String = "--° / --°",
    val tomorrowTemp: String = "--° / --°",
    val humidity: String = "--%",
    val wind: String = "-- км/ч",          // вместо давления
    val uvIndex: String = "--",           // вместо ИКВ
    val iconEmoji: String = "❓",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)