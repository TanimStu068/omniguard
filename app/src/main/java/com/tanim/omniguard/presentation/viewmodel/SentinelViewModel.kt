package com.tanim.omniguard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.model.RiskLevel
import com.tanim.omniguard.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SentinelViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SentinelUiState())
    val uiState: StateFlow<SentinelUiState> = _uiState.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    init {
        observeApps()
    }

    private fun observeApps() {
        viewModelScope.launch {
            // Observe the cached app list from the repository
            appRepository.getInstalledApps().collect { apps ->
                if (apps.isNotEmpty()) {
                    allApps = apps
                    updateStats(apps)
                    updateFilteredApps()
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun loadApps() {
        // Manual refresh if the user clicks the refresh button
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Trigger a fresh scan in the background
            appRepository.refreshApps()
        }
    }

    fun filterByPermissionType(permissionType: PermissionFilter?) {
        _uiState.update { it.copy(selectedFilter = permissionType) }
        updateFilteredApps()
    }

    fun searchApps(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredApps()
    }

    fun toggleShowOnlyWithPermissions(showOnly: Boolean) {
        _uiState.update { it.copy(showOnlyWithPermissions = showOnly) }
        updateFilteredApps()
    }

    private fun updateFilteredApps() {
        val state = _uiState.value
        var filtered = allApps

        // 1. Apply permission or risk or special filter
        state.selectedFilter?.let { filter ->
            filtered = when (filter) {
                PermissionFilter.ALL_PERMISSIONS -> filtered
                PermissionFilter.CAMERA -> filtered.filter { hasCameraPermission(it) }
                PermissionFilter.MICROPHONE -> filtered.filter { hasMicrophonePermission(it) }
                PermissionFilter.LOCATION -> filtered.filter { hasLocationPermission(it) }
                PermissionFilter.CONTACTS -> filtered.filter { hasContactsPermission(it) }
                PermissionFilter.SMS -> filtered.filter { hasSmsPermission(it) }
                PermissionFilter.STORAGE -> filtered.filter { hasStoragePermission(it) }
                PermissionFilter.HIGH_RISK -> filtered.filter { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL }
                PermissionFilter.MEDIUM_RISK -> filtered.filter { it.riskLevel == RiskLevel.MEDIUM }
                PermissionFilter.LOW_RISK -> filtered.filter { it.riskLevel == RiskLevel.LOW }
                PermissionFilter.SHADOW_APPS -> filtered.filter { it.isShadowApp }
            }
        }

        // 2. Filter apps with permissions only
        if (state.showOnlyWithPermissions) {
            filtered = filtered.filter { app ->
                app.permissions.any { it.isGranted && it.isDangerous }
            }
        }

        // 3. Apply search query
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.trim()
            filtered = filtered.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
            }
        }

        // 4. Sort
        filtered = filtered.sortedWith(
            compareByDescending<AppInfo> { it.riskLevel.ordinal }
                .thenBy { it.appName }
        )

        _uiState.update { it.copy(filteredApps = filtered) }
    }

    private fun updateStats(apps: List<AppInfo>) {
        val stats = SentinelStats(
            totalApps = apps.size,
            appsWithCamera = apps.count { hasCameraPermission(it) },
            appsWithMicrophone = apps.count { hasMicrophonePermission(it) },
            appsWithLocation = apps.count { hasLocationPermission(it) },
            appsWithContacts = apps.count { hasContactsPermission(it) },
            appsWithSms = apps.count { hasSmsPermission(it) },
            shadowApps = apps.count { it.isShadowApp },
            highRiskApps = apps.count { it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH }
        )
        _uiState.update { it.copy(stats = stats) }
    }

    private fun hasCameraPermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("CAMERA") }
    }

    private fun hasMicrophonePermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("RECORD_AUDIO") }
    }

    private fun hasLocationPermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("LOCATION") }
    }

    private fun hasContactsPermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("CONTACTS") }
    }

    private fun hasSmsPermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("SMS") }
    }

    private fun hasStoragePermission(app: AppInfo): Boolean {
        return app.permissions.any { it.isGranted && it.name.contains("STORAGE") }
    }
}

data class SentinelUiState(
    val isLoading: Boolean = false,
    val filteredApps: List<AppInfo> = emptyList(),
    val stats: SentinelStats = SentinelStats(),
    val selectedFilter: PermissionFilter? = PermissionFilter.ALL_PERMISSIONS,
    val searchQuery: String = "",
    val showOnlyWithPermissions: Boolean = false,
    val selectedAppPackage: String? = null
)

data class SentinelStats(
    val totalApps: Int = 0,
    val appsWithCamera: Int = 0,
    val appsWithMicrophone: Int = 0,
    val appsWithLocation: Int = 0,
    val appsWithContacts: Int = 0,
    val appsWithSms: Int = 0,
    val shadowApps: Int = 0,
    val highRiskApps: Int = 0
)

enum class PermissionFilter(val displayName: String) {
    ALL_PERMISSIONS("All"),
    HIGH_RISK("High Risk"),
    SHADOW_APPS("Shadow Apps"),
    MEDIUM_RISK("Medium Risk"),
    LOW_RISK("Low Risk"),
    CAMERA("Camera"),
    MICROPHONE("Microphone"),
    LOCATION("Location"),
    CONTACTS("Contacts"),
    SMS("SMS"),
    STORAGE("Storage")
}
