package com.tanim.omniguard.domain.usecase

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Debug
import com.tanim.omniguard.domain.model.RunningApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetRunningAppsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityManager: ActivityManager,
    private val packageManager: PackageManager
) {
    operator fun invoke(): Flow<List<RunningApp>> = flow {
        val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
        val runningApps = mutableMapOf<String, RunningAppBuilder>()

        runningProcesses.forEach { processInfo ->
            val packageName = processInfo.pkgList.firstOrNull() ?: return@forEach
            val importance = mapImportance(processInfo.importance)

            val builder = runningApps.getOrPut(packageName) {
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(packageName, 0)
                    ).toString()
                } catch (e: Exception) {
                    packageName
                }
                val icon = try {
                    packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }
                RunningAppBuilder(packageName, appName, icon, importance)
            }

            builder.processCount++
            builder.pids.add(processInfo.pid)
        }

        val result = runningApps.values.map { builder ->
            val memoryInfo = activityManager.getProcessMemoryInfo(builder.pids.toIntArray())
            val totalMemoryBytes = memoryInfo.sumOf { it.totalPss.toLong() * 1024 }

            RunningApp(
                packageName = builder.packageName,
                appName = builder.appName,
                icon = builder.icon,
                importance = builder.importance,
                processCount = builder.processCount,
                memoryUsageBytes = totalMemoryBytes
            )
        }.sortedByDescending { it.memoryUsageBytes }

        emit(result)
    }.flowOn(Dispatchers.IO)

    private fun mapImportance(importance: Int): String {
        return when {
            importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "Foreground"
            importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "Visible"
            importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE -> "Perceptible"
            importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "Service"
            importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "Cached"
            else -> "Background"
        }
    }

    private class RunningAppBuilder(
        val packageName: String,
        val appName: String,
        val icon: Drawable?,
        val importance: String,
        var processCount: Int = 0,
        val pids: MutableList<Int> = mutableListOf()
    )
}