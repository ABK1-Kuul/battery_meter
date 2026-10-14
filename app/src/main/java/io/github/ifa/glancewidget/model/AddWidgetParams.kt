package io.github.ifa.glancewidget.model

import kotlinx.serialization.Serializable

@Serializable
data class AddWidgetParams(
    val isTransparent: Boolean,
    val widgetStyle: WidgetSetting.Style,
    val accent: WidgetAccent = WidgetAccent.Red,
    val glassOpacity: Float = 1.0f,
)
