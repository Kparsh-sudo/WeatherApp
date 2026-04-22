package com.parshikov.weatherapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedCity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val isCurrent: Boolean = false
)

class CityStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CITIES = "cities"
        private const val KEY_CURRENT_CITY_INDEX = "current_city_index"
    }

    /**
     * Сохранить список городов
     */
    fun saveCities(cities: List<SavedCity>) {
        val json = gson.toJson(cities)
        prefs.edit().putString(KEY_CITIES, json).apply()
    }

    /**
     * Получить список всех сохранённых городов
     */
    fun getCities(): List<SavedCity> {
        val json = prefs.getString(KEY_CITIES, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedCity>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Добавить новый город (если его ещё нет в списке) и сделать текущим
     */
    fun addCityAndSetCurrent(city: SavedCity) {
        val cities = getCities().toMutableList()
        // Проверяем, нет ли уже такого города (по названию и координатам)
        val existingIndex = cities.indexOfFirst { it.name == city.name && it.lat == city.lat && it.lon == city.lon }
        val newCity = city.copy(isCurrent = true)

        if (existingIndex != -1) {
            // Город уже есть – просто делаем его текущим
            cities.forEachIndexed { index, savedCity ->
                cities[index] = savedCity.copy(isCurrent = (index == existingIndex))
            }
        } else {
            // Добавляем новый город и делаем его текущим
            cities.forEachIndexed { index, savedCity ->
                cities[index] = savedCity.copy(isCurrent = false)
            }
            cities.add(newCity)
        }
        saveCities(cities)
    }

    /**
     * Получить текущий выбранный город
     */
    fun getCurrentCity(): SavedCity? {
        return getCities().find { it.isCurrent }
    }

    /**
     * Установить город текущим по позиции в списке
     */
    fun setCurrentCity(position: Int) {
        val cities = getCities().toMutableList()
        cities.forEachIndexed { index, savedCity ->
            cities[index] = savedCity.copy(isCurrent = (index == position))
        }
        saveCities(cities)
    }

    /**
     * Удалить город по позиции
     */
    fun removeCity(position: Int) {
        val cities = getCities().toMutableList()
        if (position in cities.indices) {
            cities.removeAt(position)
            // Если удалили текущий город, делаем первый текущим (если есть)
            if (cities.isNotEmpty() && cities.none { it.isCurrent }) {
                cities[0] = cities[0].copy(isCurrent = true)
            }
            saveCities(cities)
        }
    }

    /**
     * Есть ли вообще сохранённые города
     */
    fun hasCities(): Boolean = getCities().isNotEmpty()
}
