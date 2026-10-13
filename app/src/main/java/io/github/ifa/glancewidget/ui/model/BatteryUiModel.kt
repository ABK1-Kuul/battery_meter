package io.github.ifa.glancewidget.ui.model

import android.os.BatteryManager
import androidx.compose.runtime.Immutable
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import java.util.Locale

@Immutable
data class BatteryUiModel(
    val level: Int,
    val tone: BatteryTone,
    val statusLine: UiText,
    val chargerChip: UiText?,
    val powerText: String,
    val temperatureText: String,
    val voltageText: String,
    val currentText: String,
    val isCharging: Boolean
)

fun BatteryDataWrapper.toUiModel(
    temperatureUnit: MyDevice.Temperature.TemperatureUnit? = null,
    formatDuration: (Long) -> String = { durationMs ->
        val totalSeconds = durationMs / 1000
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val hoursPart = if (hours > 0) "${hours}h" else null
        val minutesPart = if (minutes > 0 || hours == 0) "${minutes}m" else null
        listOfNotNull(hoursPart, minutesPart).joinToString(" ")
    }
): BatteryUiModel {
    val myDevice = batteryData.myDevice
    val isOverheat = myDevice.health == BatteryManager.BATTERY_HEALTH_OVERHEAT
    val tone = resolveBatteryTone(
        level = myDevice.level,
        isCharging = myDevice.isCharging,
        temperatureCelsius = myDevice.temperature.temperature,
        isOverheat = isOverheat
    )

    val remainingDurationMs = if (myDevice.isCharging && myDevice.level < 100) {
        extraBatteryInfo.getChargeTimeRemaining(true, chargeDisChargeCurrent)
    } else {
        extraBatteryInfo.getBatteryTimeRemaining(false, chargeDisChargeCurrent)
    }

    val formattedDuration = if (remainingDurationMs > 0) formatDuration(remainingDurationMs) else ""

    val statusLine = if (formattedDuration.isNotBlank()) {
        val labelRes = if (myDevice.isCharging && myDevice.level < 100) {
            R.string.remain_time_charging
        } else {
            R.string.remain_time_battery
        }
        UiText.Compound(
            prefix = UiText.Resource(labelRes),
            separator = " ",
            suffix = UiText.Plain(formattedDuration)
        )
    } else if (myDevice.isCharging) {
        UiText.Resource(R.string.charging)
    } else {
        UiText.Resource(R.string.discharging)
    }

    val chargerChip = if (myDevice.isCharging && myDevice.chargeType != MyDevice.ChargeType.NONE && myDevice.chargeType.type.isNotBlank()) {
        UiText.Plain(myDevice.chargeType.type)
    } else {
        null
    }

    val powerWatts = extraBatteryInfo.powerInWatt(myDevice.voltage)
    val powerText = String.format(Locale.getDefault(), "%.1f W", powerWatts)

    val displayTemperature = if (temperatureUnit == MyDevice.Temperature.TemperatureUnit.FAHRENHEIT) {
        MyDevice.Temperature(
            temperature = (myDevice.temperature.temperature * 1.8f) + 32f,
            temperatureUnit = MyDevice.Temperature.TemperatureUnit.FAHRENHEIT
        )
    } else {
        myDevice.temperature
    }

    val temperatureText = displayTemperature.formatTemperature()
    val voltageText = String.format(Locale.getDefault(), "%.2f V", myDevice.voltage)
    val currentText = "${extraBatteryInfo.chargeCurrent} mA"

    return BatteryUiModel(
        level = myDevice.level,
        tone = tone,
        statusLine = statusLine,
        chargerChip = chargerChip,
        powerText = powerText,
        temperatureText = temperatureText,
        voltageText = voltageText,
        currentText = currentText,
        isCharging = myDevice.isCharging
    )
}
