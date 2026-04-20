package com.tanim.omniguard.service.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class RefreshDataWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return Result.success()
    }
}