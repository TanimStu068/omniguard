// app/src/main/java/com/example/omniguard/utils/PermissionHelper.kt
package com.example.omniguard.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.omniguard.domain.model.PermissionInfo

object PermissionHelper {

    // Dangerous permission groups (Android 14)
    private val DANGEROUS_PERMISSIONS = setOf(
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",
        "android.permission.CAMERA",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_PHONE_STATE",
        "android.permission.CALL_PHONE",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ADD_VOICEMAIL",
        "android.permission.USE_SIP",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.BODY_SENSORS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_WAP_PUSH",
        "android.permission.RECEIVE_MMS",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.ACCESS_MEDIA_LOCATION"
    )

    private val PERMISSION_GROUPS = mapOf(
        "android.permission.CAMERA" to "Camera",
        "android.permission.RECORD_AUDIO" to "Microphone",
        "android.permission.ACCESS_FINE_LOCATION" to "Location",
        "android.permission.ACCESS_COARSE_LOCATION" to "Location",
        "android.permission.READ_CONTACTS" to "Contacts",
        "android.permission.WRITE_CONTACTS" to "Contacts",
        "android.permission.READ_SMS" to "SMS",
        "android.permission.SEND_SMS" to "SMS",
        "android.permission.READ_CALENDAR" to "Calendar",
        "android.permission.WRITE_CALENDAR" to "Calendar",
        "android.permission.READ_PHONE_STATE" to "Phone",
        "android.permission.READ_EXTERNAL_STORAGE" to "Storage"
    )

    fun getPermissionGroup(permissionName: String): String {
        return PERMISSION_GROUPS[permissionName] ?: "Other"
    }

    fun isDangerousPermission(permissionName: String): Boolean {
        return DANGEROUS_PERMISSIONS.contains(permissionName)
    }

    fun getPermissionRiskLevel(permissionName: String): String {
        return when {
            permissionName.contains("CAMERA") -> "CRITICAL"
            permissionName.contains("RECORD_AUDIO") -> "CRITICAL"
            permissionName.contains("LOCATION") -> "CRITICAL"
            permissionName.contains("CONTACTS") -> "HIGH"
            permissionName.contains("SMS") -> "HIGH"
            permissionName.contains("CALENDAR") -> "MEDIUM"
            permissionName.contains("PHONE") -> "MEDIUM"
            else -> "LOW"
        }
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun getGrantedPermissions(
        context: Context,
        packageName: String
    ): List<String> {
        val grantedPermissions = mutableListOf<String>()

        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
                )
            }

            packageInfo.requestedPermissions?.forEach { permission ->
                val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Before Marshmallow, permissions are granted at install time
                }

                if (granted) {
                    grantedPermissions.add(permission)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return grantedPermissions
    }
}
