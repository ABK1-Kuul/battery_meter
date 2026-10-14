package io.github.ifa.glancewidget.ui.glass

object SparklineScale {
    fun scaleToRows(
        values: List<Float>,
        columnCount: Int = 16,
        rowCount: Int = 7
    ): List<Int> {
        if (columnCount <= 0 || rowCount <= 0) return emptyList()
        if (values.isEmpty()) return List(columnCount) { 0 }
        val sampled = if (values.size == columnCount) {
            values
        } else if (values.size < columnCount) {
            val padCount = columnCount - values.size
            List(padCount) { values.first() } + values
        } else {
            val step = values.size.toFloat() / columnCount.toFloat()
            (0 until columnCount).map { i ->
                val index = (i * step).toInt().coerceIn(0, values.lastIndex)
                values[index]
            }
        }
        val min = sampled.minOrNull() ?: 0f
        val max = sampled.maxOrNull() ?: 0f
        val range = max - min
        return if (range < 0.0001f) {
            List(columnCount) { 0 }
        } else {
            sampled.map { v ->
                val normalized = ((v - min) / range).coerceIn(0f, 1f)
                (normalized * (rowCount - 1)).toInt().coerceIn(0, rowCount - 1)
            }
        }
    }
}
