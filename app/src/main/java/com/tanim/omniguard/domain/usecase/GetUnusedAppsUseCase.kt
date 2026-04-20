package com.tanim.omniguard.domain.usecase

import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUnusedAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<AppInfo>> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return repository.getInstalledApps().map { apps ->
            apps.filter { it.lastUsedTimestamp != null && it.lastUsedTimestamp < thirtyDaysAgo }
        }
    }
}