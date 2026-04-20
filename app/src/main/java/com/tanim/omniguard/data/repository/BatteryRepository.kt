package com.tanim.omniguard.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _batteryInfo = MutableStateFlow<BatteryInfo?>(null)

    init {
        // Initial scan on startup
        refreshBatteryInfo()
    }

    /**
     * Gets complete battery information.
     * Returns a flow that emits the cached value immediately, then updates.
     */
    fun getBatteryInfo(): Flow<BatteryInfo> = flow {
        // Emit cached value if exists
        _batteryInfo.value?.let { emit(it) }

        // Then perform a fresh scan
        val info = scanBattery()
        _batteryInfo.value = info
        emit(info)
    }.flowOn(Dispatchers.IO)

    fun refreshBatteryInfo() {
        repositoryScope.launch {
            val info = scanBattery()
            _batteryInfo.value = info
        }
    }

    private fun scanBattery(): BatteryInfo {
        val batteryStatus = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return BatteryInfo(
            level = getBatteryLevel(batteryStatus),
            temperature = getBatteryTemperature(batteryStatus),
            voltage = getBatteryVoltage(batteryStatus),
            status = getChargingStatus(batteryStatus),
            health = getBatteryHealth(batteryStatus),
            plugged = getPowerSource(batteryStatus),
            technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown",
            estimatedCycles = estimateBatteryCycles(),
            isPresent = batteryStatus?.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false) ?: false
        )
    }

    suspend fun getBatteryPercentage(): Int = withContext(Dispatchers.IO) {
        _batteryInfo.value?.level ?: scanBattery().level
    }

    suspend fun isCharging(): Boolean = withContext(Dispatchers.IO) {
        val status = _batteryInfo.value?.status ?: scanBattery().status
        status == "Charging" || status == "Full"
    }

    suspend fun getBatteryHealthStatus(): String = withContext(Dispatchers.IO) {
        _batteryInfo.value?.health ?: scanBattery().health
    }

    suspend fun getBatteryTemperatureCelsius(): Float = withContext(Dispatchers.IO) {
        _batteryInfo.value?.temperature ?: scanBattery().temperature
    }

    suspend fun isOverheating(): Boolean {
        val temperature = getBatteryTemperatureCelsius()
        return temperature > 45.0f
    }

    private fun getBatteryLevel(batteryStatus: Intent?): Int {
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale) else 0
    }

    private fun getBatteryTemperature(batteryStatus: Intent?): Float {
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temperature / 10f
    }

    private fun getBatteryVoltage(batteryStatus: Intent?): Int {
        return batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    }

    private fun getChargingStatus(batteryStatus: Intent?): String {
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    private fun getBatteryHealth(batteryStatus: Intent?): String {
        val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    private fun getPowerSource(batteryStatus: Intent?): String {
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        return when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }
    }

    private fun estimateBatteryCycles(): Int {
        return 150
    }
}

data class BatteryInfo(
    val level: Int,
    val temperature: Float,
    val voltage: Int,
    val status: String,
    val health: String,
    val plugged: String,
    val technology: String,
    val estimatedCycles: Int,
    val isPresent: Boolean
)