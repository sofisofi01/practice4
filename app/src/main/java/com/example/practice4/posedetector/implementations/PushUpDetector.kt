package com.example.practice4.posedetector.implementations
import android.util.Log
import kotlin.math.abs

class PushUpProcessor {
    private val yHistory = mutableListOf<Float>()
    private val historySize = 60

    private var isDown = false
    private var count = 0
    private val minAmplitude = 0.05f

    fun process(landmarks: Map<Int, Pair<Float, Float>>): Int {
        val leftShoulder = landmarks[11]
        val rightShoulder = landmarks[12]

        if (leftShoulder == null || rightShoulder == null) return count

        val currentY = (leftShoulder.second + rightShoulder.second) / 2f

        yHistory.add(currentY)
        if (yHistory.size > historySize) yHistory.removeAt(0)

        if (yHistory.size < 10) return count

        val minY = yHistory.minOrNull() ?: 0f
        val maxY = yHistory.maxOrNull() ?: 0f
        val range = maxY - minY

        if (range < minAmplitude) return count

        val relativePos = (currentY - minY) / range

        if (!isDown && relativePos > 0.85f) {
            isDown = true
        }
        else if (isDown && relativePos < 0.15f) {
            isDown = false
            count++
            Log.d("PushUp", "Repetition detected! Total: $count")
        }

        return count
    }

    fun reset() {
        count = 0
        yHistory.clear()
        isDown = false
    }
}
