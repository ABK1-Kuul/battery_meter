package io.github.ifa.glancewidget.features.settings.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.settings.SettingsViewModel
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
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
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            val units = MyDevice.Temperature.TemperatureUnit.entries
            units.forEach { unit ->
                val unitName = if (unit == MyDevice.Temperature.TemperatureUnit.CELSIUS) {
                    stringResource(R.string.celsius_unit)
                } else {
                    stringResource(R.string.fahrenheit_unit)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.m)
                        .padding(horizontal = Spacing.l)
                        .height(56.dp)
                        .clip(AppShape.inner)
                        .clickable {
                            openBottomSheet.value = false
                            scope.launch { sheetState.hide() }
                            onSelectTemperatureUnit(unit)
                        }
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            AppShape.inner
                        )
                        .padding(horizontal = Spacing.xl),
                ) {
                    Text(
                        text = unitName,
                        modifier = Modifier,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (unit == selectedUnit) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (unit == selectedUnit) {
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
