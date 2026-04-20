package com.tanim.omniguard.domain.usecase

import com.tanim.omniguard.domain.model.PermissionInfo
import com.tanim.omniguard.domain.repository.AppRepository
import com.tanim.omniguard.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHighRiskPermissionsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<PermissionInfo>> {
        return repository.getInstalledApps().map { apps ->
            apps.flatMap { it.permissions }
                .filter { permission ->
                    permission.isGranted && (
                            Constants.CRITICAL_PERMISSIONS.contains(permission.name) ||
                                    Constants.HIGH_RISK_PERMISSIONS.contains(permission.name)
                            )
                }
                .distinctBy { it.name }
        }
    }
}