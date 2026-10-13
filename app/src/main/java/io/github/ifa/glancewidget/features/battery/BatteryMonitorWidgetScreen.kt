package io.github.ifa.glancewidget.features.battery

import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Aod
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.ifa.glancewidget.MainActivity
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.battery.component.AddWidgetBottomSheet
import io.github.ifa.glancewidget.features.battery.component.DropdownMenu
import io.github.ifa.glancewidget.features.battery.detail.ChartDetailDestination
import io.github.ifa.glancewidget.features.battery.wattsmonitor.WattsDetailDestination
import io.github.ifa.glancewidget.glance.battery.BatteryWidgetReceiver.Companion.PINNED_WIDGET_DEFAULT_ID
import io.github.ifa.glancewidget.model.AddWidgetParams
import io.github.ifa.glancewidget.model.BatteryData
import io.github.ifa.glancewidget.model.ChargeDisChargeCurrent
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.DeviceType
import io.github.ifa.glancewidget.model.ExtraBatteryInfo
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.model.wrapper.PowerDetails
import io.github.ifa.glancewidget.ui.designsystem.components.AppScaffold
import io.github.ifa.glancewidget.ui.designsystem.components.ChipData
import io.github.ifa.glancewidget.ui.designsystem.components.HeroBattery
import io.github.ifa.glancewidget.ui.designsystem.components.InfoBanner
import io.github.ifa.glancewidget.ui.designsystem.components.MetricTile
import io.github.ifa.glancewidget.ui.designsystem.components.SectionHeader
import io.github.ifa.glancewidget.ui.designsystem.components.SettingsRow
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.gravity.gravityItem
import io.github.ifa.glancewidget.ui.model.asString
import io.github.ifa.glancewidget.ui.model.toUiModel
import io.github.ifa.glancewidget.utils.addWidget
import io.github.ifa.glancewidget.utils.findActivity
import io.github.ifa.glancewidget.utils.requestToPinWidget
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import java.util.Locale

const val batteryMonitorScreenRoute = "battery_monitor_screen_route"

fun NavGraphBuilder.batteryMonitorScreen(
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onOpenWattsDetailScreen: (WattsDetailDestination) -> Unit,
    onOpenChartDetailScreen: (ChartDetailDestination) -> Unit = {},
    onOpenInquiryScreen: () -> Unit,
) {
    composable(batteryMonitorScreenRoute) {
        BatteryMonitorScreen(
            onOpenWattsDetailScreen = onOpenWattsDetailScreen,
            onOpenChartDetailScreen = onOpenChartDetailScreen,
            onOpenInquiryScreen = onOpenInquiryScreen,
            snackbarHostState = snackbarHostState,
            contentPadding = contentPadding,
        )
    }
}

