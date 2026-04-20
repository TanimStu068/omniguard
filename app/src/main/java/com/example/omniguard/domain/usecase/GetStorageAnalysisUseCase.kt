package com.example.omniguard.domain.usecase

import com.example.omniguard.domain.model.StorageInfo
import com.example.omniguard.domain.repository.StorageRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStorageAnalysisUseCase @Inject constructor(
    private val repository: StorageRepositoryInterface
) {
    operator fun invoke(): Flow<StorageInfo> = repository.getStorageInfo()
}
