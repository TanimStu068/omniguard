package com.example.omniguard.utils

import com.example.omniguard.domain.model.AppCategory

object Constants {
    // Database
    const val DATABASE_NAME = "omniguard_database"
    const val DATABASE_VERSION = 1

    // DataStore
    const val SETTINGS_DATASTORE_NAME = "settings_preferences"

    // WorkManager
    const val HEALTH_CHECK_WORK_NAME = "health_check_work"
    const val HEALTH_CHECK_INTERVAL_HOURS = 24L

    // Security Score
    const val MAX_SECURITY_SCORE = 100
    const val MIN_SECURITY_SCORE = 0

    // Score penalties
    const val PENALTY_ALWAYS_ON_LOCATION = 10
    const val PENALTY_MICROPHONE_ACCESS = 8
    const val PENALTY_CAMERA_ACCESS = 8
    const val PENALTY_SENSITIVE_DATA_ACCESS = 12 // SMS, Contacts, Call Logs
    const val PENALTY_SHADOW_APP = 15
    const val PENALTY_HIGH_BACKGROUND_ACTIVITY = 10
    const val PENALTY_UNUSED_APP = 2
    const val PENALTY_LOW_STORAGE = 5

    // Storage thresholds
    const val LOW_STORAGE_THRESHOLD_PERCENT = 15
    const val BYTES_IN_GB = 1073741824L
    const val BYTES_IN_MB = 1048576L
    const val STORAGE_PATH = "/data"

    // Permission groups
    val CRITICAL_PERMISSIONS = setOf(
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION"
    )

    val HIGH_RISK_PERMISSIONS = setOf(
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG"
    )

    // Trusted Apps (Whitelisted from certain risk penalties)
    val TRUSTED_PACKAGES = setOf(
        "com.whatsapp",
        "com.bKash.customerapp",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.instagram.android",
        "com.google.android.apps.maps",
        "com.google.android.gm",
        "com.google.android.youtube",
        "com.android.vending",
        "com.microsoft.teams",
        "com.skype.raider",
        "com.slack",
        "com.viber.voip",
        "com.tencent.mm",
        "com.google.android.dialer",
        "com.google.android.contacts",
        "com.android.chrome",
        "com.google.android.apps.photos",
        "com.google.android.calendar"
    )

    /**
     * EXPECTED PERMISSIONS BY CATEGORY
     * 
     * If an app only has these, it is considered "SAFE".
     * If it takes permissions NOT in this list, it becomes a "RISK".
     */
    val EXPECTED_PERMISSIONS = mapOf(
        AppCategory.COMMUNICATION to setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_SMS",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_CALL_LOG",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.BLUETOOTH_CONNECT"
        ),
        AppCategory.SOCIAL_MEDIA to setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.BANKING to setOf(
            "android.permission.CAMERA", // For QR scanning
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.READ_PHONE_STATE",
            "android.permission.USE_BIOMETRIC",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.PRODUCTIVITY to setOf(
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.GET_ACCOUNTS",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.PHOTOGRAPHY to setOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO", // For video recording
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.ACCESS_MEDIA_LOCATION"
        ),
        AppCategory.MAPS_NAVIGATION to setOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_BACKGROUND_LOCATION",
            "android.permission.INTERNET",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.GAMES to setOf(
            "android.permission.INTERNET",
            "android.permission.VIBRATE",
            "android.permission.WAKE_LOCK",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.UTILITY to setOf(
            "android.permission.VIBRATE",
            "android.permission.WAKE_LOCK",
            "android.permission.POST_NOTIFICATIONS"
        ),
        AppCategory.TOOLS to setOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE"
        ),
        AppCategory.SYSTEM to setOf(
            "android.permission.INTERNET"
        )
    )

    object Routes {
        const val DASHBOARD = "dashboard"
        const val SENTINEL = "sentinel"
        const val PERFORMANCE = "performance"
        const val APP_DETAIL = "app_detail/{packageName}"
        const val SETTINGS = "settings"
    }
}
