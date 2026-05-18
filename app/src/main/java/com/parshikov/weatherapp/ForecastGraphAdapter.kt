package com.parshikov.weatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ForecastGraphAdapter(
    private var items: List<ForecastGraphItem>
) : RecyclerView.Adapter<ForecastGraphAdapter.ViewHolder>() {

    companion object {
        private const val NIGHT_SHIFT_DP = 12   // дополнительный сдвиг вниз для синей точки
    }

    private var nightShiftPx: Float = 0f
    private var tempMinGlobal: Int = -30
    private var tempMaxGlobal: Int = 40

    fun submitList(newItems: List<ForecastGraphItem>) {
        items = newItems
        if (items.isNotEmpty()) {
            val allTemps = items.flatMap { listOf(it.maxTempValue, it.minTempValue) }
            tempMinGlobal = allTemps.minOrNull() ?: -30
            tempMaxGlobal = allTemps.maxOrNull() ?: 40
            val padding = ((tempMaxGlobal - tempMinGlobal) * 0.1).toInt().coerceAtLeast(1)
            tempMinGlobal -= padding
            tempMaxGlobal += padding
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast_graph_cell, parent, false)
        nightShiftPx = NIGHT_SHIFT_DP * parent.context.resources.displayMetrics.density
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], tempMinGlobal, tempMaxGlobal, nightShiftPx)
    }

    override fun getItemCount(): Int = items.size

    fun getDayDotView(position: Int): View? {
        if (position < 0 || position >= items.size) return null
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) as? ViewHolder
        return holder?.dayDotView
    }

    fun getNightDotView(position: Int): View? {
        if (position < 0 || position >= items.size) return null
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) as? ViewHolder
        return holder?.nightDotView
    }

    private var recyclerView: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayDotView: View = itemView.findViewById(R.id.dayTempDot)
        val nightDotView: View = itemView.findViewById(R.id.nightTempDot)
        private val dotsContainer: View = itemView.findViewById(R.id.dotsContainer)

        private val dayName: TextView = itemView.findViewById(R.id.dayName)
        private val maxTempDay: TextView = itemView.findViewById(R.id.maxTempDay)
        private val minTempNight: TextView = itemView.findViewById(R.id.minTempNight)

        fun bind(
            item: ForecastGraphItem,
            tempMinGlobal: Int,
            tempMaxGlobal: Int,
            nightShiftPx: Float
        ) {
            dayName.text = item.dayName
            maxTempDay.text = item.maxDayTemp
            minTempNight.text = item.minNightTemp

            // Получим высоту контейнера после измерения
            val containerHeight = dotsContainer.height
            if (containerHeight == 0) {
                // Если ещё не измерен, отложим до следующего кадра
                dotsContainer.post { bind(item, tempMinGlobal, tempMaxGlobal, nightShiftPx) }
                return
            }

            val tempRange = (tempMaxGlobal - tempMinGlobal).coerceAtLeast(1).toFloat()
            val dayFraction = ((item.maxTempValue - tempMinGlobal) / tempRange).coerceIn(0f, 1f)
            val nightFraction = ((item.minTempValue - tempMinGlobal) / tempRange).coerceIn(0f, 1f)

            // Максимальное смещение вниз: высота контейнера минус размер точки и небольшие поля
            val maxOffset = containerHeight - dayDotView.height - 8 // 8dp отступа
            if (maxOffset < 0) return

            // Для самого тёплого дня (fraction = 1) точка вверху (translationY = 0)
            // Для холодного (fraction = 0) — внизу (translationY = maxOffset)
            dayDotView.translationY = maxOffset * (1f - dayFraction)
            nightDotView.translationY = maxOffset * (1f - nightFraction) + nightShiftPx
        }
    }
}