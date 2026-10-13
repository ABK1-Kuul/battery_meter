package io.github.ifa.glancewidget.features.battery.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.ifa.glancewidget.domain.AppSettingsRepository
import io.github.ifa.glancewidget.domain.BatteryStateRepository
import io.github.ifa.glancewidget.domain.BatteryUseCase
import io.github.ifa.glancewidget.model.AppSettings
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.wrapper.BatteryDataWrapper
import io.github.ifa.glancewidget.utils.buildUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class ChartRange(val durationMs: Long) {
    ONE_HOUR(1 * 60 * 60 * 1000L),
    SIX_HOURS(6 * 60 * 60 * 1000L),
    TWENTY_FOUR_HOURS(24 * 60 * 60 * 1000L),
    ALL(Long.MAX_VALUE)
}

@HiltViewModel
class ChartDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    batteryStateRepository: BatteryStateRepository,
    appSettingsRepository: AppSettingsRepository,
    batteryUseCase: BatteryUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ChartDetailDestination>()
    val metricType: String = route.metricType

    private val _selectedRange = MutableStateFlow(ChartRange.TWENTY_FOUR_HOURS)
    val selectedRange: StateFlow<ChartRange> = _selectedRange.asStateFlow()

    private val _chartRecord = batteryStateRepository.chartRecordFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChartRecord()
    )

    private val _appSettings = appSettingsRepository.get().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _batteryWrapper = batteryUseCase.getBatteryWrapper().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryDataWrapper()
    )

    data class ChartDetailUiState(
        val metricType: String = ChartDetailDestination.METRIC_TEMPERATURE,
        val chartRecord: ChartRecord = ChartRecord(),
        val temperatureUnit: MyDevice.Temperature.TemperatureUnit? = null,
        val currentTemperatureCelsius: Float = 0f,
        val currentVoltage: Float = 0f,
        val selectedRange: ChartRange = ChartRange.TWENTY_FOUR_HOURS,
    )

    val uiState: StateFlow<ChartDetailUiState> =
        buildUiState(_chartRecord, _appSettings, _batteryWrapper, _selectedRange) { record, settings, wrapper, range ->
            ChartDetailUiState(
                metricType = metricType,
                chartRecord = record,
                temperatureUnit = settings.temperatureUnit,
                currentTemperatureCelsius = wrapper.batteryData.myDevice.temperature.temperature,
                currentVoltage = wrapper.batteryData.myDevice.voltage,
                selectedRange = range,
            )
        }

    fun onRangeSelected(range: ChartRange) {
        _selectedRange.value = range
    }
}
