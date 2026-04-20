package com.example.omniguard.domain.repository

import com.example.omniguard.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
    fun refreshApps()
}
