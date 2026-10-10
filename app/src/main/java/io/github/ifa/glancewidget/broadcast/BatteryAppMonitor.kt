package io.github.ifa.glancewidget.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.github.ifa.glancewidget.domain.AppSettingsRepository
import io.github.ifa.glancewidget.domain.BatteryStateRepository
import io.github.ifa.glancewidget.glance.battery.BatteryWidget
import io.github.ifa.glancewidget.glance.battery.BatteryWidgetReceiver.Companion.BLUETOOTH_STATE_ACTIONS
import io.github.ifa.glancewidget.model.AppExtra
import io.github.ifa.glancewidget.model.AppIntent
import io.github.ifa.glancewidget.model.BatteryMeterNotification
import io.github.ifa.glancewidget.model.MyDevice
import io.github.ifa.glancewidget.model.ThemeType
import io.github.ifa.glancewidget.model.ThemeTypeColor
import io.github.ifa.glancewidget.service.NotificationHandler
import io.github.ifa.glancewidget.utils.getSerializable
import io.github.ifa.glancewidget.utils.goAsyncCoroutine
import io.github.ifa.glancewidget.utils.safeGetPairedDevices
import io.github.ifa.glancewidget.utils.toLocaleDuration
import io.github.ifa.glancewidget.utils.updateBatteryWidget
import io.github.ifa.glancewidget.utils.startBatteryStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@AndroidEntryPoint
class BatteryAppMonitor : BroadcastReceiver() {
    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var batteryStateRepository: BatteryStateRepository

    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        goAsyncCoroutine(monitorScope, Dispatchers.IO) {
            // Events are handled one at a time, in arrival order, so an older broadcast can
            // never overwrite the result of a newer one.
            monitorMutex.withLock { handleIntent(context, intent) }
        }
    }

    private suspend fun handleIntent(context: Context, intent: Intent) {
        run {
            val isBatteryDataAction = intent.action in BATTERY_DATA_ACTIONS
            val shouldUpdateWidget = intent.action !in BOOT_ACTIONS

            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val myDevice = MyDevice.fromIntent(intent)
                    batteryStateRepository.setMyDevice(myDevice)
                    val pairedDevices = context.safeGetPairedDevices()
                    if (pairedDevices.isNotEmpty()) {
                        batteryStateRepository.updateBatteryData {
                            it.copy(batteryConnectedDevices = pairedDevices)
                        }
                    }
                    // Record history here (not only in the UI loop) so it keeps being collected
                    // while the app is in the background.
                    if (myDevice.hasValidReading) {
                        batteryStateRepository.saveHistoryForChart(
                            myDevice.temperature.temperature, myDevice.voltage
                        )
                    }
                    notifyBatteryInfo(context = context, overrideDevice = myDevice)
                }

                Intent.ACTION_POWER_CONNECTED -> {
                    context.startBatteryStatus()
                    batteryStateRepository.updateBatteryData { it.setChargingStatus(true) }
                    notifyBatteryInfo(context = context, overrideCharging = true)
                }

                Intent.ACTION_POWER_DISCONNECTED -> {
                    context.startBatteryStatus()
                    batteryStateRepository.updateBatteryData { it.setChargingStatus(false) }
                    notifyBatteryInfo(context = context, overrideCharging = false)
                }

                Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    context.startBatteryStatus()
                }

                AppIntent.ACTION_SHOW_PAIRED_DEVICES_CHANGED -> {
                    val showPairedDevices =
                        intent.getBooleanExtra(AppExtra.SHOW_PAIRED_DEVICES, true)
                    batteryStateRepository.changePairedDevicesVisibility(showPairedDevices)
                }

                AppIntent.ACTION_SYNC_THEME -> {
                    intent.getSerializable<ThemeType>(AppExtra.SYNC_THEME)?.let {
                        BatteryWidget().updateWidgetSetting(context) { copy(theme = it) }
                    }
                }

                AppIntent.ACTION_SYNC_THEME_COLOR -> {
                    intent.getSerializable<ThemeTypeColor>(AppExtra.SYNC_THEME_COLOR)?.let {
                        BatteryWidget().updateWidgetSetting(context) { copy(themeColor = it) }
                    }
                }

                in BLUETOOTH_STATE_ACTIONS -> {
                    val pairedDevices = context.safeGetPairedDevices()
                    if (pairedDevices.isNotEmpty()) {
                        batteryStateRepository.updateBatteryData {
                            it.copy(batteryConnectedDevices = pairedDevices)
                        }
                    }
                }
            }

            if (isBatteryDataAction) {
                batteryStateRepository.saveExtraBatteryInformation()
            }

            if (shouldUpdateWidget) {
                context.updateBatteryWidget()
            }
        }
    }

    suspend fun notifyBatteryInfo(
        context: Context,
        overrideDevice: MyDevice? = null,
        overrideCharging: Boolean? = null
    ) {
        val appSettings = appSettingsRepository.getAppSettings()
        if (!appSettings.notificationSetting.batteryAlert) return
        val extraBatteryData = batteryStateRepository.extraBattery()
        val batteryData = batteryStateRepository.batteryData()
        val baseDevice = batteryData.myDevice

        val level = overrideDevice?.level?.takeIf { it > 0 } ?: baseDevice.level
        val isCharging = overrideCharging ?: overrideDevice?.isCharging ?: baseDevice.isCharging
        val temperature = overrideDevice?.temperature ?: baseDevice.temperature
        val chargeDisChargeCurrent = batteryStateRepository.chargeCurrent()
        val remainBatteryTime = extraBatteryData.getBatteryTimeRemaining(
            batteryData.myDevice.isCharging && batteryData.myDevice.level < 100,
            chargeDisChargeCurrent
        ).toLocaleDuration(context)
        val remainChargeTime = extraBatteryData.getChargeTimeRemaining(
            batteryData.myDevice.isCharging,
            chargeDisChargeCurrent
        ).toLocaleDuration(context)

        val notification = BatteryMeterNotification(
            batteryLevel = level,
            isCharging = isCharging,
            remainBatteryTime = remainBatteryTime,
            remainChargeTime = remainChargeTime,
            temperature = temperature
        )
        notificationHandler.notifyBatteryMonitorNotification(notification)
    }

    companion object {
        private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val monitorMutex = Mutex()

        private val BATTERY_DATA_ACTIONS = setOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED
        )

        private val BOOT_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED
        )
    }
}