package com.example.practice4.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PushUpEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pushUpDao(): PushUpDao
}
