package com.tanim.omniguard.data.repository

import android.content.Context
import android.content.pm.PackageManager
import com.tanim.omniguard.domain.model.PermissionInfo
import com.tanim.omniguard.utils.PermissionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PERMISSION REPOSITORY
 */
@Singleton
class PermissionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager
) {

    /**
     * Gets all apps with their dangerous permissions
     */
    fun getAppsWithDangerousPermissions(): Flow<List<AppWithPermissions>> = flow {
        val result = mutableListOf<AppWithPermissions>()
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        installedApps.forEach { packageInfo ->
            val grantedPermissions = mutableListOf<PermissionInfo>()

            packageInfo.requestedPermissions?.forEach { permission ->
                val isGranted = context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

                if (isGranted && PermissionHelper.isDangerousPermission(permission)) {
                    grantedPermissions.add(
                        PermissionInfo(
                            name = permission,
                            group = PermissionHelper.getPermissionGroup(permission),
                            isGranted = true,
                            isDangerous = true,
                            protectionLevel = "Dangerous"
                        )
                    )
                }
            }

            if (grantedPermissions.isNotEmpty()) {
                result.add(
                    AppWithPermissions(
                        packageName = packageInfo.packageName,
                        appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString()
                            ?: packageInfo.packageName,
                        permissions = grantedPermissions,
                        riskLevel = calculateRiskLevel(grantedPermissions)
                    )
                )
            }
        }

        emit(result.sortedByDescending { it.riskLevel })
    }.flowOn(Dispatchers.IO)

    /**
     * Gets apps that have camera permission
     */
    fun getAppsWithCameraAccess(): Flow<List<AppWithPermissions>> =
        getAppsWithDangerousPermissions().map { apps ->
            apps.filter { app -> app.permissions.any { it.name.contains("CAMERA") } }
        }

    /**
     * Gets apps that have microphone permission
     */
    fun getAppsWithMicrophoneAccess(): Flow<List<AppWithPermissions>> =
        getAppsWithDangerousPermissions().map { apps ->
            apps.filter { app -> app.permissions.any { it.name.contains("RECORD_AUDIO") } }
        }

    /**
     * Gets apps that have location permission
     */
    fun getAppsWithLocationAccess(): Flow<List<AppWithPermissions>> =
        getAppsWithDangerousPermissions().map { apps ->
            apps.filter { app -> app.permissions.any { it.name.contains("LOCATION") } }
        }

    /**
     * Gets apps that have contacts permission
     */
    fun getAppsWithContactsAccess(): Flow<List<AppWithPermissions>> =
        getAppsWithDangerousPermissions().map { apps ->
            apps.filter { app -> app.permissions.any { it.name.contains("CONTACTS") } }
        }

    /**
     * Gets apps that have SMS permission
     */
    fun getAppsWithSmsAccess(): Flow<List<AppWithPermissions>> =
        getAppsWithDangerousPermissions().map { apps ->
            apps.filter { app -> app.permissions.any { it.name.contains("SMS") } }
        }

    /**
     * Gets statistics about permissions across all apps
     */
    suspend fun getPermissionStats(): PermissionStats {
        val apps = getAppsWithDangerousPermissions().first()

        val cameraCount = apps.count { it.permissions.any { p -> p.name.contains("CAMERA") } }
        val micCount = apps.count { it.permissions.any { p -> p.name.contains("RECORD_AUDIO") } }
        val locationCount = apps.count { it.permissions.any { p -> p.name.contains("LOCATION") } }
        val contactsCount = apps.count { it.permissions.any { p -> p.name.contains("CONTACTS") } }
        val smsCount = apps.count { it.permissions.any { p -> p.name.contains("SMS") } }

        return PermissionStats(
            totalAppsWithPermissions = cameraCount + micCount + locationCount,
            cameraAccessCount = cameraCount,
            microphoneAccessCount = micCount,
            locationAccessCount = locationCount,
            contactsAccessCount = contactsCount,
            smsAccessCount = smsCount
        )
    }

    private fun calculateRiskLevel(permissions: List<PermissionInfo>): String {
        val hasCamera = permissions.any { it.name.contains("CAMERA") }
        val hasMic = permissions.any { it.name.contains("RECORD_AUDIO") }
        val hasLocation = permissions.any { it.name.contains("LOCATION") }

        return when {
            hasCamera && hasMic && hasLocation -> "CRITICAL"
            hasCamera || hasMic || hasLocation -> "HIGH"
            else -> "MEDIUM"
        }
    }
}

data class AppWithPermissions(
    val packageName: String,
    val appName: String,
    val permissions: List<PermissionInfo>,
    val riskLevel: String
)

data class PermissionStats(
    val totalAppsWithPermissions: Int,
    val cameraAccessCount: Int,
    val microphoneAccessCount: Int,
    val locationAccessCount: Int,
    val contactsAccessCount: Int,
    val smsAccessCount: Int
)