package com.parshikov.weatherapp

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ForecastGraphItemDecoration(
    private val adapter: ForecastGraphAdapter,
    private val lineColor: Int = 0xFF888888.toInt()
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        color = lineColor
        strokeWidth = 4f
        isAntiAlias = true
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (adapter.itemCount < 2) return

        for (i in 0 until adapter.itemCount - 1) {
            val dayDotCurrent = adapter.getDayDotView(i)
            val dayDotNext = adapter.getDayDotView(i + 1)
            val nightDotCurrent = adapter.getNightDotView(i)
            val nightDotNext = adapter.getNightDotView(i + 1)

            // Дневные точки
            if (dayDotCurrent != null && dayDotNext != null && dayDotCurrent.isShown && dayDotNext.isShown) {
                val (x1, y1) = getCenterInRecyclerView(parent, dayDotCurrent)
                val (x2, y2) = getCenterInRecyclerView(parent, dayDotNext)
                canvas.drawLine(x1, y1, x2, y2, paint)
            }

            // Ночные точки
            if (nightDotCurrent != null && nightDotNext != null && nightDotCurrent.isShown && nightDotNext.isShown) {
                val (x1, y1) = getCenterInRecyclerView(parent, nightDotCurrent)
                val (x2, y2) = getCenterInRecyclerView(parent, nightDotNext)
                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }
    }

    private fun getCenterInRecyclerView(recyclerView: RecyclerView, view: View): Pair<Float, Float> {
        val viewLocation = IntArray(2)
        view.getLocationInWindow(viewLocation)
        val recyclerLocation = IntArray(2)
        recyclerView.getLocationInWindow(recyclerLocation)

        val x = viewLocation[0].toFloat() - recyclerLocation[0] + view.width / 2f
        val y = viewLocation[1].toFloat() - recyclerLocation[1] + view.height / 2f
        return Pair(x, y)
    }
}