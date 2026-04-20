package com.example.omniguard.domain.usecase

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.omniguard.domain.model.AppInfo
import com.example.omniguard.domain.model.AppCategory
import com.example.omniguard.domain.model.PermissionInfo
import com.example.omniguard.domain.model.RiskLevel
import com.example.omniguard.utils.Constants
import com.example.omniguard.utils.PermissionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAppDetailsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager
) {

    suspend operator fun invoke(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        (PackageManager.GET_PERMISSIONS or
                                PackageManager.GET_SIGNATURES).toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS or
                            PackageManager.GET_SIGNATURES
                )
            }

            val applicationInfo = packageInfo.applicationInfo ?: return@withContext null
            val appName = applicationInfo.loadLabel(packageManager).toString()

            val icon = try {
                applicationInfo.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val permissions = getPermissionsForPackage(packageInfo)
            val category = categorizeApp(packageName, appName)
            val riskLevel = calculateRiskLevel(packageName, permissions, category)
            
            val hasLauncherIcon = packageManager.getLaunchIntentForPackage(packageName) != null
            val isStopped = (applicationInfo.flags and ApplicationInfo.FLAG_STOPPED) != 0
            val isSystem = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isShadowApp = !hasLauncherIcon && !isSystem && !packageName.startsWith("com.android.")

            AppInfo(
                packageName = packageName,
                appName = appName,
                icon = icon,
                isSystemApp = isSystem,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                firstInstallTime = packageInfo.firstInstallTime,
                lastUpdateTime = packageInfo.lastUpdateTime,
                permissions = permissions,
                riskLevel = riskLevel,
                category = category,
                isShadowApp = isShadowApp,
                isStopped = isStopped
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun getPermissionsForPackage(packageInfo: android.content.pm.PackageInfo): List<PermissionInfo> {
        val permissions = mutableListOf<PermissionInfo>()

        packageInfo.requestedPermissions?.forEach { permission ->
            val isGranted = packageManager.checkPermission(
                permission, 
                packageInfo.packageName
            ) == PackageManager.PERMISSION_GRANTED

            permissions.add(
                PermissionInfo(
                    name = permission,
                    group = PermissionHelper.getPermissionGroup(permission),
                    isGranted = isGranted,
                    isDangerous = PermissionHelper.isDangerousPermission(permission),
                    protectionLevel = getProtectionLevel(permission)
                )
            )
        }

        return permissions
    }

    private fun getProtectionLevel(permission: String): String {
        return try {
            val permissionInfo = packageManager.getPermissionInfo(permission, 0)
            when (permissionInfo.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_MASK_BASE) {
                android.content.pm.PermissionInfo.PROTECTION_NORMAL -> "Normal"
                android.content.pm.PermissionInfo.PROTECTION_DANGEROUS -> "Dangerous"
                android.content.pm.PermissionInfo.PROTECTION_SIGNATURE -> "Signature"
                else -> "Unknown"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    private fun calculateRiskLevel(packageName: String, permissions: List<PermissionInfo>, category: AppCategory): RiskLevel {
        val isTrusted = Constants.TRUSTED_PACKAGES.contains(packageName)
        val expectedPerms = Constants.EXPECTED_PERMISSIONS[category] ?: emptySet()
        val unexpectedPerms = permissions.filter { it.isGranted && it.isDangerous && !expectedPerms.contains(it.name) }

        if (unexpectedPerms.isEmpty()) return RiskLevel.LOW

        val criticalCount = unexpectedPerms.count { PermissionHelper.getPermissionRiskLevel(it.name) == "CRITICAL" }
        val highCount = unexpectedPerms.count { PermissionHelper.getPermissionRiskLevel(it.name) == "HIGH" }

        return when {
            criticalCount > 0 && !isTrusted -> RiskLevel.CRITICAL
            highCount >= 2 && !isTrusted -> RiskLevel.HIGH
            criticalCount > 0 || highCount > 0 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    private fun categorizeApp(packageName: String, appName: String): AppCategory {
        val lowerPackage = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPackage.contains("camera") || lowerName.contains("camera") || 
                    lowerPackage.contains("gallery") || lowerPackage.contains("photos") -> AppCategory.PHOTOGRAPHY
            lowerPackage.contains("facebook") || lowerPackage.contains("instagram") ||
                    lowerPackage.contains("twitter") || lowerPackage.contains("tiktok") ||
                    lowerPackage.contains("social") -> AppCategory.SOCIAL_MEDIA
            lowerPackage.contains("bank") || lowerPackage.contains("pay") ||
                    lowerPackage.contains("wallet") || lowerName.contains("bank") || 
                    lowerPackage.contains("bkash") || lowerPackage.contains("nagad") ||
                    lowerPackage.contains("finance") -> AppCategory.BANKING
            lowerPackage.contains("whatsapp") || lowerPackage.contains("telegram") ||
                    lowerPackage.contains("signal") || lowerPackage.contains("messenger") ||
                    lowerPackage.contains("viber") || lowerPackage.contains("chat") ||
                    lowerPackage.contains("contact") -> AppCategory.COMMUNICATION
            lowerPackage.contains("office") || lowerPackage.contains("docs") ||
                    lowerPackage.contains("calendar") || lowerPackage.contains("mail") ||
                    lowerPackage.contains("notes") || lowerPackage.contains("pdf") -> AppCategory.PRODUCTIVITY
            lowerPackage.contains("netflix") || lowerPackage.contains("spotify") ||
                    lowerPackage.contains("youtube") || lowerPackage.contains("prime") ||
                    lowerPackage.contains("video") || lowerPackage.contains("music") ||
                    lowerPackage.contains("player") -> AppCategory.ENTERTAINMENT
            lowerPackage.contains("game") || lowerPackage.contains("play") || 
                    lowerPackage.contains("puzzle") || lowerPackage.contains("arcade") -> AppCategory.GAMES
            lowerPackage.contains("map") || lowerPackage.contains("nav") || 
                    lowerPackage.contains("uber") || lowerPackage.contains("pathao") ||
                    lowerPackage.contains("gps") || lowerPackage.contains("location") -> AppCategory.MAPS_NAVIGATION
            lowerPackage.contains("tool") || lowerPackage.contains("util") ||
                    lowerPackage.contains("cleaner") || lowerPackage.contains("optimizer") ||
                    lowerPackage.contains("security") -> AppCategory.TOOLS
            lowerPackage.contains("android") || lowerPackage.contains("system") ||
                    lowerPackage.contains("google") -> AppCategory.SYSTEM
            else -> AppCategory.UNKNOWN
        }
    }
}
