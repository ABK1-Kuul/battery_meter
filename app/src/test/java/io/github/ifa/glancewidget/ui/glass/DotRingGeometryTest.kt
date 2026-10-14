package io.github.ifa.glancewidget.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class DotRingGeometryTest {

    @Test
    fun litDotsCalculatesCorrectlyForVariousProgressLevels() {
        val totalDots = 48

        val zeroLit = (0f.coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)
        assertEquals(0, zeroLit)

        val halfLit = (0.5f.coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)
        assertEquals(24, halfLit)

        val fullLit = (1.0f.coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)
        assertEquals(48, fullLit)

        val overLit = (1.5f.coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)
        assertEquals(48, overLit)

        val underLit = ((-0.5f).coerceIn(0f, 1f) * totalDots).toInt().coerceIn(0, totalDots)
        assertEquals(0, underLit)
    }

    @Test
    fun dotAnglesAreDistributedUniformlyAroundCircle() {
        val totalDots = 48
        val expectedStep = (2f * PI.toFloat()) / totalDots

        for (i in 0 until totalDots) {
            val angle = (i.toFloat() / totalDots) * (2f * PI.toFloat()) - (PI.toFloat() / 2f)
            if (i == 0) {
                assertTrue(abs(cos(angle)) < 1e-5f)
                assertTrue(abs(sin(angle) - (-1f)) < 1e-5f)
            } else if (i == 12) {
                assertTrue(abs(cos(angle) - 1f) < 1e-5f)
                assertTrue(abs(sin(angle)) < 1e-5f)
            } else if (i == 24) {
                assertTrue(abs(cos(angle)) < 1e-5f)
                assertTrue(abs(sin(angle) - 1f) < 1e-5f)
            } else if (i == 36) {
                assertTrue(abs(cos(angle) - (-1f)) < 1e-5f)
                assertTrue(abs(sin(angle)) < 1e-5f)
            }
        }
    }
}
