package com.parshikov.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.parshikov.weatherapp.ui.SavedCitiesActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: WeatherViewModel by viewModels()

    private val cityPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", Double.NaN) ?: return@registerForActivityResult
            val lon = result.data?.getDoubleExtra("lon", Double.NaN) ?: return@registerForActivityResult
            if (!lat.isNaN() && !lon.isNaN()) {
                viewModel.loadWeatherData(lat, lon)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка "+" – список городов
        findViewById<ImageView>(R.id.settingsButton1).setOnClickListener {
            cityPickerLauncher.launch(Intent(this, SavedCitiesActivity::class.java))
        }

        // Кнопка "Показать полностью" – график на 5 дней
        val showFullForecast: TextView = findViewById(R.id.showFullForecast)
        showFullForecast.setOnClickListener {
            // Берем координаты из хранилища городов (текущий город)
            val cities = viewModel.refreshCities()
            val currentCity = cities.firstOrNull { it.isCurrent }
            if (currentCity != null) {
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("lat", currentCity.lat)
                    putExtra("lon", currentCity.lon)
                    putExtra("cityName", currentCity.name)
                }
                startActivity(intent)
            } else {
                // fallback – координаты по умолчанию
                val state = viewModel.uiState.value
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("lat", state?.lat ?: 51.3737)
                    putExtra("lon", state?.lon ?: 42.0889)
                    putExtra("cityName", state?.cityName ?: "Прогноз")
                }
                startActivity(intent)
            }
        }

        // Загружаем погоду по умолчанию
        viewModel.loadWeatherData()

        // Наблюдаем за UI
        viewModel.uiState.observe(this, Observer { state ->
            updateUI(state)
        })
    }

    private fun updateUI(state: WeatherUiState) {
        findViewById<TextView>(R.id.cityName).text = state.cityName
        findViewById<TextView>(R.id.currentTemp).text = state.currentTemp
        findViewById<TextView>(R.id.weatherDesc).text = state.weatherDesc
        findViewById<TextView>(R.id.todayMinMax).text = state.todayMinMax
        findViewById<TextView>(R.id.tomorrowTemp).text = state.tomorrowTemp
        findViewById<TextView>(R.id.humidityValue).text = state.humidity
        findViewById<TextView>(R.id.pressureValue).text = state.wind
        findViewById<TextView>(R.id.aqiValue).text = state.uvIndex
        findViewById<TextView>(R.id.todayForecastTemp).text = state.todayMinMax
        findViewById<TextView>(R.id.yesterdayTemp).text = state.todayMinMax
    }
}