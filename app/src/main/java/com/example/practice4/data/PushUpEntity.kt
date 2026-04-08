package com.example.practice4.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "push_ups")
data class PushUpEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val count: Int,
    val timestamp: Long
)
