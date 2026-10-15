package io.github.ifa.glancewidget.features.health.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChangeCircle
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.model.ExtraBatteryInfo
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.ui.component.AnimatedCounter
import io.github.ifa.glancewidget.ui.designsystem.components.MetricTile
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.StatusChip
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppMotion
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.DotMatrixText
import io.github.ifa.glancewidget.ui.glass.DotRingIndicator
import io.github.ifa.glancewidget.ui.glass.GlassBorder
import io.github.ifa.glancewidget.ui.glass.GlassSurface
import io.github.ifa.glancewidget.ui.glass.GlassTextPrimary
import io.github.ifa.glancewidget.ui.glass.GlassTextSecondary
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel
import io.github.ifa.glancewidget.ui.theme.metricLarge
import io.github.ifa.glancewidget.ui.theme.metricSmall
import io.github.ifa.glancewidget.utils.Constants.MAH_UNIT
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private fun calculateCapacityHealth(extraBatteryInfo: ExtraBatteryInfo): Int? {
    val full = extraBatteryInfo.fullChargeCapacity
    val design = extraBatteryInfo.capacity
    return if (full in 500..20000 && design in 500..20000) {
        ((full.toFloat() / design.toFloat()) * 100f).roundToInt().coerceIn(1, 100)
    } else {
        null
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HealthHeroCard(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice
    val extraInfo = batteryDataWrapper.extraBatteryInfo
    val batteryHealth = batteryDataWrapper.batteryHealth
    val isGlassTheme = LocalIsGlassTheme.current

    val capacityHealthPercent = calculateCapacityHealth(extraInfo)
    val hasMeasuredCapacity = capacityHealthPercent != null
    val displayPercent = capacityHealthPercent ?: myDevice.level

    val targetProgress = if (hasMeasuredCapacity) {
        displayPercent / 100f
    } else {
        (myDevice.level / 100f).coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = AppMotion.spatial(),
        label = "healthRingProgress"
    )

    val labelText = if (hasMeasuredCapacity) {
        stringResource(id = R.string.estimated_capacity_health)
    } else {
        stringResource(id = R.string.hardware_diagnostic)
    }

    val healthDescription = "$displayPercent% $labelText, ${stringResource(id = batteryHealth.type)}"

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = healthDescription
                },
            shape = AppShape.hero
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.padding(Spacing.m),
                    contentAlignment = Alignment.Center
                ) {
                    DotRingIndicator(
                        progress = animatedProgress,
                        totalDots = 48,
                        ringSize = 180.dp,
                        dotRadius = 3.dp,
                        litColor = MaterialTheme.colorScheme.primary,
                        unlitColor = GlassBorder
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DotMatrixText(
                            text = "$displayPercent%",
                            dotSize = 3.dp,
                            dotSpacing = 1.dp,
                            charSpacing = 3.dp,
                            tint = GlassTextPrimary
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = labelText.uppercase(),
                            style = MaterialTheme.typography.glassLabel,
                            color = GlassTextSecondary
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        StatusChip(
                            label = stringResource(id = batteryHealth.type),
                            icon = Icons.Rounded.Favorite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusChip(
                        label = if (myDevice.isCharging) {
                            if (myDevice.chargeType.type.isNotBlank()) myDevice.chargeType.type else stringResource(R.string.charging)
                        } else {
                            stringResource(R.string.discharging)
                        },
                        icon = Icons.Rounded.Bolt
                    )
                    if (myDevice.temperature.temperature > 0f) {
                        StatusChip(
                            label = myDevice.temperature.formatTemperature(),
                            icon = Icons.Rounded.Thermostat
                        )
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = healthDescription
                },
            shape = AppShape.hero,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.padding(Spacing.m),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(180.dp),
                        stroke = Stroke(
                            width = 16f,
                            cap = StrokeCap.Round,
                        ),
                        wavelength = 32.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            AnimatedCounter(
                                count = displayPercent,
                                style = MaterialTheme.typography.metricLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = Spacing.s)
                            )
                        }
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.m))
                        StatusChip(
                            label = stringResource(id = batteryHealth.type),
                            icon = Icons.Rounded.Favorite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.m))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusChip(
                        label = if (myDevice.isCharging) {
                            if (myDevice.chargeType.type.isNotBlank()) myDevice.chargeType.type else stringResource(R.string.charging)
                        } else {
                            stringResource(R.string.discharging)
                        },
                        icon = Icons.Rounded.Bolt
                    )
                    if (myDevice.temperature.temperature > 0f) {
                        StatusChip(
                            label = myDevice.temperature.formatTemperature(),
                            icon = Icons.Rounded.Thermostat
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthMetricsRow(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice

    val voltageText = if (myDevice.voltage > 0f) {
        String.format(Locale.getDefault(), "%.2f", myDevice.voltage)
    } else {
        "--"
    }

    val tempText = if (myDevice.temperature.temperature > 0f) {
        String.format(Locale.getDefault(), "%.1f", myDevice.temperature.temperature)
    } else {
        "--"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        MetricTile(
            label = stringResource(id = R.string.voltage),
            value = voltageText,
            unit = "V",
            icon = Icons.Rounded.ElectricBolt,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            label = stringResource(id = R.string.temperature),
            value = tempText,
            unit = myDevice.temperature.temperatureUnit.unit,
            icon = Icons.Rounded.Thermostat,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HealthMetricsSecondRow(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice
    val extraInfo = batteryDataWrapper.extraBatteryInfo
    val capacityHealthPercent = calculateCapacityHealth(extraInfo)

    val currentMa = extraInfo.chargeCurrent
    val currentText = if (currentMa != 0 && currentMa != Int.MIN_VALUE) {
        val sign = if (currentMa > 0) "+" else ""
        "$sign$currentMa"
    } else {
        "--"
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        MetricTile(
            label = stringResource(id = R.string.current),
            value = currentText,
            unit = "mA",
            icon = Icons.Rounded.Speed,
            modifier = Modifier.weight(1f)
        )

        if (capacityHealthPercent != null) {
            val wearPercent = (100 - capacityHealthPercent).coerceAtLeast(0)
            MetricTile(
                label = stringResource(id = R.string.wear_level),
                value = "$wearPercent",
                unit = "%",
                icon = Icons.Rounded.ChangeCircle,
                modifier = Modifier.weight(1f)
            )
        } else {
            val cycleText = if (myDevice.cycleCount > 0) myDevice.cycleCount.toString() else "--"
            MetricTile(
                label = stringResource(id = R.string.cycle_count),
                value = cycleText,
                unit = "",
                icon = Icons.Rounded.ChangeCircle,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HealthCapacityGroup(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice
    val extraInfo = batteryDataWrapper.extraBatteryInfo
    val isGlassTheme = LocalIsGlassTheme.current
    val context = LocalContext.current

    val hasMeasuredCapacity = calculateCapacityHealth(extraInfo) != null
    val rowValueStyle = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.metricSmall
    val rowValueColor = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onSurface

    val designCapacityText = if (extraInfo.capacity in 500..20000) {
        "${extraInfo.capacity} $MAH_UNIT"
    } else {
        "--"
    }

    val fullCapacityText = if (hasMeasuredCapacity) {
        "${extraInfo.fullChargeCapacity} $MAH_UNIT"
    } else {
        stringResource(id = R.string.calibrating_capacity)
    }

    val fullCapacitySubtitle = if (!hasMeasuredCapacity) {
        stringResource(id = R.string.calibrating_capacity_desc)
    } else {
        null
    }

    val currentChargeText = if (extraInfo.chargeCounter > 0) {
        "${extraInfo.chargeCounter} $MAH_UNIT"
    } else if (extraInfo.capacity in 500..20000 && myDevice.level > 0) {
        "${(extraInfo.capacity * myDevice.level / 100)} $MAH_UNIT"
    } else {
        "--"
    }

    val timeRemaining = if (myDevice.isCharging) {
        batteryDataWrapper.remainChargeTime(context)
    } else {
        batteryDataWrapper.remainBatteryTime(context)
    }

    SettingsGroup(
        title = stringResource(id = R.string.capacity),
        modifier = modifier
    ) {
        SettingsRow(
            title = stringResource(id = R.string.design_capacity),
            leadingIcon = Icons.Rounded.BatterySaver,
            trailing = {
                Text(
                    text = designCapacityText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.l),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SettingsRow(
            title = stringResource(id = R.string.full_charge_capacity),
            subtitle = fullCapacitySubtitle,
            leadingIcon = Icons.Rounded.BatteryFull,
            trailing = {
                Text(
                    text = fullCapacityText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.l),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SettingsRow(
            title = stringResource(id = R.string.charge_counter),
            leadingIcon = Icons.Rounded.Refresh,
            trailing = {
                Text(
                    text = currentChargeText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        if (timeRemaining.isNotBlank()) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.l),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            SettingsRow(
                title = stringResource(id = if (myDevice.isCharging) R.string.remain_time_charging else R.string.remain_time_battery),
                leadingIcon = Icons.Rounded.Timer,
                trailing = {
                    Text(
                        text = timeRemaining,
                        style = rowValueStyle,
                        color = rowValueColor
                    )
                }
            )
        }
    }
}

@Composable
fun HealthHardwareGroup(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice
    val batteryHealth = batteryDataWrapper.batteryHealth
    val isGlassTheme = LocalIsGlassTheme.current

    val rowValueStyle = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.metricSmall
    val rowValueColor = if (isGlassTheme) GlassTextPrimary else MaterialTheme.colorScheme.onSurface

    val techText = if (myDevice.technology.isNotBlank()) myDevice.technology else "Li-ion"
    val powerSourceText = if (myDevice.isCharging) {
        if (myDevice.chargeType.type.isNotBlank()) myDevice.chargeType.type else stringResource(id = R.string.charging)
    } else {
        stringResource(id = R.string.discharging)
    }
    val cycleText = if (myDevice.cycleCount > 0) myDevice.cycleCount.toString() else "--"

    SettingsGroup(
        title = stringResource(id = R.string.technology),
        modifier = modifier
    ) {
        SettingsRow(
            title = stringResource(id = R.string.health),
            leadingIcon = Icons.Rounded.Favorite,
            trailing = {
                Text(
                    text = stringResource(id = batteryHealth.type),
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.l),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SettingsRow(
            title = stringResource(id = R.string.technology),
            leadingIcon = Icons.Rounded.Build,
            trailing = {
                Text(
                    text = techText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.l),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SettingsRow(
            title = stringResource(id = R.string.power_source),
            leadingIcon = Icons.Rounded.Power,
            trailing = {
                Text(
                    text = powerSourceText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = Spacing.l),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        SettingsRow(
            title = stringResource(id = R.string.cycle_count),
            leadingIcon = Icons.Rounded.ChangeCircle,
            trailing = {
                Text(
                    text = cycleText,
                    style = rowValueStyle,
                    color = rowValueColor
                )
            }
        )
    }
}

@Composable
fun HealthLongevityCard(
    modifier: Modifier = Modifier
) {
    val isGlassTheme = LocalIsGlassTheme.current

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.l),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = if (isGlassTheme) {
                        stringResource(id = R.string.battery_longevity_tip_title).uppercase()
                    } else {
                        stringResource(id = R.string.battery_longevity_tip_title)
                    },
                    style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.battery_longevity_tip_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGlassTheme) GlassTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (isGlassTheme) {
        GlassSurface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.card
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = AppShape.card,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            content()
        }
    }
}

@Composable
fun BatteryExtraInformation(
    batteryDataWrapper: BatteryDataWrapper,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        HealthHeroCard(batteryDataWrapper = batteryDataWrapper)
        HealthMetricsRow(batteryDataWrapper = batteryDataWrapper)
        HealthMetricsSecondRow(batteryDataWrapper = batteryDataWrapper)
        HealthCapacityGroup(batteryDataWrapper = batteryDataWrapper)
        HealthHardwareGroup(batteryDataWrapper = batteryDataWrapper)
        HealthLongevityCard()
    }
}

@ThemePreviews
@Composable
fun BatteryExtraInformationPreview() {
    MaterialTheme {
        BatteryExtraInformation(
            batteryDataWrapper = BatteryDataWrapper()
        )
    }
}
