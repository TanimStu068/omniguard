package com.example.omniguard.domain.usecase

import android.os.StatFs
import com.example.omniguard.domain.model.AppInfo
import com.example.omniguard.domain.model.SecurityScore
import com.example.omniguard.domain.model.RiskLevel
import com.example.omniguard.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CalculateSecurityScoreUseCase @Inject constructor() {

    operator fun invoke(
        apps: List<AppInfo>,
        storagePath: String = Constants.STORAGE_PATH
    ): Flow<SecurityScore> = flow {
        var score = Constants.MAX_SECURITY_SCORE
        val penalties = mutableMapOf<String, Int>()

        // 1. Analyze apps for unexpected permissions
        apps.forEach { app ->
            // Skip system apps for penalty calculation to prevent score bottoming out
            if (app.isSystemApp) return@forEach

            val expectedPerms = Constants.EXPECTED_PERMISSIONS[app.category] ?: emptySet()
            
            app.permissions.filter { it.isGranted && it.isDangerous }.forEach { perm ->
                if (!expectedPerms.contains(perm.name)) {
                    // Penalty for UNEXPECTED dangerous permission in non-system apps
                    val penalty = when {
                        perm.name.contains("LOCATION") -> Constants.PENALTY_ALWAYS_ON_LOCATION
                        perm.name.contains("RECORD_AUDIO") -> Constants.PENALTY_MICROPHONE_ACCESS
                        perm.name.contains("CAMERA") -> Constants.PENALTY_CAMERA_ACCESS
                        else -> 2 // Default small penalty for other unexpected dangerous permissions
                    }
                    score -= penalty
                }
            }
        }

        // 4. Shadow Apps (Only non-system)
        val shadowApps = apps.filter { it.isShadowApp && !it.isSystemApp }
        val shadowPenalty = shadowApps.size * Constants.PENALTY_SHADOW_APP
        if (shadowPenalty > 0) {
            score -= shadowPenalty
            penalties["Shadow Apps"] = shadowPenalty
        }

        // 5. High Background Activity (Only non-system)
        val highBackgroundApps = apps.filter { it.backgroundProcesses > 3 && !it.isSystemApp }
        val backgroundPenalty = highBackgroundApps.size * Constants.PENALTY_HIGH_BACKGROUND_ACTIVITY
        if (backgroundPenalty > 0) {
            score -= backgroundPenalty
            penalties["High Background Activity"] = backgroundPenalty
        }

        // 6. Unused Apps (30+ days)
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val unusedApps = apps.filter {
            it.lastUsedTimestamp != null && it.lastUsedTimestamp!! < thirtyDaysAgo && !it.isSystemApp
        }
        val unusedPenalty = unusedApps.size * Constants.PENALTY_UNUSED_APP
        if (unusedPenalty > 0) {
            score -= unusedPenalty
            penalties["Unused Apps"] = unusedPenalty
        }

        // 7. Low Storage
        try {
            val stat = StatFs(storagePath)
            val totalSpace = stat.blockCountLong * stat.blockSizeLong
            val freeSpace = stat.availableBlocksLong * stat.blockSizeLong
            if (totalSpace > 0L) {
                val freePercent = (freeSpace.toFloat() / totalSpace) * 100
                if (freePercent < Constants.LOW_STORAGE_THRESHOLD_PERCENT) {
                    score -= Constants.PENALTY_LOW_STORAGE
                    penalties["Low Storage"] = Constants.PENALTY_LOW_STORAGE
                }
            }
        } catch (e: Exception) {}

        val finalScore = score.coerceIn(Constants.MIN_SECURITY_SCORE, Constants.MAX_SECURITY_SCORE)
        val riskLabel = when {
            finalScore >= 80 -> "Excellent"
            finalScore >= 60 -> "Good"
            finalScore >= 40 -> "Fair"
            else -> "Poor"
        }

        emit(
            SecurityScore(
                score = finalScore,
                riskLevel = riskLabel,
                penalties = penalties,
                totalAppsScanned = apps.size,
                highRiskAppsCount = apps.count { (it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH) && !it.isSystemApp }
            )
        )
    }
}
