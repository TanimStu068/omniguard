package com.example.omniguard.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.omniguard.domain.model.AppCategory
import com.example.omniguard.domain.model.RiskLevel

@Entity(tableName = "apps")
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val lastScanTime: Long,
    val permissionCount: Int,
    val riskLevel: String,
    val isSystemApp: Boolean,
    val lastUsedTimestamp: Long?,
    val category: String,
    val appSizeBytes: Long,
    val iconHash: String? = null,
    val riskScore: Int = 0,
    val isShadowApp: Boolean = false,
    val backgroundProcesses: Int = 0
) {
    fun toRiskLevel(): RiskLevel = when (riskLevel) {
        "CRITICAL" -> RiskLevel.CRITICAL
        "HIGH" -> RiskLevel.HIGH
        "MEDIUM" -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }

    fun toCategory(): AppCategory = try {
        AppCategory.valueOf(category)
    } catch (e: IllegalArgumentException) {
        AppCategory.UNKNOWN
    }
}