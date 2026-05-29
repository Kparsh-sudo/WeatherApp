package com.parshikov.weatherapp

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)

    // Температура: "celsius" или "fahrenheit"
    var temperatureUnit: String
        get() = prefs.getString("temp_unit", "celsius") ?: "celsius"
        set(value) = prefs.edit().putString("temp_unit", value).apply()

    // Ветер: "kmh", "ms", "mph"
    var windUnit: String
        get() = prefs.getString("wind_unit", "kmh") ?: "kmh"
        set(value) = prefs.edit().putString("wind_unit", value).apply()

    // Ночной режим (не обновлять с 23:00 до 7:00) – пока только сохраняется
    var nightModeEnabled: Boolean
        get() = prefs.getBoolean("night_mode", false)
        set(value) = prefs.edit().putBoolean("night_mode", value).apply()
}