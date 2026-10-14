package io.github.ifa.glancewidget.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SparklineScaleTest {

    @Test
    fun emptyListReturnsAllZeros() {
        val result = SparklineScale.scaleToRows(emptyList(), columnCount = 16, rowCount = 7)
        assertEquals(16, result.size)
        assertTrue(result.all { it == 0 })
    }

    @Test
    fun constantValuesReturnAllZeros() {
        val result = SparklineScale.scaleToRows(listOf(42f, 42f, 42f), columnCount = 16, rowCount = 7)
        assertEquals(16, result.size)
        assertTrue(result.all { it == 0 })
    }

    @Test
    fun scalesMinAndMaxCorrectly() {
        val values = listOf(10f, 20f, 30f, 40f)
        val result = SparklineScale.scaleToRows(values, columnCount = 4, rowCount = 7)
        assertEquals(4, result.size)
        assertEquals(0, result.first())
        assertEquals(6, result.last())
    }

    @Test
    fun handlesUnderAndOverSampledInputs() {
        val twoValues = listOf(0f, 100f)
        val upsampled = SparklineScale.scaleToRows(twoValues, columnCount = 16, rowCount = 7)
        assertEquals(16, upsampled.size)
        assertEquals(0, upsampled.first())
        assertEquals(6, upsampled.last())

        val manyValues = (0..99).map { it.toFloat() }
        val downsampled = SparklineScale.scaleToRows(manyValues, columnCount = 16, rowCount = 7)
        assertEquals(16, downsampled.size)
        assertEquals(0, downsampled.first())
        assertEquals(6, downsampled.last())
        assertTrue(downsampled.all { it in 0..6 })
    }

    @Test
    fun handlesInvalidParametersGracefully() {
        val emptyCols = SparklineScale.scaleToRows(listOf(1f, 2f), columnCount = 0, rowCount = 7)
        assertTrue(emptyCols.isEmpty())

        val emptyRows = SparklineScale.scaleToRows(listOf(1f, 2f), columnCount = 16, rowCount = 0)
        assertTrue(emptyRows.isEmpty())
    }
}
