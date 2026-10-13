package io.github.ifa.glancewidget.features.health.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChangeCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.ui.component.AnimatedCounter
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsGroup
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.StatusChip
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.theme.metricLarge
import io.github.ifa.glancewidget.ui.theme.metricSmall
import io.github.ifa.glancewidget.utils.Constants.MAH_UNIT

private fun Int.asMah(): String = if (this > 0) "$this $MAH_UNIT" else "--"

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryExtraInformation(
    modifier: Modifier = Modifier,
    batteryDataWrapper: BatteryDataWrapper,
) {
    val myDevice = batteryDataWrapper.batteryData.myDevice
    val batteryHealth = batteryDataWrapper.batteryHealth
    val extraBatteryInfo = batteryDataWrapper.extraBatteryInfo

    val fullCapacity = extraBatteryInfo.fullChargeCapacity.toFloat()
    val designCapacity = extraBatteryInfo.capacity.toFloat()

    val percentRaw = if (designCapacity > 0) {
        ((fullCapacity / designCapacity) * 100).toInt()
    } else {
        0
    }

    val percent = if (percentRaw in 1..100) percentRaw else 100

    val animatePercentFloat = remember { Animatable(0f) }
    LaunchedEffect(percent) {
        animatePercentFloat.animateTo(
            targetValue = percent / 100f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                        progress = { animatePercentFloat.value },
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
                                count = percent,
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
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        StatusChip(
                            label = stringResource(id = batteryHealth.type),
                            icon = Icons.Rounded.Favorite
                        )
                    }
                }
            }
        }

        SettingsGroup(title = stringResource(id = R.string.capacity)) {
            SettingsRow(
                title = stringResource(id = R.string.design_capacity),
                leadingIcon = Icons.Rounded.BatterySaver,
                trailing = {
                    Text(
                        text = "${extraBatteryInfo.capacity} $MAH_UNIT",
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Spacing.l),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            SettingsRow(
                title = stringResource(id = R.string.full_charge_capacity),
                leadingIcon = Icons.Rounded.BatteryFull,
                trailing = {
                    Text(
                        text = extraBatteryInfo.fullChargeCapacity.asMah(),
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = extraBatteryInfo.chargeCounter.asMah(),
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        SettingsGroup(title = stringResource(id = R.string.health)) {
            SettingsRow(
                title = stringResource(id = R.string.health),
                leadingIcon = Icons.Rounded.Favorite,
                trailing = {
                    Text(
                        text = stringResource(id = batteryHealth.type),
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = myDevice.technology,
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
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
                        text = myDevice.cycleCount.toString(),
                        style = MaterialTheme.typography.metricSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
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
