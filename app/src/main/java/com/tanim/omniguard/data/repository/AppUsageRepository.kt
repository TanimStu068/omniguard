package com.tanim.omniguard.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * APP USAGE REPOSITORY
 *
 * WHAT IT DOES:
 * 📱 Tracks which apps are used and when
️️ ⏰ Identifies unused apps (30+ days no usage)
 * 📊 Provides usage statistics for security score calculation
 * 🎯 Supports "Unused App Suggester" feature
 */
@Singleton
class AppUsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val packageManager: PackageManager,
    private val usageStatsManager: UsageStatsManager
) {

    /**
     * Gets all apps with their last usage timestamps
     */
    fun getAllAppsWithUsage(): Flow<List<AppWithUsage>> = flow {
        val usageStats = getUsageStats()
        val installedApps = packageManager.getInstalledPackages(0)
        val result = mutableListOf<AppWithUsage>()

        installedApps.forEach { packageInfo ->
            val packageName = packageInfo.packageName
            val usageStat = usageStats.find { it.packageName == packageName }
            val lastUsedTime = usageStat?.lastTimeUsed ?: 0L
            val totalTimeInForeground = usageStat?.totalTimeInForeground ?: 0L

            result.add(
                AppWithUsage(
                    packageName = packageName,
                    appName = packageInfo.applicationInfo?.loadLabel(packageManager)?.toString()
                        ?: packageName,
                    lastUsedTimestamp = if (lastUsedTime > 0) lastUsedTime else null,
                    totalTimeInForeground = totalTimeInForeground,
                    daysSinceLastUse = calculateDaysSince(lastUsedTime),
                    isUnused = isUnusedApp(lastUsedTime)
                )
            )
        }

        emit(result.sortedByDescending { it.daysSinceLastUse })
    }.flowOn(Dispatchers.IO)

    /**
     * Gets only unused apps (not used in last 30 days)
     */
    fun getUnusedApps(): Flow<List<AppWithUsage>> =
        getAllAppsWithUsage().map { apps ->
            apps.filter { it.isUnused && !isSystemApp(it.packageName) }
        }

    /**
     * Gets recently used apps (last 7 days)
     */
    fun getRecentlyUsedApps(): Flow<List<AppWithUsage>> =
        getAllAppsWithUsage().map { apps ->
            val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
            apps.filter { it.lastUsedTimestamp != null && it.lastUsedTimestamp > sevenDaysAgo }
        }

    /**
     * Gets never used apps
     */
    fun getNeverUsedApps(): Flow<List<AppWithUsage>> =
        getAllAppsWithUsage().map { apps ->
            apps.filter { it.lastUsedTimestamp == null && !isSystemApp(it.packageName) }
        }

    /**
     * Gets usage statistics for the last 30 days
     */
    suspend fun getLast30DaysUsage(): List<UsageStats> = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startTime = calendar.timeInMillis

        usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
    }

    suspend fun getAppUsageTime(packageName: String): Long = withContext(Dispatchers.IO) {
        val usageStats = getLast30DaysUsage()
        usageStats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L
    }

    fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun getUsageStats(): List<UsageStats> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -1)
        val startTime = calendar.timeInMillis

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
    }

    private fun calculateDaysSince(lastUsedTimestamp: Long): Int {
        if (lastUsedTimestamp == 0L) return -1
        val now = System.currentTimeMillis()
        val diffInMillis = now - lastUsedTimestamp
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun isUnusedApp(lastUsedTimestamp: Long): Boolean {
        if (lastUsedTimestamp == 0L) return true
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return lastUsedTimestamp < thirtyDaysAgo
    }

    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val flags = packageInfo.applicationInfo?.flags ?: 0
            (flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

data class AppWithUsage(
    val packageName: String,
    val appName: String,
    val lastUsedTimestamp: Long?,
    val totalTimeInForeground: Long,
    val daysSinceLastUse: Int,
    val isUnused: Boolean
)