package com.tanim.omniguard.domain.model

import android.graphics.drawable.Drawable

data class RunningApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val importance: String,
    val processCount: Int,
    val memoryUsageBytes: Long
)