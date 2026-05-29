package com.parshikov.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.parshikov.weatherapp.ui.SavedCitiesActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var settingsChanged = false   // флаг, что настройки менялись

    // Лаунчер для запроса разрешений геолокации
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Для определения местоположения необходимо разрешение", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Лаунчер для получения результата из SavedCitiesActivity
    private val cityPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("lat", Double.NaN) ?: return@registerForActivityResult
            val lon = result.data?.getDoubleExtra("lon", Double.NaN) ?: return@registerForActivityResult
            if (!lat.isNaN() && !lon.isNaN()) {
                viewModel.loadWeatherData(lat, lon, force = true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация клиента геолокации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Кнопка "+" – список городов
        findViewById<ImageView>(R.id.settingsButton1).setOnClickListener {
            cityPickerLauncher.launch(Intent(this, SavedCitiesActivity::class.java))
        }

        // Кнопка "Службы местоположения"
        findViewById<TextView>(R.id.locationSettingsLink).setOnClickListener {
            requestLocationPermission()
        }

        // Кнопка "Показать полностью" – график на 5 дней
        val showFullForecast: TextView = findViewById(R.id.showFullForecast)
        showFullForecast.setOnClickListener {
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
                val state = viewModel.uiState.value
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("lat", state?.lat ?: 51.3737)
                    putExtra("lon", state?.lon ?: 42.0889)
                    putExtra("cityName", state?.cityName ?: "Прогноз")
                }
                startActivity(intent)
            }
        }

        // Кнопка "три точки" – настройки
        findViewById<ImageView>(R.id.settingsButton2).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            settingsChanged = true   // запоминаем, что были в настройках
        }

        // Загружаем погоду по умолчанию
        viewModel.loadWeatherData()

        // Наблюдаем за UI
        viewModel.uiState.observe(this, Observer { state ->
            updateUI(state)
        })
    }

    override fun onResume() {
        super.onResume()
        if (settingsChanged) {
            viewModel.loadWeatherData()
            settingsChanged = false
        }
    }

    /** Проверяет наличие разрешений и либо запрашивает их, либо сразу получает координаты */
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** Получает текущее местоположение с высоким приоритетом */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Нет разрешения на геолокацию", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Определяем местоположение...", Toast.LENGTH_SHORT).show()

        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(currentLocationRequest, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("MainActivity", "Current location: lat=$lat, lon=$lon")
                    viewModel.loadWeatherData(lat, lon, force = true)
                } else {
                    tryGetLastLocation()
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to get location", e)
                tryGetLastLocation()
            }
    }

    /** Запасной метод, если getCurrentLocation не сработал */
    @SuppressLint("MissingPermission")
    private fun tryGetLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    Log.d("MainActivity", "Last known location: lat=$lat, lon=$lon")
                    viewModel.loadWeatherData(lat, lon, force = true)
                } else {
                    Toast.makeText(this, "Не удалось определить местоположение. Включите GPS и попробуйте снова.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Не удалось определить местоположение. Включите GPS и попробуйте снова.", Toast.LENGTH_LONG).show()
            }
    }

    /** Обновляет все текстовые поля на главном экране */
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