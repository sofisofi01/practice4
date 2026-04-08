package com.example.practice4.exercise.implementations

import android.util.Log
import com.example.practice4.exercise.interfaces.IExerciseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PushUpExerciseDetector @Inject constructor() : IExerciseDetector {

    companion object {
        private const val TAG = "PushUpExerciseDetector"
        // порог в нормализованных координатах (0.0-1.0) — 5% высоты кадра
        private const val DROP_THRESHOLD = 0.05f
        private const val MIN_TIME_BETWEEN_MS = 800L
    }

    private val _count = MutableStateFlow(0)
    override val count: StateFlow<Int> = _count.asStateFlow()

    private val _isDown = MutableStateFlow(false)
    override val isDown: StateFlow<Boolean> = _isDown.asStateFlow()

    private val _timestamps = mutableListOf<Long>()
    override val timestamps: List<Long> get() = _timestamps.toList()

    private var isInDownPosition = false
    private var lastCountTime = 0L
    private var baseShoulderY: Float? = null

    override fun analyze(landmarks: Map<Int, Pair<Float, Float>>) {
        val leftShoulderY = landmarks[PoseLandmark.LEFT_SHOULDER]?.second
        val rightShoulderY = landmarks[PoseLandmark.RIGHT_SHOULDER]?.second

        if (leftShoulderY == null && rightShoulderY == null) {
            Log.w(TAG, "No shoulders detected")
            return
        }

        val shoulderY = listOfNotNull(leftShoulderY, rightShoulderY).average().toFloat()

        if (baseShoulderY == null || shoulderY < baseShoulderY!!) {
            baseShoulderY = shoulderY
        }

        val base = baseShoulderY ?: return
        val drop = shoulderY - base

        Log.d(TAG, "shoulderY=$shoulderY base=$base drop=$drop threshold=$DROP_THRESHOLD isDown=$isInDownPosition")

        val now = System.currentTimeMillis()
        if (!isInDownPosition && drop > DROP_THRESHOLD) {
            isInDownPosition = true
            _isDown.value = true
            Log.d(TAG, "DOWN detected")
        } else if (isInDownPosition && drop < DROP_THRESHOLD / 2 &&
            (now - lastCountTime) >= MIN_TIME_BETWEEN_MS
        ) {
            isInDownPosition = false
            _isDown.value = false
            _count.value++
            _timestamps.add(now)
            lastCountTime = now
            baseShoulderY = shoulderY
            Log.d(TAG, "UP detected — count=${_count.value}")
        }
    }

    override fun reset() {
        _count.value = 0
        _isDown.value = false
        isInDownPosition = false
        lastCountTime = 0L
        baseShoulderY = null
        _timestamps.clear()
    }
}
