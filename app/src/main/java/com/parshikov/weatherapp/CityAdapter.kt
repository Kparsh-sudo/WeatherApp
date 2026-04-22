package com.parshikov.weatherapp.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.parshikov.weatherapp.R
import com.parshikov.weatherapp.data.SavedCity

class CityAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    private var cities: List<SavedCity> = emptyList()

    fun submitList(newList: List<SavedCity>) {
        cities = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position], position)
    }

    override fun getItemCount(): Int = cities.size

    inner class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityNameText: TextView = itemView.findViewById(R.id.cityNameText)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
        private val cardView: CardView = itemView as CardView

        fun bind(city: SavedCity, position: Int) {
            cityNameText.text = city.name

            // Подсветка текущего города
            if (city.isCurrent) {
                cardView.setCardBackgroundColor(0xFFBBDEFB.toInt())
            } else {
                cardView.setCardBackgroundColor(0xFF90CAF9.toInt())
            }

            cardView.setOnClickListener {
                Log.d("CityAdapter", "Item clicked at position $position, city: ${city.name}")
                onItemClick(position)
            }

            deleteButton.setOnClickListener {
                Log.d("CityAdapter", "Delete clicked at position $position, city: ${city.name}")
                onDeleteClick(position)
            }
        }
    }
}