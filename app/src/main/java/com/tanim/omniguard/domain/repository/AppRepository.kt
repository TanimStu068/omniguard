package com.tanim.omniguard.domain.repository

import com.tanim.omniguard.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
    fun refreshApps()
}