package com.tanim.omniguard.data.local.dao

import androidx.room.*
import com.tanim.omniguard.data.local.entities.AppInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM apps")
    fun getAllApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): AppInfoEntity?

    @Query("SELECT * FROM apps WHERE isSystemApp = 0")
    fun getUserApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM apps WHERE riskLevel = 'CRITICAL' OR riskLevel = 'HIGH'")
    fun getHighRiskApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM apps WHERE isShadowApp = 1")
    fun getShadowApps(): Flow<List<AppInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppInfoEntity>)

    @Delete
    suspend fun deleteApp(app: AppInfoEntity)

    @Query("DELETE FROM apps")
    suspend fun deleteAllApps()

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getAppCount(): Int

    @Query("SELECT COUNT(*) FROM apps WHERE riskLevel = 'CRITICAL'")
    suspend fun getCriticalAppCount(): Int

    @Query("SELECT COUNT(*) FROM apps WHERE riskLevel = 'HIGH'")
    suspend fun getHighRiskAppCount(): Int
}