package com.tanim.omniguard.presentation.viewmodel

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanim.omniguard.data.repository.AppUsageRepository
import com.tanim.omniguard.data.repository.AppWithUsage
import com.tanim.omniguard.data.repository.BatteryInfo as RepoBatteryInfo
import com.tanim.omniguard.data.repository.BatteryRepository
import com.tanim.omniguard.data.repository.StorageAnalysis
import com.tanim.omniguard.data.repository.StorageRepository
import com.tanim.omniguard.domain.model.RunningApp
import com.tanim.omniguard.domain.usecase.GetRunningAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageRepository: StorageRepository,
    private val batteryRepository: BatteryRepository,
    private val appUsageRepository: AppUsageRepository,
    private val getRunningAppsUseCase: GetRunningAppsUseCase,
    private val activityManager: ActivityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private var dataCollectionJob: Job? = null

    init {
        loadPerformanceData()
        startRealTimeMonitoring()
    }

    fun refreshData() {
        storageRepository.refreshStorageStats()
        batteryRepository.refreshBatteryInfo()
        // appUsageRepository refresh if it had one, but it fetches fresh on collect flow

        // Restart the data collection job to ensure fresh collection
        loadPerformanceData()
    }

    private fun loadPerformanceData() {
        dataCollectionJob?.cancel()
        dataCollectionJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                storageRepository.getStorageAnalysis(),
                batteryRepository.getBatteryInfo(),
                appUsageRepository.getUnusedApps(),
                getRunningAppsUseCase()
            ) { storage, battery, unusedApps, runningApps ->
                PerformanceData(storage, battery, unusedApps, runningApps)
            }.collect { data ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        storageInfo = mapStorageInfo(data.storage),
                        batteryInfo = mapBatteryInfo(data.battery),
                        ramInfo = getRamInfo(),
                        unusedApps = mapUnusedApps(data.unusedApps),
                        runningApps = data.runningApps
                    )
                }
            }
        }
    }

    private fun startRealTimeMonitoring() {
        viewModelScope.launch {
            while (true) {
                val updatedRamInfo = getRamInfo()
                // Also update running apps periodically
                getRunningAppsUseCase().take(1).collect { apps ->
                    _uiState.update { it.copy(ramInfo = updatedRamInfo, runningApps = apps) }
                }
                delay(5000) // Update every 5 seconds
            }
        }
    }

    private fun mapStorageInfo(analysis: StorageAnalysis): StorageInfo {
        return StorageInfo(
            totalSpace = analysis.totalSpace,
            freeSpace = analysis.freeSpace,
            usedSpace = analysis.usedSpace,
            usedPercentage = analysis.usedPercentage,
            categories = analysis.categories.map {
                StorageCategory(it.name, it.size, it.color)
            }
        )
    }

    private fun mapBatteryInfo(info: RepoBatteryInfo): BatteryInfo {
        return BatteryInfo(
            percentage = info.level.toFloat(),
            status = info.status,
            health = info.health,
            temperature = info.temperature,
            voltage = info.voltage,
            estimatedCycles = info.estimatedCycles,
            isCharging = info.status == "Charging" || info.status == "Full"
        )
    }

    private fun mapUnusedApps(apps: List<AppWithUsage>): List<UnusedApp> {
        return apps.map { app ->
            UnusedApp(
                packageName = app.packageName,
                appName = app.appName,
                daysUnused = app.daysSinceLastUse,
                storageSize = 0L // Storage size should be fetched separately or added to AppWithUsage
            )
        }
    }

    private fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam
        val usedPercentage = (usedRam.toFloat() / totalRam) * 100

        return RamInfo(
            totalRam = totalRam,
            usedRam = usedRam,
            availableRam = availableRam,
            usedPercentage = usedPercentage,
            isLowMemory = memoryInfo.lowMemory
        )
    }

    private data class PerformanceData(
        val storage: StorageAnalysis,
        val battery: RepoBatteryInfo,
        val unusedApps: List<AppWithUsage>,
        val runningApps: List<RunningApp>
    )
}

data class PerformanceUiState(
    val isLoading: Boolean = false,
    val storageInfo: StorageInfo = StorageInfo(),
    val batteryInfo: BatteryInfo = BatteryInfo(),
    val ramInfo: RamInfo = RamInfo(),
    val unusedApps: List<UnusedApp> = emptyList(),
    val runningApps: List<RunningApp> = emptyList()
)

data class StorageInfo(
    val totalSpace: Long = 0,
    val freeSpace: Long = 0,
    val usedSpace: Long = 0,
    val usedPercentage: Float = 0f,
    val categories: List<StorageCategory> = emptyList()
)

data class StorageCategory(
    val name: String,
    val size: Long,
    val color: Int
)

data class BatteryInfo(
    val percentage: Float = 0f,
    val status: String = "Unknown",
    val health: String = "Unknown",
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val estimatedCycles: Int = 0,
    val isCharging: Boolean = false
)

data class RamInfo(
    val totalRam: Long = 0,
    val usedRam: Long = 0,
    val availableRam: Long = 0,
    val usedPercentage: Float = 0f,
    val isLowMemory: Boolean = false
)

data class UnusedApp(
    val packageName: String,
    val appName: String,
    val daysUnused: Int,
    val storageSize: Long
)