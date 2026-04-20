package com.example.omniguard.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.omniguard.data.local.entities.PermissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionDao {
    @Query("SELECT * FROM permissions")
    fun getAllPermissions(): Flow<List<PermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermissions(permissions: List<PermissionEntity>)
}
