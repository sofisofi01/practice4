package com.example.practice4.DI

import android.app.Application
import android.hardware.SensorManager
import androidx.room.Room
import com.example.practice4.data.AppDatabase
import com.example.practice4.data.PushUpDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SensorsModule {

    @Provides
    @Singleton
    fun provideSensorManager(app: Application): SensorManager {
        return app.getSystemService(SensorManager::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "pushup_db"
        ).build()
    }
    
    @Provides
    fun providePushUpDao(db: AppDatabase): PushUpDao {
        return db.pushUpDao()
    }
}