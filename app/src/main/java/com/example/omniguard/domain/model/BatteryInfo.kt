package com.example.omniguard.domain.model

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val health: String,
    val temperature: Float,
    val capacity: Int
)
