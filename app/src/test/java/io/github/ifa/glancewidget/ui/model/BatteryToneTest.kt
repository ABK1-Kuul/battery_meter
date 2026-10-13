package io.github.ifa.glancewidget.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryToneTest {

    @Test
    fun resolveBatteryTone_hotWhenTemperatureAtOrAbove42() {
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 42.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 45.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_hotWhenOverheatFlagIsTrue() {
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 30.0f,
                isOverheat = true
            )
        )
    }

    @Test
    fun resolveBatteryTone_lowWhenLevelAtOrBelow15AndNotCharging() {
        assertEquals(
            BatteryTone.Low,
            resolveBatteryTone(
                level = 15,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Low,
            resolveBatteryTone(
                level = 5,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_fullWhenLevelAtOrAbove100() {
        assertEquals(
            BatteryTone.Full,
            resolveBatteryTone(
                level = 100,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Full,
            resolveBatteryTone(
                level = 105,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_chargingWhenIsChargingTrue() {
        assertEquals(
            BatteryTone.Charging,
            resolveBatteryTone(
                level = 50,
                isCharging = true,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_coldWhenTemperatureAtOrBelow5() {
        assertEquals(
            BatteryTone.Cold,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 5.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Cold,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 0.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_normalOtherwise() {
        assertEquals(
            BatteryTone.Normal,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_priorityConflictHotWinsOverCharging() {
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 50,
                isCharging = true,
                temperatureCelsius = 43.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_priorityConflictHotWinsOverLow() {
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 10,
                isCharging = false,
                temperatureCelsius = 43.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_priorityConflictFullWinsOverCharging() {
        assertEquals(
            BatteryTone.Full,
            resolveBatteryTone(
                level = 100,
                isCharging = true,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_boundary15And16Percent() {
        assertEquals(
            BatteryTone.Low,
            resolveBatteryTone(
                level = 15,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Normal,
            resolveBatteryTone(
                level = 16,
                isCharging = false,
                temperatureCelsius = 25.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_boundary41Dot9And42Celsius() {
        assertEquals(
            BatteryTone.Normal,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 41.9f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Hot,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 42.0f,
                isOverheat = false
            )
        )
    }

    @Test
    fun resolveBatteryTone_boundary5And5Dot1Celsius() {
        assertEquals(
            BatteryTone.Cold,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 5.0f,
                isOverheat = false
            )
        )
        assertEquals(
            BatteryTone.Normal,
            resolveBatteryTone(
                level = 50,
                isCharging = false,
                temperatureCelsius = 5.1f,
                isOverheat = false
            )
        )
    }
}
