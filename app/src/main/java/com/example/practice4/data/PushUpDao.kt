package com.example.practice4.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PushUpDao {
    @Query("SELECT * FROM push_ups ORDER BY timestamp DESC")
    fun getAll(): Flow<List<PushUpEntity>>
    
    @Insert
    suspend fun insert(pushUp: PushUpEntity)
    
    @Update
    suspend fun update(pushUp: PushUpEntity)
    
    @Delete
    suspend fun delete(pushUp: PushUpEntity)
}
