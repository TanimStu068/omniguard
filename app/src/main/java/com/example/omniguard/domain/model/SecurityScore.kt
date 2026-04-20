package com.example.omniguard.domain.model

data class SecurityScore(
    val score: Int,
    val riskLevel: String,
    val penalties: Map<String, Int>,
    val totalAppsScanned: Int,
    val highRiskAppsCount: Int
)
