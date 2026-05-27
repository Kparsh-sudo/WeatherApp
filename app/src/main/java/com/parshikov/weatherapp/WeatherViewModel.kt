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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(WeatherUiState())
    val uiState: LiveData<WeatherUiState> = _uiState

    private val _graphItems = MutableLiveData<List<ForecastGraphItem>>()
    val graphItems: LiveData<List<ForecastGraphItem>> = _graphItems

    private val apiKey = "daa"
    private val apiService = WeatherApiClient.apiService

    private val defaultLat = 51.3737
    private val defaultLon = 42.0889

    private val cityStorage = CityStorage(application)
    private val appPreferences = AppPreferences(application)

    fun loadWeatherData(lat: Double = defaultLat, lon: Double = defaultLon, force: Boolean = false) {
        if (!force && appPreferences.nightModeEnabled && isNightTime()) {
            Log.d("WeatherVM", "Обновление пропущено (ночной режим)")
            return
        }

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

                // Ветер
                val windSpeedKph = today?.Day?.wind?.speed?.value ?: 0.0
                val windSpeedConverted = when (appPreferences.windUnit) {
                    "ms" -> windSpeedKph / 3.6
                    "mph" -> windSpeedKph / 1.609
                    else -> windSpeedKph
                }
                val windUnitLabel = when (appPreferences.windUnit) {
                    "ms" -> "м/с"
                    "mph" -> "миль/ч"
                    else -> "км/ч"
                }
                val windStr = if (windSpeedConverted > 0) {
                    "${windSpeedConverted.toInt()} $windUnitLabel, ${today?.Day?.wind?.direction?.localized ?: ""}"
                } else "--"

                val uvItem = today?.airAndPollen?.find { it.name == "UVIndex" }
                val uvStr = if (uvItem != null && uvItem.value > 0) {
                    "${uvItem.value.toInt()}, ${uvItem.category}"
                } else "--"

                // Конвертация температуры
                val isFahrenheit = appPreferences.temperatureUnit == "fahrenheit"
                val currentTemp = (todayMax + todayMin) / 2
                val currentTempStr = if (isFahrenheit) "${celsiusToFahrenheit(currentTemp)}°F" else "${currentTemp}°C"
                val todayMaxStr = if (isFahrenheit) "${celsiusToFahrenheit(todayMax)}°F" else "${todayMax}°C"
                val todayMinStr = if (isFahrenheit) "${celsiusToFahrenheit(todayMin)}°F" else "${todayMin}°C"
                val tomorrowMaxStr = if (isFahrenheit) "${celsiusToFahrenheit(tomorrowMax)}°F" else "${tomorrowMax}°C"
                val tomorrowMinStr = if (isFahrenheit) "${celsiusToFahrenheit(tomorrowMin)}°F" else "${tomorrowMin}°C"

                val emoji = getEmojiForIcon(iconCode)

                // Сохраняем город
                cityStorage.addCityAndSetCurrent(SavedCity(cityName, lat, lon, isCurrent = true))

                val newState = WeatherUiState(
                    cityName = cityName,
                    currentTemp = currentTempStr,
                    weatherDesc = weatherDesc,
                    todayMinMax = "$todayMaxStr / $todayMinStr",
                    yesterdayTemp = "--° / --°",
                    tomorrowTemp = "$tomorrowMaxStr / $tomorrowMinStr",
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

                // Данные для графика (с учётом единиц температуры)
                val graphData = dailyList.take(5).mapIndexed { index, day ->
                    val date = Date(day.EpochDate * 1000)
                    val dayName = when (index) {
                        0 -> "Сегодня"
                        1 -> "Завтра"
                        else -> SimpleDateFormat("E", Locale("ru")).format(date)
                    }
                    val maxVal = day.Temperature.Maximum.Value.toInt()
                    val minVal = day.Temperature.Minimum.Value.toInt()
                    val maxDisplay = if (isFahrenheit) celsiusToFahrenheit(maxVal) else maxVal
                    val minDisplay = if (isFahrenheit) celsiusToFahrenheit(minVal) else minVal
                    ForecastGraphItem(
                        dayName = dayName,
                        maxDayTemp = "${maxDisplay}°",
                        minNightTemp = "${minDisplay}°",
                        maxTempValue = maxDisplay,
                        minTempValue = minDisplay
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

    private fun celsiusToFahrenheit(celsius: Int): Int = (celsius * 9 / 5) + 32

    private fun isNightTime(): Boolean {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour >= 23 || hour < 7
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