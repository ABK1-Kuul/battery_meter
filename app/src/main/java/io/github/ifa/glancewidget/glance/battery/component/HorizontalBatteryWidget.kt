package io.github.ifa.glancewidget.glance.battery.component

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.unit.ColorProvider
import io.github.ifa.glancewidget.glance.battery.BatteryWidget.Companion.PADDING
import io.github.ifa.glancewidget.glance.battery.utils.cornerRadiusCompat
import io.github.ifa.glancewidget.model.BatteryData
import io.github.ifa.glancewidget.model.BonedDevice
import io.github.ifa.glancewidget.model.DeviceType
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.ui.model.BatteryTone

@SuppressLint("RestrictedApi")
@Composable
fun HorizontalBatteryWidget(
    battery: BatteryData?,
    percent: Int,
    isCharging: Boolean,
    isTransparent: Boolean,
    connectedDevice: List<BonedDevice>,
    sizeWidget: WidgetSetting.Type,
    tone: BatteryTone = BatteryTone.Normal,
) {
    val transparent by rememberUpdatedState(isTransparent)
    val deviceType by remember(battery) {
        derivedStateOf { battery?.myDevice?.deviceType ?: DeviceType.PHONE }
    }
    val deviceName by remember(battery) {
        derivedStateOf { battery?.myDevice?.name.toString() }
    }
    val charging by rememberUpdatedState(isCharging)
    val connectedDevices by remember(connectedDevice, sizeWidget) {
        derivedStateOf { connectedDevice.take(sizeWidget.itemOnSizeForHorizontal()) }
    }

    Box(
        modifier = GlanceModifier.fillMaxSize()
            .padding(PADDING)
            .cornerRadiusCompat(
                24,
                if (transparent) {
                    ColorProvider(Color.Transparent)
                } else {
                    GlanceTheme.colors.widgetBackground
                }
            ),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            BatteryItem(
                deviceType = deviceType,
                percent = percent,
                isCharging = charging,
                deviceName = deviceName,
                tone = tone,
                modifier = GlanceModifier.defaultWeight()
            )
            if (connectedDevice.isNotEmpty()) {
                when (sizeWidget) {
                    WidgetSetting.Type.FullWidex1, WidgetSetting.Type.Wide -> {
                        GridWrapItem(
                            connectedDevice = connectedDevices,
                            modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                        )
                    }

                    else -> {
                        FullWidthItem(
                            connectedDevices = connectedDevices,
                            modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 100, heightDp = 100)
@Composable
fun HorizontalBatteryWidget1x1Preview() {
    HorizontalBatteryWidget(
        battery = null,
        percent = 85,
        isCharging = false,
        isTransparent = false,
        connectedDevice = emptyList(),
        sizeWidget = WidgetSetting.Type.Smallest,
        tone = BatteryTone.Normal
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 100)
@Composable
fun HorizontalBatteryWidget2x1Preview() {
    HorizontalBatteryWidget(
        battery = null,
        percent = 85,
        isCharging = true,
        isTransparent = false,
        connectedDevice = emptyList(),
        sizeWidget = WidgetSetting.Type.Small,
        tone = BatteryTone.Charging
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 350, heightDp = 100)
@Composable
fun HorizontalBatteryWidget4x1Preview() {
    HorizontalBatteryWidget(
        battery = null,
        percent = 12,
        isCharging = false,
        isTransparent = false,
        connectedDevice = listOf(BonedDevice(batteryInPercentage = 60)),
        sizeWidget = WidgetSetting.Type.FullWidex1,
        tone = BatteryTone.Low
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 200, heightDp = 200)
@Composable
fun HorizontalBatteryWidget2x2Preview() {
    HorizontalBatteryWidget(
        battery = null,
        percent = 85,
        isCharging = false,
        isTransparent = false,
        connectedDevice = listOf(BonedDevice(batteryInPercentage = 45)),
        sizeWidget = WidgetSetting.Type.Square,
        tone = BatteryTone.Normal
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 350, heightDp = 350)
@Composable
fun HorizontalBatteryWidget4x4Preview() {
    HorizontalBatteryWidget(
        battery = null,
        percent = 100,
        isCharging = true,
        isTransparent = false,
        connectedDevice = listOf(BonedDevice(batteryInPercentage = 75), BonedDevice(batteryInPercentage = 50)),
        sizeWidget = WidgetSetting.Type.Large,
        tone = BatteryTone.Full
    )
}
