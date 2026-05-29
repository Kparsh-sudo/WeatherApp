package com.parshikov.weatherapp

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
        private const val KEY_WIND_UNIT = "wind_unit"
        private const val KEY_NIGHT_MODE_ENABLED = "night_mode_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

        // Значения по умолчанию
        private const val DEFAULT_TEMPERATURE_UNIT = "celsius"   // "celsius" или "fahrenheit"
        private const val DEFAULT_WIND_UNIT = "kmh"              // "kmh", "ms", "mph"
        private const val DEFAULT_NIGHT_MODE_ENABLED = false
        private const val DEFAULT_NOTIFICATIONS_ENABLED = true
    }

    /**
     * Единица измерения температуры
     * @return "celsius" или "fahrenheit"
     */
    val temperatureUnit: String
        get() = prefs.getString(KEY_TEMPERATURE_UNIT, DEFAULT_TEMPERATURE_UNIT) ?: DEFAULT_TEMPERATURE_UNIT

    fun setTemperatureUnit(unit: String) {
        prefs.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply()
    }

    /**
     * Единица измерения ветра
     * @return "kmh" (км/ч), "ms" (м/с), "mph" (миль/ч)
     */
    val windUnit: String
        get() = prefs.getString(KEY_WIND_UNIT, DEFAULT_WIND_UNIT) ?: DEFAULT_WIND_UNIT

    fun setWindUnit(unit: String) {
        prefs.edit().putString(KEY_WIND_UNIT, unit).apply()
    }

    /**
     * Ночной режим (отключение обновлений ночью)
     */
    val nightModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_NIGHT_MODE_ENABLED, DEFAULT_NIGHT_MODE_ENABLED)

    fun setNightModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE_ENABLED, enabled).apply()
    }

    /**
     * Включены ли уведомления
     */
    val notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    /**
     * Сбросить все настройки на значения по умолчанию
     */
    fun resetToDefaults() {
        prefs.edit()
            .putString(KEY_TEMPERATURE_UNIT, DEFAULT_TEMPERATURE_UNIT)
            .putString(KEY_WIND_UNIT, DEFAULT_WIND_UNIT)
            .putBoolean(KEY_NIGHT_MODE_ENABLED, DEFAULT_NIGHT_MODE_ENABLED)
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
            .apply()
    }
}