package io.github.ifa.glancewidget.ui.glass

import kotlin.math.roundToInt

object DotRingGeometry {
    fun calculateLitCount(progress: Float, totalDots: Int): Int {
        return (progress.coerceIn(0f, 1f) * totalDots).roundToInt().coerceIn(0, totalDots)
    }

    fun calculateHeadIndex(progress: Float, totalDots: Int): Int {
        val litCount = calculateLitCount(progress, totalDots)
        return litCount - 1
    }
}
