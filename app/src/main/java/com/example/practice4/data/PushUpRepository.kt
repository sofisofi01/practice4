package com.example.practice4.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PushUpRepository @Inject constructor(
    private val dao: PushUpDao
) {
    fun getAll(): Flow<List<PushUpEntity>> = dao.getAll()
    
    suspend fun insert(count: Int, timestamp: Long) {
        dao.insert(PushUpEntity(count = count, timestamp = timestamp))
    }
    
    suspend fun update(entity: PushUpEntity) {
        dao.update(entity)
    }
    
    suspend fun delete(entity: PushUpEntity) {
        dao.delete(entity)
    }
}
