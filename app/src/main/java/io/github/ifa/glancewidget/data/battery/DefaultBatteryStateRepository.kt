package io.github.ifa.glancewidget.data.battery

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.edit
import io.github.ifa.glancewidget.data.batteryWidgetStore
import io.github.ifa.glancewidget.domain.BatteryStateRepository
import io.github.ifa.glancewidget.glance.battery.BatteryWidget
import io.github.ifa.glancewidget.glance.battery.BatteryWidget.Companion.BATTERY_PREFERENCES
import io.github.ifa.glancewidget.model.BatteryData
import io.github.ifa.glancewidget.model.ChargeDisChargeCurrent
import io.github.ifa.glancewidget.model.ChartRecord
import io.github.ifa.glancewidget.model.ExtraBatteryInfo
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.WidgetSetting
import io.github.ifa.glancewidget.utils.Constants.DEFAULT_MAX_COLLECT_CURRENT
import io.github.ifa.glancewidget.utils.getExtraBatteryInformation
import io.github.ifa.glancewidget.utils.fromJson
import io.github.ifa.glancewidget.utils.setBoolean
import io.github.ifa.glancewidget.utils.setObject
import io.github.ifa.glancewidget.utils.toJson
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map

class DefaultBatteryStateRepository(
    private val batteryDataStore: BatteryDataStore, private val context: Context
) : BatteryStateRepository {
    @Composable
    override fun battery(): BatteryData {
        val result by remember {
            batteryDataStore.get()
        }.collectAsState(BatteryData.initial())
        return result
    }

    override fun batteryFlow(): Flow<BatteryData> {
        return batteryDataStore.get()
    }

    override suspend fun batteryData(): BatteryData {
        return batteryDataStore.getBatteryData()
    }

    override fun extraBatteryFlow(): Flow<ExtraBatteryInfo> = flow {
        val default = try {
            context.getExtraBatteryInformation()
        } catch (e: Exception) {
            Log.e(TAG, "extraBatteryFlow: ${e.message}")
            ExtraBatteryInfo()
        }
        // Window lives inside flow{} so every collector gets its own, fresh window.
        val recentCurrents = ArrayDeque<Int>()
        emitAll(
            batteryDataStore.getExtraBatteryInformation(default).conflate().map { info ->
                recentCurrents.addLast(info.chargeCurrent)
                if (recentCurrents.size > CURRENT_SMOOTHING_WINDOW) recentCurrents.removeFirst()
                // Only the noisy instantaneous current is smoothed. Charge counter and
                // capacities are passed through untouched so they match the system value.
                info.copy(chargeCurrent = recentCurrents.average().toInt())
            }
        )
    }

    override suspend fun extraBattery(): ExtraBatteryInfo {
        val extraBattery = try {
            context.getExtraBatteryInformation()
        } catch (e: Exception) {
            Log.e(TAG, "extraBattery: ${e.message}")
            ExtraBatteryInfo()
        }
        return batteryDataStore.getExtraBatteryData(extraBattery)
    }

    override suspend fun chargeCurrent(): ChargeDisChargeCurrent {
        return batteryDataStore.getChargeCurrent()
    }

    override fun chargeCurrentFlow(): Flow<ChargeDisChargeCurrent> {
        return batteryDataStore.getChargeCurrentFlow()
    }

    override fun chartRecordFlow(): Flow<ChartRecord> {
        return batteryDataStore.getChartRecordFlow()
    }

    override suspend fun saveExtraBatteryInformation() {
        val previousFull = batteryDataStore.getExtraBatteryData(ExtraBatteryInfo()).fullChargeCapacity
        val extraBatteryInfo = try {
            context.getExtraBatteryInformation(previousFull)
        } catch (e: Exception) {
            Log.e(TAG, "saveExtraBatteryInformation: ${e.message}")
            ExtraBatteryInfo()
        }
        batteryDataStore.saveExtraBatteryInformation(extraBatteryInfo)
    }

    override suspend fun setBatteryData(batteryData: BatteryData) {
        context.batteryWidgetStore.setObject(BATTERY_PREFERENCES, batteryData)
    }

    /** Read-modify-write in ONE DataStore transaction so concurrent writers can't clobber each other. */
    override suspend fun updateBatteryData(transform: (BatteryData) -> BatteryData) {
        context.batteryWidgetStore.edit { prefs ->
            val current = fromJson<BatteryData>(prefs[BATTERY_PREFERENCES]) ?: BatteryData.initial()
            prefs[BATTERY_PREFERENCES] = toJson(transform(current))
        }
    }

    override suspend fun changePairedDevicesVisibility(showPairedDevices: Boolean) {
        context.batteryWidgetStore.setBoolean(
            BatteryWidget.SHOW_PAIRED_DEVICES, showPairedDevices
        )
    }

    override suspend fun saveWidgetInitialSetting(
        appWidgetId: Int, isTransparent: Boolean, widgetStyle: WidgetSetting.Style
    ) {
        saveWidgetSettings(appWidgetId) {
            it.copy(
                isTransparent = isTransparent,
                style = widgetStyle,
            )
        }
    }

    override suspend fun saveChargeCurrent(chargeCurrent: Int) {
        val chargeDisChargeCurrent = batteryDataStore.getChargeCurrent()
        val chargeSpeed = ChargeDisChargeCurrent.getChargeSpeed(chargeCurrent)
        val newChargeDisChargeCurrent = when (chargeSpeed) {
            ChargeDisChargeCurrent.ChargeSpeed.FAST -> {
                chargeDisChargeCurrent.copy(
                    fastChargeCurrents = chargeDisChargeCurrent.fastChargeCurrents.addUpTo(
                        DEFAULT_MAX_COLLECT_CURRENT, chargeCurrent
                    )
                )
            }

            ChargeDisChargeCurrent.ChargeSpeed.TURBO -> {
                chargeDisChargeCurrent.copy(
                    turboChargeCurrents = chargeDisChargeCurrent.turboChargeCurrents.addUpTo(
                        DEFAULT_MAX_COLLECT_CURRENT, chargeCurrent
                    )
                )
            }

            ChargeDisChargeCurrent.ChargeSpeed.NORMAL -> {
                chargeDisChargeCurrent.copy(
                    chargeCurrents = chargeDisChargeCurrent.chargeCurrents.addUpTo(
                        DEFAULT_MAX_COLLECT_CURRENT, chargeCurrent
                    )
                )
            }

            ChargeDisChargeCurrent.ChargeSpeed.DISCHARGING -> {
                chargeDisChargeCurrent.copy(
                    dischargeCurrents = chargeDisChargeCurrent.dischargeCurrents.addUpTo(
                        DEFAULT_MAX_COLLECT_CURRENT * 4, chargeCurrent
                    )
                )
            }
        }
        batteryDataStore.saveChargeCurrent(newChargeDisChargeCurrent)
    }

    override suspend fun setMyDevice(myDevice: MyDevice) {
        // An empty/placeholder Intent must never overwrite a real reading.
        if (!myDevice.hasValidReading) return
        updateBatteryData { it.copy(myDevice = myDevice) }
    }

    override suspend fun saveHistoryForChart(temperature: Float, voltage: Float) {
        if (voltage <= 0f || temperature <= -50f) return
        context.batteryWidgetStore.edit { prefs ->
            val history = fromJson<ChartRecord>(prefs[BatteryDataStore.CHART_RECORD_PREFERENCES])
                ?: ChartRecord()
            val temperatures = history.temperatures.toMutableList()
            val voltages = history.voltages.toMutableList()
            val timestamps = history.timestamps.toMutableList()
            var changed = false
            if (temperature != temperatures.lastOrNull()) {
                temperatures.add(temperature)
                changed = true
            }
            if (voltage != voltages.lastOrNull()) {
                voltages.add(voltage)
                changed = true
            }
            if (changed) {
                timestamps.add(System.currentTimeMillis())
                prefs[BatteryDataStore.CHART_RECORD_PREFERENCES] = toJson(
                    ChartRecord(
                        temperatures = temperatures.takeLast(MAX_HISTORY_POINTS),
                        voltages = voltages.takeLast(MAX_HISTORY_POINTS),
                        timestamps = timestamps.takeLast(MAX_HISTORY_POINTS),
                    )
                )
            }
        }
    }

    private suspend fun saveWidgetSettings(
        appWidgetId: Int, onAppliedNewSetting: (WidgetSetting) -> WidgetSetting
    ) {
        val widgetSettings = batteryDataStore.getWidgetSettings()
        val savedSetting = widgetSettings.settings[appWidgetId] ?: WidgetSetting()
        val newWidgetSetting = onAppliedNewSetting(savedSetting)
        val newSettings =
            widgetSettings.copy(settings = widgetSettings.settings.toMutableMap().apply {
                this[appWidgetId] = newWidgetSetting
            },)
        batteryDataStore.saveWidgetSettings(newSettings)
    }
    companion object {
        const val TAG = "DefaultBatteryStateRepository"
        private const val CURRENT_SMOOTHING_WINDOW = 3
        private const val MAX_HISTORY_POINTS = 100
    }
}

fun List<Int>.addUpTo(limit: Int, value: Int): List<Int> {
    return this.toPersistentList().add(value).takeLast(limit)
}