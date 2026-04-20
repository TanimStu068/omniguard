package com.example.omniguard.domain.repository

import com.example.omniguard.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryRepositoryInterface {
    fun getBatteryInfo(): Flow<BatteryInfo>
}
