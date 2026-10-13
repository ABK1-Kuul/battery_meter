package io.github.ifa.glancewidget.model

import io.github.ifa.glancewidget.utils.fromJson
import io.github.ifa.glancewidget.utils.toJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartRecordTest {

    @Test
    fun legacyJsonWithoutTimestampsDeserializesWithEmptyTimestamps() {
        val legacyJson = """{"temperatures":[30.5,31.0],"voltages":[4.1,4.2]}"""
        val record = fromJson<ChartRecord>(legacyJson)
        assertNotNull(record)
        assertEquals(listOf(30.5f, 31.0f), record!!.temperatures)
        assertEquals(listOf(4.1f, 4.2f), record.voltages)
        assertTrue(record.timestamps.isEmpty())
    }

    @Test
    fun jsonWithTimestampsDeserializesCorrectly() {
        val json = """{"temperatures":[30.5],"voltages":[4.1],"timestamps":[1700000000000]}"""
        val record = fromJson<ChartRecord>(json)
        assertNotNull(record)
        assertEquals(listOf(30.5f), record!!.temperatures)
        assertEquals(listOf(4.1f), record.voltages)
        assertEquals(listOf(1700000000000L), record.timestamps)
    }

    @Test
    fun serializationRoundTripPreservesAllFields() {
        val original = ChartRecord(
            temperatures = listOf(28.0f, 29.5f),
            voltages = listOf(3.9f, 4.0f),
            timestamps = listOf(1600000000000L, 1600000060000L),
        )
        val serialized = toJson(original)
        val deserialized = fromJson<ChartRecord>(serialized)
        assertEquals(original, deserialized)
    }
}
