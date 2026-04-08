package com.example.practice4.exercise.interfaces

import kotlinx.coroutines.flow.StateFlow

interface IExerciseDetector {
    val count: StateFlow<Int>
    val isDown: StateFlow<Boolean>
    val timestamps: List<Long>

    fun analyze(landmarks: Map<Int, Pair<Float, Float>>)
    fun reset()
}
