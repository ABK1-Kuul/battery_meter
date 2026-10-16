package io.github.ifa.glancewidget.features.settings.component

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.settings.SettingsViewModel
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.glass.LocalIsGlassTheme
import io.github.ifa.glancewidget.ui.theme.glassLabel
import kotlinx.coroutines.launch

@SuppressLint("LocalContextConfigurationRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureUnitSetting(
    uiState: SettingsViewModel.SettingsScreenUiState,
    onSelectTemperatureUnit: (MyDevice.Temperature.TemperatureUnit) -> Unit
) {
    val selectedUnit = uiState.temperatureUnit ?: MyDevice.Temperature.TemperatureUnit.CELSIUS
    val sheetState = rememberModalBottomSheetState()
    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isGlassTheme = LocalIsGlassTheme.current

    SettingsRow(
        title = stringResource(R.string.temperature_unit),
        subtitle = if (selectedUnit == MyDevice.Temperature.TemperatureUnit.CELSIUS) {
            stringResource(R.string.celsius_unit)
        } else {
            stringResource(R.string.fahrenheit_unit)
        },
        leadingPainter = painterResource(R.drawable.ic_device_thermostat),
        trailing = {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        onClick = {
            if (openBottomSheet.value) {
                scope.launch { sheetState.hide() }
            } else {
                openBottomSheet.value = true
                scope.launch { sheetState.show() }
            }
        }
    )

    if (openBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
                scope.launch { sheetState.hide() }
            },
            sheetState = sheetState,
            containerColor = if (isGlassTheme) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl)
            ) {
                Text(
                    text = if (isGlassTheme) stringResource(R.string.temperature_unit).uppercase() else stringResource(R.string.temperature_unit),
                    style = if (isGlassTheme) MaterialTheme.typography.glassLabel else MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.m)
                )
                val units = MyDevice.Temperature.TemperatureUnit.entries
                units.forEach { unit ->
                    val isSelected = unit == selectedUnit
                    val unitName = if (unit == MyDevice.Temperature.TemperatureUnit.CELSIUS) {
                        stringResource(R.string.celsius_unit)
                    } else {
                        stringResource(R.string.fahrenheit_unit)
                    }
                    Surface(
                        shape = AppShape.card,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else if (isGlassTheme) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.l, vertical = Spacing.xs)
                            .clip(AppShape.card)
                            .clickable {
                                openBottomSheet.value = false
                                scope.launch { sheetState.hide() }
                                onSelectTemperatureUnit(unit)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.xl, vertical = Spacing.l)
                        ) {
                            Text(
                                text = unitName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun TemperatureUnitSettingPreview() {
    MaterialTheme {
        TemperatureUnitSetting(
            uiState = SettingsViewModel.SettingsScreenUiState(
                temperatureUnit = MyDevice.Temperature.TemperatureUnit.CELSIUS
            ),
            onSelectTemperatureUnit = {}
        )
    }
}
