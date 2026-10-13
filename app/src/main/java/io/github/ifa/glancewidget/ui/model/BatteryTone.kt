package io.github.ifa.glancewidget.ui.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class BatteryTone {
    Normal,
    Charging,
    Full,
    Low,
    Hot,
    Cold
}

data class ToneColors(
    val container: Color,
    val onContainer: Color,
    val accent: Color
)

fun resolveBatteryTone(
    level: Int,
    isCharging: Boolean,
    temperatureCelsius: Float,
    isOverheat: Boolean
): BatteryTone {
    return when {
        temperatureCelsius >= 42f || isOverheat -> BatteryTone.Hot
        level <= 15 && !isCharging -> BatteryTone.Low
        level >= 100 -> BatteryTone.Full
        isCharging -> BatteryTone.Charging
        temperatureCelsius <= 5f -> BatteryTone.Cold
        else -> BatteryTone.Normal
    }
}

@Composable
fun BatteryTone.colors(): ToneColors {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        BatteryTone.Hot, BatteryTone.Low -> ToneColors(
            container = scheme.errorContainer,
            onContainer = scheme.onErrorContainer,
            accent = scheme.error
        )
        BatteryTone.Full, BatteryTone.Cold -> ToneColors(
            container = scheme.secondaryContainer,
            onContainer = scheme.onSecondaryContainer,
            accent = scheme.secondary
        )
        BatteryTone.Charging -> ToneColors(
            container = scheme.tertiaryContainer,
            onContainer = scheme.onTertiaryContainer,
            accent = scheme.tertiary
        )
        BatteryTone.Normal -> ToneColors(
            container = scheme.primaryContainer,
            onContainer = scheme.onPrimaryContainer,
            accent = scheme.primary
        )
    }
}
