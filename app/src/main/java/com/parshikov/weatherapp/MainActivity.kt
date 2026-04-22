package com.parshikov.weatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.parshikov.weatherapp.ui.SavedCitiesActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    // Лаунчер для получения результата выбора города
    private val cityPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", 0.0) ?: return@registerForActivityResult
            val lon = result.data?.getDoubleExtra("lon", 0.0) ?: return@registerForActivityResult
            Log.d("MainActivity", "Received city coordinates: $lat, $lon")
            viewModel.loadWeatherData(lat, lon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка "+"
        findViewById<ImageView>(R.id.settingsButton1).setOnClickListener {
            cityPickerLauncher.launch(Intent(this, SavedCitiesActivity::class.java))
        }

        // Загружаем погоду по умолчанию при первом запуске
        viewModel.loadWeatherData()

        // Наблюдаем за изменениями UI
        viewModel.uiState.observe(this, Observer { state ->
            updateUI(state)
        })
    }

    private fun updateUI(state: WeatherUiState) {
        findViewById<TextView>(R.id.cityName).text = state.cityName
        findViewById<TextView>(R.id.currentTemp).text = state.currentTemp
        findViewById<TextView>(R.id.weatherDesc).text = state.weatherDesc
        findViewById<TextView>(R.id.todayMinMax).text = state.todayMinMax
        findViewById<TextView>(R.id.yesterdayTemp).text = state.yesterdayTemp
        findViewById<TextView>(R.id.tomorrowTemp).text = state.tomorrowTemp
        findViewById<TextView>(R.id.humidityValue).text = state.humidity
        findViewById<TextView>(R.id.pressureValue).text = state.pressure
        findViewById<TextView>(R.id.aqiValue).text = state.aqi

        // Добавляем обновление для "Сегодня" в панели прогноза
        findViewById<TextView>(R.id.todayForecastTemp).text = state.todayMinMax  // используем ту же строку

        val iconUrl = "https://yastatic.net/weather/i/icons/funky/dark/${state.iconCode}.png"
        Glide.with(this)
            .load(iconUrl)
            .placeholder(R.drawable.weather_app_logo)
            .error(R.drawable.weather_app_logo)
            .into(findViewById(R.id.weatherIcon))
    }
}