package io.github.ifa.glancewidget.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.LocaleManager
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.BatteryManager
import android.os.Build
import android.os.LocaleList
import android.os.Process.myUid
import android.text.TextPaint
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.LocaleListCompat
import androidx.core.util.TypedValueCompat.spToPx
import io.github.ifa.glancewidget.model.AppSettings
import io.github.ifa.glancewidget.model.BonedDevice
import io.github.ifa.glancewidget.model.DeviceType
import io.github.ifa.glancewidget.model.ExtraBatteryInfo
import io.github.ifa.glancewidget.utils.Constants.MAX_DESIGN_CAPACITY
import io.github.ifa.glancewidget.utils.Constants.MIN_DESIGN_CAPACITY
import io.github.ifa.glancewidget.utils.Constants.MIN_PERCENT_FOR_FULL_CAPACITY_ESTIMATE
import androidx.core.graphics.createBitmap
import io.github.ifa.glancewidget.service.BatteryStatusService

@SuppressLint("MissingPermission")
fun Context.getPairedDevices(): List<BonedDevice> {
    val pairedDevices = kotlin.runCatching {
        val btManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter.bondedDevices
    }.getOrDefault(emptyList())

    return pairedDevices.map {
        BonedDevice(
            name = it.name,
            address = it.address,
            batteryInPercentage = it.batteryLevel,
            batteryInMinutes = 0,
            deviceType = DeviceType.fromClass(it.bluetoothClass.deviceClass)
        )
    }.sortedBy { it.deviceType.ordinal }
}

fun Context.safeGetPairedDevices(): List<BonedDevice> {
    return runCatching { getPairedDevices() }.getOrDefault(emptyList())
}

/**
 * Reads the live battery counters from [BatteryManager].
 *
 * @param previousFullChargeCapacity last stored full-charge estimate (mAh) or <= 0 if none. The
 * estimate is derived from an integer battery percentage, so it is only trustworthy at higher
 * charge levels. At low levels the previous value is kept and new estimates are smoothed.
 */
fun Context.getExtraBatteryInformation(previousFullChargeCapacity: Int = -1): ExtraBatteryInfo {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val rawChargeCounter =
        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    // Int.MIN_VALUE (unsupported) and 0 (not ready) are not real readings.
    // The value is in micro amp hours, but some devices already report milli amp hours.
    val chargeCounter = when {
        rawChargeCounter <= 0 -> -1
        rawChargeCounter / 1000 < MAX_DESIGN_CAPACITY / 1000 -> rawChargeCounter
        else -> rawChargeCounter / 1000
    }
    val capacityPercent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    // micro amp to milli amp. Int.MIN_VALUE means unsupported.
    val rawCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    val chargeCurrent = if (rawCurrent == Int.MIN_VALUE) 0 else rawCurrent / 1_000

    // Reflection on a hidden class can fail: that must not take the other readings down with it.
    val designCapacity = runCatching { getDesignCapacity() }.getOrNull()

    val estimate = if (chargeCounter > 0 && capacityPercent in 1..100) {
        chargeCounter * 100f / capacityPercent
    } else {
        -1f
    }
    val isEstimateReliable = capacityPercent >= MIN_PERCENT_FOR_FULL_CAPACITY_ESTIMATE
    val clampTo = designCapacity ?: MAX_DESIGN_CAPACITY
    val fullChargeCapacity = when {
        estimate > 0 && previousFullChargeCapacity > 0 && isEstimateReliable ->
            (previousFullChargeCapacity * 0.9f + estimate * 0.1f).roundToNearest(10, clampTo)

        estimate > 0 && previousFullChargeCapacity <= 0 -> estimate.roundToNearest(10, clampTo)
        previousFullChargeCapacity > 0 -> previousFullChargeCapacity
        else -> -1
    }

    return ExtraBatteryInfo(
        capacity = designCapacity ?: MIN_DESIGN_CAPACITY,
        fullChargeCapacity = fullChargeCapacity,
        chargeCounter = chargeCounter,
        chargeCurrent = chargeCurrent
    )
}

fun Float.roundToNearest(step: Int, max: Int): Int {
    val rounded = ((this + step / 2f) / step).toInt() * step
    return minOf(rounded, max)
}

/** Returns the design capacity in mAh, or null when it can not be read or looks implausible. */
@SuppressLint("PrivateApi")
private fun Context.getDesignCapacity(): Int? {
    val powerProfileClass = "com.android.internal.os.PowerProfile"
    val mPowerProfile = Class.forName(powerProfileClass).getConstructor(
        Context::class.java
    ).newInstance(this)
    val designCapacity = (Class.forName(powerProfileClass).getMethod(
        "getBatteryCapacity"
    ).invoke(mPowerProfile) as Double).toInt()

    return designCapacity.takeIf { it in MIN_DESIGN_CAPACITY..MAX_DESIGN_CAPACITY }
}

fun Context.setLocale(localeCode: String) {
    val code = if (localeCode == AppSettings.Language.DEFAULT.code) {
        ""
    } else {
        localeCode
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        setLocaleForDevicesHigherApi33(code)
    } else {
        setLocaleForDevicesLowerApi33(code)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Context.setLocaleForDevicesHigherApi33(localeCode: String) {
    getSystemService(LocaleManager::class.java).applicationLocales =
        LocaleList.forLanguageTags(localeCode)
}

private fun setLocaleForDevicesLowerApi33(localeTag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
}

fun Context.textAsBitmap(
    text: String,
    fontSize: TextUnit,
    color: Color = Color.Black,
    letterSpacing: Float = 0.1f,
    font: Int
): Bitmap {
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    paint.textSize = spToPx(fontSize.value, resources.displayMetrics)
    paint.color = color.toArgb()
    paint.letterSpacing = letterSpacing
    paint.typeface = ResourcesCompat.getFont(this, font)

    val baseline = -paint.ascent()
    val width = (paint.measureText(text)).toInt()
    val height = (baseline + paint.descent()).toInt()
    val image = createBitmap(width, height)
    val canvas = Canvas(image)
    canvas.drawText(text, 0f, baseline, paint)
    return image
}

fun Context.checkAppUsagePermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), packageName)
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), packageName)
    }
    if (mode == AppOpsManager.MODE_ALLOWED) {
        return true
    }
    val usageStatsManager = getUsageStatsManager() ?: return false
    val now = System.currentTimeMillis()
    val stats = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY,
        now - 1000 * 60 * 60,
        now
    )
    return !stats.isNullOrEmpty()
}

fun Context.getUsageStatsManager(): UsageStatsManager? {
    return getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.getInstalledApps(): List<String> {
    val flags = PackageManager.GET_META_DATA
    val installedApps = packageManager.getInstalledApplications(flags).map { it.packageName }
    val intent = Intent(Intent.ACTION_MAIN, null)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val resolvedApps = packageManager.queryIntentActivities(intent, flags).mapNotNull { it.activityInfo?.packageName }
    return (installedApps + resolvedApps).distinct()
}

fun Context.startBatteryStatus() {
    runCatching {
        val intent = Intent(this, BatteryStatusService::class.java)
        startForegroundService(intent)
    }
}
