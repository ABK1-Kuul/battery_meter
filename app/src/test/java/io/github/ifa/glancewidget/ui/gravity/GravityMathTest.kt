package io.github.ifa.glancewidget.ui.gravity

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GravityMathTest {

    @Test
    fun avoidanceFractionBoundaries() {
        val lead = 40f

        assertEquals(1f, avoidanceFraction(distance = 0f, lead = lead), 0.0001f)
        assertEquals(0f, avoidanceFraction(distance = lead, lead = lead), 0.0001f)
        assertEquals(0f, avoidanceFraction(distance = lead + 20f, lead = lead), 0.0001f)
        assertEquals(1f, avoidanceFraction(distance = -15f, lead = lead), 0.0001f)

        val midFraction = avoidanceFraction(distance = 20f, lead = lead)
        assertEquals(0.5f, midFraction, 0.0001f)

        assertEquals(1f, avoidanceFraction(distance = 0f, lead = 0f), 0.0001f)
        assertEquals(0f, avoidanceFraction(distance = 10f, lead = 0f), 0.0001f)
    }

    @Test
    fun avoidanceInsetClamping() {
        val pillStart = 300f
        val gap = 10f

        val nonOverlappingEnd = 280f
        assertEquals(0f, avoidanceInset(rowEnd = nonOverlappingEnd, pillStart = pillStart, gap = gap, fraction = 1f), 0.0001f)

        val exactClearanceEnd = 290f
        assertEquals(0f, avoidanceInset(rowEnd = exactClearanceEnd, pillStart = pillStart, gap = gap, fraction = 1f), 0.0001f)

        val overlappingEnd = 350f
        val fullExpectedInset = 350f - (300f - 10f)
        assertEquals(fullExpectedInset, avoidanceInset(rowEnd = overlappingEnd, pillStart = pillStart, gap = gap, fraction = 1f), 0.0001f)

        assertEquals(0f, avoidanceInset(rowEnd = overlappingEnd, pillStart = pillStart, gap = gap, fraction = 0f), 0.0001f)
        assertEquals(fullExpectedInset * 0.5f, avoidanceInset(rowEnd = overlappingEnd, pillStart = pillStart, gap = gap, fraction = 0.5f), 0.0001f)
    }

    @Test
    fun gravityOrderByDistance() {
        val anchor = Offset(x = 100f, y = 100f)
        val centers = listOf(
            Offset(x = 200f, y = 200f),
            Offset(x = 105f, y = 105f),
            Offset(x = 130f, y = 140f)
        )

        val order = gravityOrder(centers = centers, anchor = anchor)
        assertEquals(listOf(1, 2, 0), order)
    }

    @Test
    fun gravityOrderEmptyAndSingle() {
        val anchor = Offset(x = 50f, y = 50f)
        assertTrue(gravityOrder(emptyList(), anchor).isEmpty())
        assertEquals(listOf(0), gravityOrder(listOf(Offset(10f, 10f)), anchor))
    }

    @Test
    fun gravityDelaysCapping() {
        val count = 10
        val staggerMs = 40
        val capMs = 300

        val delays = gravityDelays(count = count, staggerMs = staggerMs, capMs = capMs)
        val expected = listOf(0, 40, 80, 120, 160, 200, 240, 280, 300, 300)

        assertEquals(expected, delays)
        assertTrue(gravityDelays(0, staggerMs, capMs).isEmpty())
        assertTrue(gravityDelays(-2, staggerMs, capMs).isEmpty())
    }
}
