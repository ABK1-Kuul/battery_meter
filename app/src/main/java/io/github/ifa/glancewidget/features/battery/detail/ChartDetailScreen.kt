package io.github.ifa.glancewidget.features.battery.detail

import androidx.annotation.Keep
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.ifa.glancewidget.R
import io.github.ifa.glancewidget.features.battery.detail.component.VicoDetailLineChart
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.ui.component.AnimatedTextTopAppBar
import io.github.ifa.glancewidget.ui.component.SelectableChips
import io.github.ifa.glancewidget.ui.designsystem.components.SectionHeader
import io.github.ifa.glancewidget.ui.designsystem.components.ThemePreviews
import io.github.ifa.glancewidget.ui.designsystem.tokens.AppShape
import io.github.ifa.glancewidget.ui.designsystem.tokens.Spacing
import io.github.ifa.glancewidget.ui.localcomposition.LocalAnimatedVisibilityScope
import io.github.ifa.glancewidget.ui.localcomposition.LocalSharedTransitionScope
import kotlinx.serialization.Serializable
import java.util.Locale

@Keep
@Serializable
data class ChartDetailDestination(
    val metricType: String = METRIC_TEMPERATURE,
) {
    companion object {
        const val METRIC_TEMPERATURE = "temperature"
        const val METRIC_VOLTAGE = "voltage"
        const val STATE_KEY = "chart_detail_destination_key"
    }
}

fun NavGraphBuilder.chartDetailScreen(
    onNavigationIconClick: () -> Unit,
) {
    composable<ChartDetailDestination> {
        CompositionLocalProvider(
            LocalAnimatedVisibilityScope provides this@composable,
        ) {
            ChartDetailScreen(
                animatedVisibilityScope = this@composable,
                onNavigationIconClick = onNavigationIconClick,
            )
        }
    }
}

fun NavController.navigateToChartDetailScreen(destination: ChartDetailDestination) {
    navigate(destination)
}

@Composable
fun ChartDetailScreen(
    viewModel: ChartDetailViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigationIconClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    ChartDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        animatedVisibilityScope = animatedVisibilityScope,
        onNavigationIconClick = onNavigationIconClick,
        onSelectRange = viewModel::onRangeSelected,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun ChartDetailScreen(
    uiState: ChartDetailViewModel.ChartDetailUiState,
    snackbarHostState: SnackbarHostState,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onNavigationIconClick: () -> Unit,
    onSelectRange: (ChartRange) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val isTemperature = uiState.metricType == ChartDetailDestination.METRIC_TEMPERATURE
    val isFahrenheit = uiState.temperatureUnit == MyDevice.Temperature.TemperatureUnit.FAHRENHEIT

    val titleRes = if (isTemperature) R.string.temperature else R.string.voltage
    val accentColor = if (isTemperature) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    val currentValue = if (isTemperature) {
        val tempC = uiState.currentTemperatureCelsius
        if (isFahrenheit) (tempC * 1.8f) + 32f else tempC
    } else {
        uiState.currentVoltage
    }
    val unitText = if (isTemperature) {
        if (isFahrenheit) "°F" else "°C"
    } else {
        "V"
    }
    val locale = LocalConfiguration.current.locales[0]
    val valueFormat = if (isTemperature) "%.1f" else "%.2f"
    val displayValue = String.format(locale, valueFormat, currentValue)

    val rawValues = remember(uiState.chartRecord, isTemperature, isFahrenheit) {
        if (isTemperature) {
            if (isFahrenheit) {
                uiState.chartRecord.temperatures.map { it * 1.8f + 32f }
            } else {
                uiState.chartRecord.temperatures
            }
        } else {
            uiState.chartRecord.voltages
        }
    }
    val timestamps = uiState.chartRecord.timestamps
    val now = remember(rawValues, timestamps) { System.currentTimeMillis() }

    val (filteredValues, filteredTimestamps) = remember(rawValues, timestamps, uiState.selectedRange, now) {
        val range = uiState.selectedRange
        if (timestamps.isEmpty() || timestamps.size != rawValues.size || range == ChartRange.ALL) {
            rawValues to timestamps
        } else {
            val cutoff = now - range.durationMs
            val filtered = rawValues.zip(timestamps).filter { it.second >= cutoff }
            if (filtered.isNotEmpty()) {
                filtered.map { it.first } to filtered.map { it.second }
            } else {
                rawValues to timestamps
            }
        }
    }

    val rangeOptions = listOf(
        stringResource(id = R.string.chart_range_1h),
        stringResource(id = R.string.chart_range_6h),
        stringResource(id = R.string.chart_range_24h),
        stringResource(id = R.string.chart_range_all),
    )
    val selectedRangeIndex = uiState.selectedRange.ordinal

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AnimatedTextTopAppBar(
                title = stringResource(id = titleRes),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigationIconClick,
                        colors = IconButtonDefaults.iconButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = stringResource(id = R.string.back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(
                            key = "${ChartDetailDestination.STATE_KEY}_${uiState.metricType}"
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            } else {
                Modifier
            }

            Box(
                modifier = sharedModifier
                    .fillMaxWidth()
                    .clip(AppShape.card)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(Spacing.l)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Text(
                                text = displayValue,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = unitText,
                                style = MaterialTheme.typography.titleLarge,
                                color = accentColor,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(AppShape.inner)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTemperature) Icons.Rounded.Thermostat else Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                SectionHeader(text = stringResource(id = R.string.chart_history))
                SelectableChips(
                    options = rangeOptions,
                    selected = selectedRangeIndex,
                    onSelect = { onSelectRange(ChartRange.entries[it]) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(AppShape.card)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(Spacing.m),
                contentAlignment = Alignment.Center
            ) {
                if (filteredValues.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_chart_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    VicoDetailLineChart(
                        values = filteredValues,
                        timestamps = filteredTimestamps,
                        accentColor = accentColor,
                        valueFormatter = { String.format(Locale.getDefault(), valueFormat, it) },
                        modifier = Modifier.fillMaxSize(),
                        minSpan = if (isTemperature) 4.0 else 0.4
                    )
                }
            }

            if (filteredValues.isNotEmpty()) {
                val minVal = filteredValues.minOrNull() ?: 0f
                val maxVal = filteredValues.maxOrNull() ?: 0f
                val avgVal = filteredValues.average().toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    val summaryModifier = Modifier
                        .weight(1f)
                        .clip(AppShape.card)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(Spacing.m)

                    Column(
                        modifier = summaryModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Min",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(locale, valueFormat, minVal) + unitText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = summaryModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Avg",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(locale, valueFormat, avgVal) + unitText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = summaryModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Max",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(locale, valueFormat, maxVal) + unitText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun ChartDetailScreenPreview() {
    MaterialTheme {
        ChartDetailScreen(
            uiState = ChartDetailViewModel.ChartDetailUiState(
                metricType = ChartDetailDestination.METRIC_TEMPERATURE,
                chartRecord = ChartRecord(
                    temperatures = listOf(30.0f, 31.5f, 32.0f, 33.5f, 34.0f),
                    voltages = listOf(4.1f, 4.15f, 4.2f),
                    timestamps = listOf(1000L, 2000L, 3000L, 4000L, 5000L)
                ),
                temperatureUnit = MyDevice.Temperature.TemperatureUnit.CELSIUS,
                currentTemperatureCelsius = 34.0f,
                currentVoltage = 4.2f,
                selectedRange = ChartRange.TWENTY_FOUR_HOURS,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onNavigationIconClick = {},
        )
    }
}
