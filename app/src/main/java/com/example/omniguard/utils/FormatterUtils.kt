package com.example.omniguard.utils

import java.util.Locale

object FormatterUtils {
    /**
     * Formats bytes into a human-readable string (B, KB, MB, GB)
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        return when {
            bytes >= 1_073_741_824 -> String.format(Locale.getDefault(), "%.2f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> String.format(Locale.getDefault(), "%.2f MB", bytes / 1_048_576.0)
            bytes >= 1024 -> String.format(Locale.getDefault(), "%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    /**
     * Formats usage time in milliseconds to a human-readable string
     */
    fun formatUsageTime(millis: Long): String {
        if (millis <= 0L) return "0m"
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
}
