package com.tanim.omniguard.domain.repository

import com.tanim.omniguard.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryRepositoryInterface {
    fun getBatteryInfo(): Flow<BatteryInfo>
}