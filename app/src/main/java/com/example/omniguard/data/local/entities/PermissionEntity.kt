package com.example.omniguard.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permissions")
data class PermissionEntity(
    @PrimaryKey
    val permissionName: String,
    val isGranted: Boolean
)
