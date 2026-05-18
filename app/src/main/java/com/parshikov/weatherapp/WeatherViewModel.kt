package com.parshikov.weatherapp

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.parshikov.weatherapp.data.CityStorage
import com.parshikov.weatherapp.data.SavedCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(WeatherUiState())
    val uiState: LiveData<WeatherUiState> = _uiState

    private val _graphItems = MutableLiveData<List<ForecastGraphItem>>()
    val graphItems: LiveData<List<ForecastGraphItem>> = _graphItems

    private val apiKey = "КЛЮЧ АПИ"
    private val apiService = WeatherApiClient.apiService

    private val defaultLat = 51.3737
    private val defaultLon = 42.0889

    private val cityStorage = CityStorage(application)

    fun loadWeatherData(lat: Double = defaultLat, lon: Double = defaultLon) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)
            try {
                val coordString = "$lat,$lon"
                val locationResponse = apiService.getLocationKey(apiKey, coordString)
                val locationKey = locationResponse.key
                val cityName = locationResponse.localizedName

                val forecastResponse = apiService.get5DayForecast(locationKey, apiKey)
                val dailyList = forecastResponse.DailyForecasts

                val today = dailyList.getOrNull(0)
                val tomorrow = dailyList.getOrNull(1)

                val todayMax = today?.Temperature?.Maximum?.Value?.toInt() ?: 0
                val todayMin = today?.Temperature?.Minimum?.Value?.toInt() ?: 0
                val tomorrowMax = tomorrow?.Temperature?.Maximum?.Value?.toInt() ?: 0
                val tomorrowMin = tomorrow?.Temperature?.Minimum?.Value?.toInt() ?: 0

                val weatherDesc = today?.Day?.iconPhrase ?: ""
                val iconCode = today?.Day?.icon ?: 1

                val humidityAvg = today?.Day?.relativeHumidity?.Average
                val humidityStr = if (humidityAvg != null) "$humidityAvg%" else "--%"

                val windObj = today?.Day?.wind
                val windStr = if (windObj != null && windObj.speed.value > 0) {
                    "${windObj.speed.value.toInt()} км/ч, ${windObj.direction.localized}"
                } else "-- км/ч"

                val uvItem = today?.airAndPollen?.find { it.name == "UVIndex" }
                val uvStr = if (uvItem != null && uvItem.value > 0) {
                    "${uvItem.value.toInt()}, ${uvItem.category}"
                } else "--"

                val emoji = getEmojiForIcon(iconCode)

                // Сохраняем город
                cityStorage.addCityAndSetCurrent(SavedCity(cityName, lat, lon, isCurrent = true))

                val newState = WeatherUiState(
                    cityName = cityName,
                    currentTemp = "${(todayMax + todayMin) / 2}°",
                    weatherDesc = weatherDesc,
                    todayMinMax = "${todayMax}° / ${todayMin}°",
                    yesterdayTemp = "--° / --°",
                    tomorrowTemp = "${tomorrowMax}° / ${tomorrowMin}°",
                    humidity = humidityStr,
                    wind = windStr,
                    uvIndex = uvStr,
                    iconEmoji = emoji,
                    isLoading = false,
                    lat = lat,
                    lon = lon
                )
                _uiState.value = newState
                Log.d("WeatherVM", "Updated state: $newState")

                // Данные для графика с числовыми значениями
                val graphData = dailyList.take(5).mapIndexed { index, day ->
                    val date = Date(day.EpochDate * 1000)
                    val dayName = when (index) {
                        0 -> "Сегодня"
                        1 -> "Завтра"
                        else -> SimpleDateFormat("E", Locale("ru")).format(date)
                    }
                    val maxVal = day.Temperature.Maximum.Value.toInt()
                    val minVal = day.Temperature.Minimum.Value.toInt()
                    ForecastGraphItem(
                        dayName = dayName,
                        maxDayTemp = "${maxVal}°",
                        minNightTemp = "${minVal}°",
                        maxTempValue = maxVal,
                        minTempValue = minVal
                    )
                }
                _graphItems.postValue(graphData)

            } catch (e: IOException) {
                Log.e("WeatherVM", "Network error", e)
                _uiState.value = _uiState.value?.copy(isLoading = false, errorMessage = "Ошибка сети")
            } catch (e: Exception) {
                Log.e("WeatherVM", "Error", e)
                _uiState.value = _uiState.value?.copy(isLoading = false, errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    private fun getEmojiForIcon(code: Int): String = when (code) {
        1, 2, 33, 34 -> "☀️"
        3, 4, 35, 36 -> "🌤️"
        5, 6, 7, 38 -> "⛅"
        8, 9, 10, 11, 12 -> "🌧️"
        13, 14, 15, 16, 17 -> "🌦️"
        18, 19, 20, 21, 22 -> "🌩️"
        23, 24, 25, 26, 27 -> "❄️"
        else -> "❓"
    }

    private suspend fun getCityName(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                addresses?.firstOrNull()?.locality ?: "Неизвестный город"
            } catch (e: Exception) {
                "Неизвестный город"
            }
        }
    }

    // Управление списком городов
    fun refreshCities(): List<SavedCity> = cityStorage.getCities()

    fun deleteCity(position: Int) {
        cityStorage.removeCity(position)
    }

    fun addCityAndLoad(city: SavedCity) {
        cityStorage.addCityAndSetCurrent(city)
        loadWeatherData(city.lat, city.lon)
    }

    fun selectCity(position: Int) {
        val cities = cityStorage.getCities()
        if (position in cities.indices) {
            cityStorage.setCurrentCity(position)
            val city = cities[position]
            loadWeatherData(city.lat, city.lon)
        }
    }
}