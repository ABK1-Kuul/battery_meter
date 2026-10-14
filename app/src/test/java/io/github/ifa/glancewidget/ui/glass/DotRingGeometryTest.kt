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
        val totalDots = 44

        val zeroLit = DotRingGeometry.calculateLitCount(0f, totalDots)
        assertEquals(0, zeroLit)
        val zeroHead = DotRingGeometry.calculateHeadIndex(0f, totalDots)
        assertEquals(-1, zeroHead)

        val point46Lit = DotRingGeometry.calculateLitCount(0.46f, totalDots)
        assertEquals(20, point46Lit)
        val point46Head = DotRingGeometry.calculateHeadIndex(0.46f, totalDots)
        assertEquals(19, point46Head)

        val fullLit = DotRingGeometry.calculateLitCount(1.0f, totalDots)
        assertEquals(44, fullLit)
        val fullHead = DotRingGeometry.calculateHeadIndex(1.0f, totalDots)
        assertEquals(43, fullHead)

        val overLit = DotRingGeometry.calculateLitCount(1.5f, totalDots)
        assertEquals(44, overLit)

        val underLit = DotRingGeometry.calculateLitCount(-0.5f, totalDots)
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
