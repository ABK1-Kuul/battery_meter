package io.github.ifa.glancewidget.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DotGlyphsTest {

    @Test
    fun allRequiredGlyphsHaveValidDimensions() {
        val chars = listOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '%', '.', ':', '-', '+', '/', 'C', 'c', 'W', 'w',
            'V', 'v', 'A', 'a', 'M', 'm', 'H', 'h', '°', ' '
        )

        for (char in chars) {
            val glyph = DotGlyphs.getGlyph(char)
            assertEquals("Glyph for '$char' must have 7 rows", 7, glyph.size)
            for (rowIndex in glyph.indices) {
                val row = glyph[rowIndex]
                assertEquals("Row $rowIndex of '$char' must have 5 columns", 5, row.length)
                for (colIndex in row.indices) {
                    val bit = row[colIndex]
                    assertTrue(
                        "Bit at ($rowIndex, $colIndex) of '$char' must be '0' or '1'",
                        bit == '0' || bit == '1'
                    )
                }
            }
        }
    }

    @Test
    fun unknownCharacterReturnsBlankGlyph() {
        val glyph = DotGlyphs.getGlyph('?')
        assertEquals(7, glyph.size)
        for (row in glyph) {
            assertEquals("00000", row)
        }
    }

    @Test
    fun isLitReturnsCorrectValues() {
        assertTrue(DotGlyphs.isLit('1', 0, 2))
        assertFalse(DotGlyphs.isLit('1', 0, 0))
        assertFalse(DotGlyphs.isLit('1', -1, 2))
        assertFalse(DotGlyphs.isLit('1', 0, 10))
    }
}
