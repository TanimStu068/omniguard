package com.tanim.omniguard.data.repository

import com.tanim.omniguard.data.local.database.dao.AppInfoDao
import com.tanim.omniguard.data.local.entities.AppInfoEntity
import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.repository.AppRepository
import com.tanim.omniguard.domain.usecase.GetInstalledAppsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val appInfoDao: AppInfoDao
) : AppRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Using a Flow from the database for instant data
    override fun getInstalledApps(): Flow<List<AppInfo>> {
        return appInfoDao.getAllApps().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    init {
        // Automatically start a background refresh when the app starts
        refreshApps()
    }

    override fun refreshApps() {
        repositoryScope.launch {
            getInstalledAppsUseCase().collect { appList ->
                // Update the database cache with new scan results
                val entities = appList.map { it.toEntity() }
                appInfoDao.insertApps(entities)
            }
        }
    }

    // Helper extension to convert domain model to entity
    private fun AppInfo.toEntity(): AppInfoEntity {
        return AppInfoEntity(
            packageName = this.packageName,
            appName = this.appName,
            lastScanTime = System.currentTimeMillis(),
            permissionCount = this.permissions.size,
            riskLevel = this.riskLevel.name,
            isSystemApp = this.isSystemApp,
            lastUsedTimestamp = this.lastUsedTimestamp,
            category = this.category.name,
            appSizeBytes = this.appSizeBytes,
            riskScore = this.riskScore,
            isShadowApp = this.isShadowApp,
            backgroundProcesses = this.backgroundProcesses
        )
    }

    // Helper extension to convert entity to domain model
    private fun AppInfoEntity.toDomainModel(): AppInfo {
        return AppInfo(
            packageName = this.packageName,
            appName = this.appName,
            icon = null, // Note: Icons are heavy and shouldn't be stored in DB. We load them on demand.
            isSystemApp = this.isSystemApp,
            versionName = "",
            versionCode = 0,
            firstInstallTime = 0,
            lastUpdateTime = 0,
            riskLevel = this.toRiskLevel(),
            category = this.toCategory(),
            isShadowApp = this.isShadowApp,
            backgroundProcesses = this.backgroundProcesses,
            lastUsedTimestamp = this.lastUsedTimestamp
        )
    }
}