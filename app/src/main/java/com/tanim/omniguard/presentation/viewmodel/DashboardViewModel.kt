package com.tanim.omniguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanim.omniguard.data.repository.AppUsageRepository
import com.tanim.omniguard.data.repository.BatteryRepository
import com.tanim.omniguard.data.repository.StorageRepository
import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.model.SecurityScore
import com.tanim.omniguard.domain.model.RiskLevel
import com.tanim.omniguard.domain.repository.AppRepository
import com.tanim.omniguard.domain.usecase.CalculateSecurityScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val calculateSecurityScoreUseCase: CalculateSecurityScoreUseCase,
    private val storageRepository: StorageRepository,
    private val batteryRepository: BatteryRepository,
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            appRepository.refreshApps()
            // We don't need to call loadDashboardData() here because
            // the Flow in loadDashboardData() will automatically emit
            // new values when the database is updated.
            _isRefreshing.value = false
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            combine(
                appRepository.getInstalledApps(),
                storageRepository.getStorageAnalysis(),
                batteryRepository.getBatteryInfo()
            ) { apps, storage, battery ->
                DataPack(apps, storage, battery)
            }
                .flatMapLatest { data ->
                    calculateSecurityScoreUseCase(data.apps).map { score ->
                        data to score
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load data: ${e.message}") }
                }
                .collect { (data, score) ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = data.apps.isEmpty(), // Only show loading if we have NO apps yet
                            apps = data.apps,
                            securityScore = score,
                            totalApps = data.apps.size,
                            highRiskApps = data.apps.count { (it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH) && !it.isSystemApp },
                            criticalPermissionsCount = getCriticalPermissionsCount(data.apps),
                            recentAlerts = generateAlerts(data.apps, score, data.storage.isLowStorage, data.battery.health != "Good"),
                            batteryPercentage = data.battery.level,
                            storageUsedPercentage = data.storage.usedPercentage
                        )
                    }
                }
        }
    }

    private data class DataPack(
        val apps: List<AppInfo>,
        val storage: com.tanim.omniguard.data.repository.StorageAnalysis,
        val battery: com.tanim.omniguard.data.repository.BatteryInfo
    )

    private fun getCriticalPermissionsCount(apps: List<AppInfo>): Int {
        var total = 0
        for (app in apps) {
            total += app.permissions.count { it.isGranted && it.isDangerous }
        }
        return total
    }

    private fun generateAlerts(
        apps: List<AppInfo>,
        score: SecurityScore,
        isLowStorage: Boolean,
        isBatteryIssue: Boolean
    ): List<Alert> {
        val alerts = mutableListOf<Alert>()

        if (score.score < 60) {
            alerts.add(Alert(AlertType.CRITICAL, "Security Risk", "Your security score is low.", "Fix Now"))
        }

        if (isLowStorage) {
            alerts.add(Alert(AlertType.WARNING, "Storage Low", "Less than 15% storage remaining.", "Clean Up"))
        }

        if (isBatteryIssue) {
            alerts.add(Alert(AlertType.WARNING, "Battery Health", "Battery health issues detected.", "Check Details"))
        }

        val highRiskAppsCount = apps.count { (it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH) && !it.isSystemApp }
        if (highRiskAppsCount > 0) {
            alerts.add(Alert(AlertType.CRITICAL, "$highRiskAppsCount High Risk Apps", "Review dangerous permissions.", "Review"))
        }

        return alerts
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val apps: List<AppInfo> = emptyList(),
    val securityScore: SecurityScore? = null,
    val totalApps: Int = 0,
    val highRiskApps: Int = 0,
    val criticalPermissionsCount: Int = 0,
    val recentAlerts: List<Alert> = emptyList(),
    val batteryPercentage: Int = 0,
    val storageUsedPercentage: Float = 0f
)

data class Alert(
    val type: AlertType,
    val title: String,
    val message: String,
    val action: String
)

enum class AlertType {
    CRITICAL, WARNING, INFO
}