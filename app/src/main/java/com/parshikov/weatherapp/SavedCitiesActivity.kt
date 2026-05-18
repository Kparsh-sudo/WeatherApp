package com.parshikov.weatherapp.ui

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parshikov.weatherapp.R
import com.parshikov.weatherapp.WeatherViewModel
import com.parshikov.weatherapp.data.SavedCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class SavedCitiesActivity : AppCompatActivity() {
    private lateinit var citiesRecyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private val viewModel: WeatherViewModel by viewModels()   // нам нужен только для refreshCities и deleteCity
    private lateinit var adapter: CityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_cities)

        citiesRecyclerView = findViewById(R.id.citiesRecyclerView)
        searchInput = findViewById(R.id.citySearchInput)

        setupRecyclerView()
        loadCities()

        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.searchButton).setOnClickListener {
            performSearch()
        }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    private fun setupRecyclerView() {
        adapter = CityAdapter(
            onItemClick = { position ->
                val cities = viewModel.refreshCities()
                if (position in cities.indices) {
                    val city = cities[position]
                    val resultIntent = Intent().apply {
                        putExtra("lat", city.lat)
                        putExtra("lon", city.lon)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            },
            onDeleteClick = { position ->
                viewModel.deleteCity(position)
                loadCities()
            }
        )
        citiesRecyclerView.layoutManager = LinearLayoutManager(this)
        citiesRecyclerView.adapter = adapter
    }

    private fun loadCities() {
        val cities = viewModel.refreshCities()
        adapter.submitList(cities)
    }

    private fun performSearch() {
        val query = searchInput.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val city = searchCity(query)
                if (city != null) {
                    val resultIntent = Intent().apply {
                        putExtra("lat", city.lat)
                        putExtra("lon", city.lon)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    Toast.makeText(this@SavedCitiesActivity, "Город не найден", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this@SavedCitiesActivity, "Ошибка сети", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SavedCitiesActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun searchCity(query: String): SavedCity? {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(this@SavedCitiesActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val lat = address.latitude
                val lon = address.longitude
                val cityName = address.locality ?: address.adminArea ?: address.subAdminArea ?: query
                SavedCity(name = cityName, lat = lat, lon = lon)
            } else {
                null
            }
        }
    }
}