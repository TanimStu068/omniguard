package com.tanim.omniguard.domain.repository

import com.tanim.omniguard.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

interface StorageRepositoryInterface {
    fun getStorageInfo(): Flow<StorageInfo>
}