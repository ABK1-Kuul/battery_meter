package io.github.ifa.glancewidget.glance.glass

import io.github.ifa.glancewidget.model.WidgetAccent
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.ui.model.BatteryTone
import kotlin.math.roundToInt

data class WidgetBitmapCacheKey(
    val style: WidgetSetting.Style,
    val width: Int,
    val height: Int,
    val level: Int,
    val isCharging: Boolean,
    val tone: BatteryTone,
    val tempRounded: Float,
    val voltRounded: Float,
    val accent: WidgetAccent,
    val opacity: Float
) {
    companion object {
        fun create(
            style: WidgetSetting.Style,
            width: Int,
            height: Int,
            level: Int,
            isCharging: Boolean,
            tone: BatteryTone,
            temperature: Float,
            voltage: Float,
            accent: WidgetAccent,
            opacity: Float
        ): WidgetBitmapCacheKey {
            val roundedTemp = (temperature * 2f).roundToInt() / 2f
            val roundedVolt = (voltage * 20f).roundToInt() / 20f
            val roundedOpacity = (opacity * 20f).roundToInt() / 20f
            return WidgetBitmapCacheKey(
                style = style,
                width = width,
                height = height,
                level = level.coerceIn(0, 100),
                isCharging = isCharging,
                tone = tone,
                tempRounded = roundedTemp,
                voltRounded = roundedVolt,
                accent = accent,
                opacity = roundedOpacity
            )
        }
    }
}
