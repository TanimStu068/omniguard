package com.example.omniguard.domain.repository

import com.example.omniguard.domain.model.StorageInfo
import kotlinx.coroutines.flow.Flow

interface StorageRepositoryInterface {
    fun getStorageInfo(): Flow<StorageInfo>
}
