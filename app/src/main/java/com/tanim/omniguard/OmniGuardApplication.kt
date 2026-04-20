package com.tanim.omniguard

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.tanim.omniguard.service.worker.HealthCheckWorker
import com.tanim.omniguard.utils.Constants
import com.tanim.omniguard.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class OmniGuardApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Manual initialization of WorkManager to ensure HiltWorkerFactory is used correctly
        // and to avoid issues with on-demand initialization in some environments.
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

        try {
            WorkManager.initialize(this, config)
        } catch (e: Exception) {
            // Already initialized, nothing to do
        }

        // Create notification channel on app start
        NotificationHelper.createNotificationChannel(this)

        // Schedule periodic health checks
        scheduleHealthCheck()
    }

    private fun scheduleHealthCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<HealthCheckWorker>(
            Constants.HEALTH_CHECK_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.HEALTH_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}