@Composable
internal fun BatteryMonitorScreen(
    viewModel: BatteryMonitorViewModel = hiltViewModel(),
    contentPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onOpenWattsDetailScreen: (WattsDetailDestination) -> Unit,
    onOpenChartDetailScreen: (ChartDetailDestination) -> Unit = {},
    onOpenInquiryScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showBottomSheet by rememberUpdatedState(uiState.setupWidgetId != INVALID_APPWIDGET_ID)
    val context = LocalContext.current
    val activity = context.findActivity()

    val scope = rememberCoroutineScope()
    val addPinnedWidgetMessage = stringResource(id = R.string.pinned_widget_added)
    LaunchedEffect(Unit) {
        val intent = activity?.intent ?: return@LaunchedEffect
        viewModel.controlExtras(intent)
    }
    BatteryMonitorScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        isShowAddWidgetBottomSheet = showBottomSheet,
        onOpenWattsDetailScreen = onOpenWattsDetailScreen,
        onOpenChartDetailScreen = onOpenChartDetailScreen,
        onDisMissBottomSheet = viewModel::hideBottomSheet,
        onClickAddWidget = { params ->
            if (uiState.setupWidgetId == PINNED_WIDGET_DEFAULT_ID) {
                val isSupported = activity?.requestToPinWidget(params)
                if (isSupported == true) {
                    scope.launch {
                        snackbarHostState.showSnackbar(addPinnedWidgetMessage)
                    }
                    viewModel.hideBottomSheet()
                }
            } else {
                viewModel.setWidgetSetting(
                    appWidgetId = uiState.setupWidgetId,
                    params = params
                )
                activity?.addWidget(uiState.setupWidgetId)
            }
        },
        onRequestPiningWidget = viewModel::createPinnedWidget,
        onForceReloadClick = {
            activity?.apply {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
                startActivity(intent)
            }
        },
        onOpenInquiryScreen = onOpenInquiryScreen,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryMonitorScreen(
    uiState: BatteryMonitorViewModel.BatteryMonitorScreenUiState,
    contentPadding: PaddingValues,
    isShowAddWidgetBottomSheet: Boolean = false,
    onOpenWattsDetailScreen: (WattsDetailDestination) -> Unit,
    onOpenChartDetailScreen: (ChartDetailDestination) -> Unit = {},
    onDisMissBottomSheet: () -> Unit = {},
    onClickAddWidget: (AddWidgetParams) -> Unit,
    onRequestPiningWidget: () -> Unit = {},
    onForceReloadClick: () -> Unit,
    onOpenInquiryScreen: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current
    val locale = LocalConfiguration.current.locales[0]
    var dropdownExpanded by remember { mutableStateOf(false) }

    AppScaffold(
        title = stringResource(id = R.string.battery),
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(onClick = onRequestPiningWidget) {
                Icon(
                    imageVector = Icons.Rounded.Widgets,
                    contentDescription = stringResource(id = R.string.add_widget)
                )
            }
            IconButton(onClick = { dropdownExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                onForceReloadClick = {
                    onForceReloadClick()
                    dropdownExpanded = false
                },
                onOpenInquiryScreen = onOpenInquiryScreen
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            contentPadding = scaffoldPadding,
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.measurementProgress < 100f) {
                item {
                    InfoBanner(
                        text = stringResource(id = R.string.measurement_in_progress_description),
                        icon = Icons.Rounded.Info,
                        progress = uiState.measurementProgress,
                        modifier = Modifier.gravityItem("banner")
                    )
                }
            }

            item {
                val heroModel = uiState.batteryOverall.toUiModel(temperatureUnit = uiState.temperatureUnit)
                val heroChips = listOfNotNull(
                    heroModel.chargerChip?.let { ChipData(it.asString(), Icons.Rounded.Bolt) },
                    ChipData(heroModel.powerText, Icons.Rounded.ElectricBolt),
                    ChipData(heroModel.temperatureText, Icons.Rounded.Thermostat)
                )
                HeroBattery(
                    model = heroModel,
                    chips = heroChips,
                    modifier = Modifier.gravityItem("hero")
                )
            }

            item {
                val myDevice = uiState.batteryOverall.batteryData.myDevice
                val extraInfo = uiState.batteryOverall.extraBatteryInfo
                val powerWatts = extraInfo.powerInWatt(myDevice.voltage)
                val powerPercentage = powerWatts.toFloat() / extraInfo.maxWattsChargeInput
                val powerText = String.format(locale, "%.1f", powerWatts)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gravityItem("power_current"),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    MetricTile(
                        label = stringResource(id = R.string.power),
                        value = powerText,
                        unit = "W",
                        icon = Icons.Rounded.ElectricBolt,
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = {
                            onOpenWattsDetailScreen(
                                WattsDetailDestination(
                                    power = powerWatts.toFloat(),
                                    powerPercentage = powerPercentage
                                )
                            )
                        }
                    )
                    MetricTile(
                        label = stringResource(id = R.string.current),
                        value = "${extraInfo.chargeCurrent}",
                        unit = "mA",
                        icon = Icons.Rounded.Waves,
                        modifier = Modifier.weight(1f),
                        accent = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            item {
                val myDevice = uiState.batteryOverall.batteryData.myDevice
                val isFahrenheit = uiState.temperatureUnit == MyDevice.Temperature.TemperatureUnit.FAHRENHEIT
                val tempCelsius = myDevice.temperature.temperature
                val displayTemp = if (isFahrenheit) (tempCelsius * 1.8f) + 32f else tempCelsius
                val tempValue = String.format(locale, "%.1f", displayTemp)
                val tempUnit = if (isFahrenheit) "°F" else "°C"

                val temperatureSeries = remember(uiState.chartTrackingData.temperatures, uiState.temperatureUnit) {
                    if (isFahrenheit) {
                        uiState.chartTrackingData.temperatures.map { it * 1.8f + 32f }.toPersistentList()
                    } else {
                        uiState.chartTrackingData.temperatures.toPersistentList()
                    }
                }
                val voltageSeries = remember(uiState.chartTrackingData.voltages) {
                    uiState.chartTrackingData.voltages.toPersistentList()
                }
                val voltageText = String.format(locale, "%.2f", myDevice.voltage)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .gravityItem("temp_voltage"),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    MetricTile(
                        label = stringResource(id = R.string.temperature),
                        value = tempValue,
                        unit = tempUnit,
                        icon = Icons.Rounded.Thermostat,
                        modifier = Modifier.weight(1f),
                        series = temperatureSeries,
                        accent = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            onOpenChartDetailScreen(
                                ChartDetailDestination(
                                    metricType = ChartDetailDestination.METRIC_TEMPERATURE
                                )
                            )
                        }
                    )
                    MetricTile(
                        label = stringResource(id = R.string.voltage),
                        value = voltageText,
                        unit = "V",
                        icon = Icons.Rounded.BatteryChargingFull,
                        modifier = Modifier.weight(1f),
                        series = voltageSeries,
                        accent = MaterialTheme.colorScheme.primary,
                        onClick = {
                            onOpenChartDetailScreen(
                                ChartDetailDestination(
                                    metricType = ChartDetailDestination.METRIC_VOLTAGE
                                )
                            )
                        }
                    )
                }
            }

            val connectedDevices = uiState.batteryOverall.batteryData.batteryConnectedDevices
            if (connectedDevices.isNotEmpty()) {
                item {
                    SectionHeader(text = stringResource(id = R.string.connected_devices))
                }
                items(connectedDevices, key = { it.address }) { device ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .gravityItem("device_${device.address}"),
                        shape = AppShape.card,
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.l, vertical = Spacing.m),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = device.deviceType.icon),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(Spacing.m))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${device.batteryInPercentage}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                var isAodChecked by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShape.card,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    SettingsRow(
                        title = stringResource(id = R.string.always_on_display),
                        subtitle = stringResource(id = R.string.always_on_display_desc),
                        leadingIcon = Icons.Rounded.Aod,
                        trailing = {
                            Switch(
                                checked = isAodChecked,
                                onCheckedChange = { checked ->
                                    isAodChecked = checked
                                    if (checked) {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }

        if (isShowAddWidgetBottomSheet) {
            AddWidgetBottomSheet(
                uiState = uiState,
                onDisMiss = onDisMissBottomSheet,
                onClickAddWidget = onClickAddWidget
            )
        }
    }
}

@ThemePreviews
@Composable
fun BatteryMonitorScreenPreview() {
    MaterialTheme {
        val sampleMyDevice = MyDevice(
            level = 75,
            isCharging = true,
            temperature = MyDevice.Temperature(temperature = 31.5f),
            voltage = 4.12f,
            scale = 100
        )
        val sampleBatteryData = BatteryData(
            myDevice = sampleMyDevice,
            batteryConnectedDevices = emptyList()
        )
        val sampleExtraBatteryInfo = ExtraBatteryInfo(
            capacity = 5000,
            fullChargeCapacity = 4800,
            chargeCounter = 3600,
            chargeCurrent = 1800
        )
        val sampleUiState = BatteryMonitorViewModel.BatteryMonitorScreenUiState(
            batteryOverall = BatteryDataWrapper(
                batteryData = sampleBatteryData,
                extraBatteryInfo = sampleExtraBatteryInfo,
                powerDetails = PowerDetails(7.4f, 0.4f),
                chargeDisChargeCurrent = ChargeDisChargeCurrent()
            ),
            chartTrackingData = ChartRecord(
                temperatures = listOf(30f, 31f, 31.5f),
                voltages = listOf(4.1f, 4.11f, 4.12f)
            ),
            temperatureUnit = MyDevice.Temperature.TemperatureUnit.CELSIUS
        )
        BatteryMonitorScreen(
            uiState = sampleUiState,
            contentPadding = PaddingValues(0.dp),
            isShowAddWidgetBottomSheet = false,
            onOpenWattsDetailScreen = {},
            onOpenChartDetailScreen = {},
            onClickAddWidget = {},
            onForceReloadClick = {},
            onOpenInquiryScreen = {}
        )
    }
}
