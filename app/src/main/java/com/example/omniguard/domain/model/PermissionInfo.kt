package com.example.omniguard.domain.model

data class PermissionInfo(
    val name: String,
    val group: String,
    val isGranted: Boolean,
    val isDangerous: Boolean,
    val protectionLevel: String,
    val description: String = ""
)
