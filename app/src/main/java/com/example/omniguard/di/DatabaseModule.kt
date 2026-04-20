package com.example.omniguard.di

import android.content.Context
import com.example.omniguard.data.local.database.OmniGuardDatabase
import com.example.omniguard.data.local.database.dao.AppInfoDao
import com.example.omniguard.data.local.database.dao.PermissionDao
import com.example.omniguard.data.local.database.dao.ScanHistoryDao
import com.example.omniguard.data.local.database.dao.SecurityLogDao
import com.example.omniguard.data.local.datastore.SettingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OmniGuardDatabase {
        return OmniGuardDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAppInfoDao(database: OmniGuardDatabase): AppInfoDao {
        return database.appInfoDao()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(database: OmniGuardDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }

    @Provides
    @Singleton
    fun providePermissionDao(database: OmniGuardDatabase): PermissionDao {
        return database.permissionDao()
    }

    @Provides
    @Singleton
    fun provideSecurityLogDao(database: OmniGuardDatabase): SecurityLogDao {
        return database.securityLogDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
