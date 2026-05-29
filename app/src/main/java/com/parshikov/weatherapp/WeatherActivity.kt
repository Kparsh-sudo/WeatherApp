package com.parshikov.weatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WeatherActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: ForecastGraphAdapter
    private var lat: Double = 51.3737
    private var lon: Double = 42.0889
    private var cityName: String = "Прогноз"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Извлекаем переданные параметры
        lat = intent.getDoubleExtra("lat", 51.3737)
        lon = intent.getDoubleExtra("lon", 42.0889)
        cityName = intent.getStringExtra("cityName") ?: "Прогноз"

        Log.d("WeatherActivity", "onCreate: lat=$lat, lon=$lon, city=$cityName")

        // Заголовок и кнопка назад
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = cityName
        toolbar.setNavigationOnClickListener { finish() }

        // Настройка RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.forecastGraphRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = ForecastGraphAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(ForecastGraphItemDecoration(adapter))

        // Загружаем погоду
        requestWeatherData()

        // Наблюдаем за элементами графика
        viewModel.graphItems.observe(this, Observer { items ->
            Log.d("WeatherActivity", "graphItems changed: size=${items.size}")
            if (items.isNotEmpty()) {
                adapter.submitList(items)
                recyclerView.invalidateItemDecorations()
            } else {
                Log.w("WeatherActivity", "graphItems is empty – check network or API key")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requestWeatherData()
    }

    private fun requestWeatherData() {
        Log.d("WeatherActivity", "requestWeatherData: loading for $lat, $lon")
        viewModel.loadWeatherData(lat, lon)
    }
}