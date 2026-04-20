package com.tanim.omniguard.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tanim.omniguard.data.local.database.dao.AppInfoDao
import com.tanim.omniguard.data.local.database.dao.PermissionDao
import com.tanim.omniguard.data.local.database.dao.ScanHistoryDao
import com.tanim.omniguard.data.local.database.dao.SecurityLogDao
import com.tanim.omniguard.data.local.entities.AppInfoEntity
import com.tanim.omniguard.data.local.entities.PermissionEntity
import com.tanim.omniguard.data.local.entities.ScanHistoryEntity
import com.tanim.omniguard.data.local.entities.SecurityLogEntity

@Database(
    entities = [
        AppInfoEntity::class,
        ScanHistoryEntity::class,
        PermissionEntity::class,
        SecurityLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class OmniGuardDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun permissionDao(): PermissionDao
    abstract fun securityLogDao(): SecurityLogDao

    companion object {
        @Volatile
        private var INSTANCE: OmniGuardDatabase? = null

        fun getDatabase(context: Context): OmniGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniGuardDatabase::class.java,
                    "omniguard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}