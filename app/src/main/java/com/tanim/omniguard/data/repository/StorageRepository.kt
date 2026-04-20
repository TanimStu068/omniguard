package com.tanim.omniguard.data.repository

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageManager: StorageManager
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _storageAnalysis = MutableStateFlow(
        StorageAnalysis(0, 0, 0, 0f, false, emptyList<StorageCategory>())
    )

    init {
        refreshStorageStats()
    }

    fun getStorageAnalysis(): Flow<StorageAnalysis> {
        return _storageAnalysis.asStateFlow()
    }

    fun refreshStorageStats() {
        repositoryScope.launch {
            val analysis = performScan()
            _storageAnalysis.value = analysis
        }
    }

    private suspend fun performScan(): StorageAnalysis = withContext(Dispatchers.IO) {
        val dataStat = StatFs(Environment.getDataDirectory().path)
        val dataTotal = dataStat.blockCountLong * dataStat.blockSizeLong
        val dataFree = dataStat.availableBlocksLong * dataStat.blockSizeLong
        val dataUsed = dataTotal - dataFree

        val physicalTotal = getPhysicalStorageSize(dataTotal)
        val categoryBreakdown = getStorageCategoryBreakdown(dataUsed, physicalTotal, dataTotal)

        val displayUsedSpace = (physicalTotal - dataFree).coerceAtLeast(dataUsed)
        val displayUsedPercentage = (displayUsedSpace.toFloat() / physicalTotal.coerceAtLeast(1)) * 100

        StorageAnalysis(
            totalSpace = physicalTotal,
            usedSpace = displayUsedSpace,
            freeSpace = dataFree,
            usedPercentage = displayUsedPercentage,
            isLowStorage = (dataFree.toFloat() / dataTotal.coerceAtLeast(1)) < 0.15f,
            categories = categoryBreakdown
        )
    }

    private suspend fun getStorageCategoryBreakdown(
        dataUsedSpace: Long,
        physicalTotal: Long,
        dataTotal: Long
    ): List<StorageCategory> = withContext(Dispatchers.IO) {
        
        val canReadImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val canReadVideo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val canReadAudio = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        var imagesSize = 0L
        var videosSize = 0L
        var audioSize = 0L
        var downloadsSize = 0L

        if (canReadImages) {
            imagesSize = queryCollectionSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        
        if (canReadVideo) {
            videosSize = queryCollectionSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        }
        
        if (canReadAudio) {
            audioSize = queryCollectionSize(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        }

        // Downloads query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadsSize = queryCollectionSize(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
        }
        
        // Fallback for Downloads or if MediaStore collection is empty
        if (downloadsSize == 0L && (canReadImages || canReadVideo || canReadAudio || hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE))) {
            downloadsSize = queryDownloadsByPath()
        }

        var appsSize = 0L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isUsageStatsPermissionGranted()) {
            try {
                val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val appStats = statsManager.queryStatsForUser(StorageManager.UUID_DEFAULT, Process.myUserHandle())
                appsSize = appStats.appBytes + appStats.dataBytes + appStats.cacheBytes
            } catch (e: Exception) {
                Log.e("StorageRepository", "Error querying app stats", e)
            }
        }

        if (appsSize == 0L) appsSize = (dataUsedSpace * 0.35).toLong().coerceAtLeast(1024L * 1024 * 400)

        val categories = mutableListOf<StorageCategory>()
        categories.add(StorageCategory("Apps", appsSize, 0xFF4CAF50.toInt(), "📱"))
        categories.add(StorageCategory("Images & Videos", imagesSize + videosSize, 0xFF2196F3.toInt(), "🖼️"))
        categories.add(StorageCategory("Audio", audioSize, 0xFF00BCD4.toInt(), "🎵"))
        categories.add(StorageCategory("Downloads", downloadsSize, 0xFF9C27B0.toInt(), "⬇️"))

        val accounted = appsSize + imagesSize + videosSize + audioSize + downloadsSize
        val documentsSize = (dataUsedSpace - accounted).coerceAtLeast(1024L * 1024 * 10)
        categories.add(StorageCategory("Documents", documentsSize, 0xFFFF9800.toInt(), "📄"))

        val osReserve = (physicalTotal - dataTotal).coerceAtLeast(0)
        val systemSize = osReserve.coerceAtLeast((physicalTotal * 0.15).toLong())
        categories.add(StorageCategory("System", systemSize, 0xFFF44336.toInt(), "⚙️"))

        return@withContext categories.sortedByDescending { it.size }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun queryCollectionSize(uri: android.net.Uri): Long {
        var total = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                if (sizeIdx != -1) {
                    while (cursor.moveToNext()) {
                        total += cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error querying URI: $uri", e)
        }
        return total
    }

    private fun queryDownloadsByPath(): Long {
        var total = 0L
        try {
            val uri = MediaStore.Files.getContentUri("external")
            val projection = arrayOf(MediaStore.Files.FileColumns.SIZE)
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE 'Download%'"
            } else {
                "${MediaStore.Files.FileColumns.DATA} LIKE '%/Download/%'"
            }
            context.contentResolver.query(uri, projection, selection, null, null)?.use { cursor ->
                val sizeIdx = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                if (sizeIdx != -1) {
                    while (cursor.moveToNext()) {
                        total += cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error querying downloads by path", e)
        }
        return total
    }

    private fun getPhysicalStorageSize(dataTotal: Long): Long {
        val gb = 1024L * 1024 * 1024
        var bytes = 0L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val statsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                bytes = statsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            } catch (e: Exception) {
                Log.w("StorageRepository", "Could not get total bytes from StorageStatsManager")
            }
        }
        if (bytes <= 0) bytes = dataTotal

        val tiers = longArrayOf(16, 32, 64, 128, 256, 512, 1024).map { it * gb }
        for (tier in tiers) {
            if (bytes < tier) {
                if (bytes > tier * 0.75) return tier
            }
        }
        return bytes
    }
}

data class StorageAnalysis(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val usedPercentage: Float,
    val isLowStorage: Boolean,
    val categories: List<StorageCategory>
)

data class StorageCategory(
    val name: String,
    val size: Long,
    val color: Int,
    val icon: String
)