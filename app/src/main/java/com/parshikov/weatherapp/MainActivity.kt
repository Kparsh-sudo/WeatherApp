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

        // Вот здесь была ошибка – используем ImageView
        findViewById<ImageView>(R.id.settingsButton1).setOnClickListener {
            cityPickerLauncher.launch(Intent(this, SavedCitiesActivity::class.java))
        }

        viewModel.loadWeatherData()

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

        // Новые поля
        findViewById<TextView>(R.id.pressureValue).text = state.wind
        findViewById<TextView>(R.id.aqiValue).text = state.uvIndex

        // Дублируем todayMinMax в todayForecastTemp и yesterdayTemp
        findViewById<TextView>(R.id.todayForecastTemp).text = state.todayMinMax
        findViewById<TextView>(R.id.yesterdayTemp).text = state.todayMinMax
    }
}