package com.tanim.omniguard.service.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tanim.omniguard.data.repository.BatteryRepository
import com.tanim.omniguard.data.repository.StorageRepository
import com.tanim.omniguard.domain.model.RiskLevel
import com.tanim.omniguard.domain.repository.AppRepository
import com.tanim.omniguard.domain.usecase.CalculateSecurityScoreUseCase
import com.tanim.omniguard.utils.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appRepository: AppRepository,
    private val storageRepository: StorageRepository,
    private val batteryRepository: BatteryRepository,
    private val calculateSecurityScoreUseCase: CalculateSecurityScoreUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Fetch current state from repositories
            val apps = appRepository.getInstalledApps().first()
            val storage = storageRepository.getStorageAnalysis().first()
            val batteryInfo = batteryRepository.getBatteryInfo().first()
            val score = calculateSecurityScoreUseCase(apps).first()

            // 1. Detailed High Risk App Check
            val highRiskApps = apps.filter {
                (it.riskLevel == RiskLevel.CRITICAL || it.riskLevel == RiskLevel.HIGH) && !it.isSystemApp
            }

            if (highRiskApps.isNotEmpty()) {
                val message = if (highRiskApps.size == 1) {
                    "${highRiskApps.first().appName} has dangerous permissions. Review now."
                } else {
                    "${highRiskApps.size} apps have dangerous permissions. Device at risk."
                }
                NotificationHelper.showSecurityNotification(
                    applicationContext, 101, "Security Alert", message, "sentinel"
                )
            }

            // 2. Storage Alert with specific data
            if (storage.isLowStorage) {
                val used = storage.usedPercentage.toInt()
                NotificationHelper.showSecurityNotification(
                    applicationContext,
                    103,
                    "Storage Warning",
                    "Storage is ${used}% full. Clean up to prevent system lag.",
                    "performance"
                )
            }

            // 3. Low Security Score
            if (score.score < 60) {
                NotificationHelper.showSecurityNotification(
                    applicationContext,
                    102,
                    "Security Integrity Low",
                    "Your security score dropped to ${score.score}. Run a full scan.",
                    "dashboard"
                )
            }

            // 4. Battery Health Check
            if (batteryInfo.health != "Good") {
                NotificationHelper.showSecurityNotification(
                    applicationContext,
                    104,
                    "Battery Health Alert",
                    "Battery status: ${batteryInfo.health}. Check performance details.",
                    "performance"
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}