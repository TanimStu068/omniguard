package com.tanim.omniguard.domain.usecase

import com.tanim.omniguard.domain.model.AppInfo
import com.tanim.omniguard.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DetectShadowAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<AppInfo>> {
        return repository.getInstalledApps().map { apps ->
            apps.filter { it.isShadowApp }
        }
    }
}