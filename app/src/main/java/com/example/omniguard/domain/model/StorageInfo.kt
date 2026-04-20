package com.example.omniguard.domain.model

data class StorageInfo(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val appsSize: Long,
    val cacheSize: Long
)
