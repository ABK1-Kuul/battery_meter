package io.github.ifa.glancewidget.glance.glass

import io.github.ifa.glancewidget.model.WidgetAccent
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.ui.model.BatteryTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetBitmapCacheKeyTest {

    @Test
    fun equalKeysHaveSameHashCode() {
        val key1 = WidgetBitmapCacheKey.create(
            style = WidgetSetting.Style.DotRing,
            width = 300,
            height = 300,
            level = 80,
            isCharging = true,
            tone = BatteryTone.Charging,
            temperature = 25.1f,
            voltage = 4.12f,
            accent = WidgetAccent.Red,
            opacity = 1.0f
        )
        val key2 = WidgetBitmapCacheKey.create(
            style = WidgetSetting.Style.DotRing,
            width = 300,
            height = 300,
            level = 80,
            isCharging = true,
            tone = BatteryTone.Charging,
            temperature = 25.2f,
            voltage = 4.11f,
            accent = WidgetAccent.Red,
            opacity = 1.0f
        )

        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
    }

    @Test
    fun differentParamsProduceDifferentKeys() {
        val key1 = WidgetBitmapCacheKey.create(
            style = WidgetSetting.Style.DotRing,
            width = 300,
            height = 300,
            level = 80,
            isCharging = true,
            tone = BatteryTone.Charging,
            temperature = 25.0f,
            voltage = 4.10f,
            accent = WidgetAccent.Red,
            opacity = 1.0f
        )
        val key2 = WidgetBitmapCacheKey.create(
            style = WidgetSetting.Style.DotRing,
            width = 300,
            height = 300,
            level = 80,
            isCharging = true,
            tone = BatteryTone.Charging,
            temperature = 25.6f,
            voltage = 4.10f,
            accent = WidgetAccent.Red,
            opacity = 1.0f
        )

        assertNotEquals(key1, key2)
    }

    @Test
    fun cacheEvictsLeastRecentlyUsedItem() {
        val cache = WidgetLruCache<Int, String>(maxSizeBytes = 3) { _, str -> str.length }

        cache.put(1, "a")
        cache.put(2, "b")
        cache.put(3, "c")

        assertEquals(3, cache.size())
        assertEquals(3, cache.entryCount())

        cache.get(1)

        cache.put(4, "d")

        assertNull(cache.get(2))
        assertEquals("a", cache.get(1))
        assertEquals("c", cache.get(3))
        assertEquals("d", cache.get(4))
        assertEquals(3, cache.size())
    }
}
