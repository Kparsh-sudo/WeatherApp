package com.parshikov.weatherapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Toolbar с кнопкой "Назад"
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        prefs = AppPreferences(this)

        // Температура
        val tempRadioGroup = findViewById<RadioGroup>(R.id.temperatureRadioGroup)
        if (prefs.temperatureUnit == "celsius") {
            tempRadioGroup.check(R.id.radioCelsius)
        } else {
            tempRadioGroup.check(R.id.radioFahrenheit)
        }
        tempRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            prefs.temperatureUnit = if (checkedId == R.id.radioCelsius) "celsius" else "fahrenheit"
        }

        // Ветер
        val windSpinner = findViewById<Spinner>(R.id.windUnitSpinner)
        val windUnits = listOf("км/ч (kmh)", "м/с (ms)", "миль/ч (mph)")
        val windValues = listOf("kmh", "ms", "mph")
        windSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, windUnits)
        val currentWindIndex = windValues.indexOf(prefs.windUnit).coerceAtLeast(0)
        windSpinner.setSelection(currentWindIndex)
        windSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.windUnit = windValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Ночной режим
        val nightSwitch = findViewById<SwitchCompat>(R.id.nightModeSwitch)
        nightSwitch.isChecked = prefs.nightModeEnabled
        nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.nightModeEnabled = isChecked
        }
    }
}