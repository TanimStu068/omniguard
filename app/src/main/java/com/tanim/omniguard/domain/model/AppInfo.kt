package com.tanim.omniguard.domain.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val versionName: String,
    val versionCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val permissions: List<PermissionInfo> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val riskScore: Int = 0,
    val category: AppCategory = AppCategory.UNKNOWN,
    val lastUsedTimestamp: Long? = null,
    val appSizeBytes: Long = 0,
    val isShadowApp: Boolean = false,
    val isStopped: Boolean = false,
    val backgroundProcesses: Int = 0
)

enum class AppCategory {
    SOCIAL_MEDIA,
    BANKING,
    COMMUNICATION,
    PRODUCTIVITY,
    ENTERTAINMENT,
    GAMES,
    PHOTOGRAPHY,
    UTILITY,
    MAPS_NAVIGATION,
    TOOLS,
    SYSTEM,
    UNKNOWN
}