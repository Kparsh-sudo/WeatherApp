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
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(WeatherUiState())
    val uiState: LiveData<WeatherUiState> = _uiState

    private val apiKey = "366ee947-5168-40ac-ab54-cef3a82e2ade" // ваш ключ Яндекс.Погоды
    private val apiService = WeatherApiClient.apiService

    private val defaultLat = 51.3737
    private val defaultLon = 42.0889

    private val cityStorage = CityStorage(application)

    fun loadWeatherData(lat: Double = defaultLat, lon: Double = defaultLon) {
        Log.d("WeatherVM", "loadWeatherData called with lat=$lat, lon=$lon")
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.getWeather(
                    apiKey = apiKey,
                    lat = lat,
                    lon = lon
                )

                val cityName = getCityName(lat, lon)
                val fact = response.fact
                val today = response.forecasts[0].parts.day
                val tomorrow = response.forecasts[1].parts.day

                cityStorage.addCityAndSetCurrent(SavedCity(cityName, lat, lon, isCurrent = true))

                val todayMinMax = "${today.tempMax}° / ${today.tempMin}°"
                val tomorrowMinMax = "${tomorrow.tempMax}° / ${tomorrow.tempMin}°"

                val newState = _uiState.value?.copy(
                    cityName = cityName,
                    currentTemp = "${fact.temp}°",
                    weatherDesc = fact.condition,
                    todayMinMax = todayMinMax,
                    yesterdayTemp = todayMinMax,
                    tomorrowTemp = tomorrowMinMax,
                    humidity = "${fact.humidity}%",
                    pressure = "${fact.pressureMm} мм рт. ст.",
                    aqi = "—",
                    iconCode = fact.icon,
                    isLoading = false,
                    errorMessage = null
                )
                Log.d("WeatherVM", "UI updated with city: $cityName, temp: ${fact.temp}")
                _uiState.value = newState
            } catch (e: IOException) {
                Log.e("WeatherVM", "Network error", e)
                _uiState.value = _uiState.value?.copy(isLoading = false, errorMessage = "Ошибка сети")
            } catch (e: Exception) {
                Log.e("WeatherVM", "Error", e)
                _uiState.value = _uiState.value?.copy(isLoading = false, errorMessage = "Ошибка: ${e.message}")
            }
        }
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

    fun refreshCities(): List<SavedCity> {
        val cities = cityStorage.getCities()
        Log.d("WeatherVM", "refreshCities: ${cities.map { it.name }}")
        return cities
    }

    fun selectCity(position: Int) {
        val cities = cityStorage.getCities()
        Log.d("WeatherVM", "selectCity position=$position, cities size=${cities.size}")
        if (position in cities.indices) {
            cityStorage.setCurrentCity(position)
            val city = cities[position]
            Log.d("WeatherVM", "Selected city: ${city.name} (${city.lat}, ${city.lon})")
            loadWeatherData(city.lat, city.lon)
        }
    }

    fun deleteCity(position: Int) {
        cityStorage.removeCity(position)
        val cities = cityStorage.getCities()
        if (cities.isNotEmpty()) {
            val current = cityStorage.getCurrentCity() ?: cities[0]
            loadWeatherData(current.lat, current.lon)
        } else {
            loadWeatherData(defaultLat, defaultLon)
        }
    }

    fun addCityAndLoad(city: SavedCity) {
        Log.d("WeatherVM", "addCityAndLoad: ${city.name}")
        cityStorage.addCityAndSetCurrent(city)
        loadWeatherData(city.lat, city.lon)
    }
}