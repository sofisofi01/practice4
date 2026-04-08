package com.example.practice4.exercise.implementations

import android.util.Log
import com.example.practice4.exercise.interfaces.IExerciseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushUpExerciseDetector @Inject constructor() : IExerciseDetector {

    companion object {
        private const val TAG = "PushUpExerciseDetector"
        // Минимальная амплитуда движения плеч (3% от высоты кадра)
        private const val MIN_AMPLITUDE = 0.03f 
        private const val MIN_TIME_BETWEEN_MS = 600L
        private const val HISTORY_SIZE = 100
    }

    private val _count = MutableStateFlow(0)
    override val count: StateFlow<Int> = _count.asStateFlow()

    private val _isDown = MutableStateFlow(false)
    override val isDown: StateFlow<Boolean> = _isDown.asStateFlow()

    private val _timestamps = mutableListOf<Long>()
    override val timestamps: List<Long> get() = _timestamps.toList()

    private val yHistory = mutableListOf<Float>()
    private var lastCountTime = 0L

    override fun analyze(landmarks: Map<Int, Pair<Float, Float>>) {
        if (landmarks.isEmpty()) return

        val leftShoulderY = landmarks[PoseLandmark.LEFT_SHOULDER]?.second
        val rightShoulderY = landmarks[PoseLandmark.RIGHT_SHOULDER]?.second

        if (leftShoulderY == null && rightShoulderY == null) {
            if (System.currentTimeMillis() % 50 == 0L) {
                Log.w(TAG, "Shoulders not found in landmarks: ${landmarks.keys}")
            }
            return
        }

        val currentY = listOfNotNull(leftShoulderY, rightShoulderY).average().toFloat()
        
        yHistory.add(currentY)
        if (yHistory.size > HISTORY_SIZE) {
            yHistory.removeAt(0)
        }

        if (yHistory.size < 10) return

        val minY = yHistory.minOrNull() ?: 0f
        val maxY = yHistory.maxOrNull() ?: 0f
        val range = maxY - minY

        // Относительная позиция: 0.0 - верх (minY), 1.0 - низ (maxY)
        val relativePos = if (range > 0) (currentY - minY) / range else 0.5f
        
        // Логируем каждые 10 кадров, чтобы не спамить, но видеть прогресс
        if (System.currentTimeMillis() % 10 == 0L) {
            Log.d(TAG, "curY=${String.format("%.3f", currentY)} range=${String.format("%.3f", range)} relPos=${String.format("%.2f", relativePos)} isDown=${_isDown.value}")
        }

        // Если амплитуда движения слишком мала, не меняем состояние
        if (range < MIN_AMPLITUDE) return

        val now = System.currentTimeMillis()

        if (!_isDown.value && relativePos > 0.75f) {
            _isDown.value = true
            Log.d(TAG, "!!! DOWN detected (relPos=$relativePos, range=$range)")
        } else if (_isDown.value && relativePos < 0.25f && (now - lastCountTime) >= MIN_TIME_BETWEEN_MS) {
            _isDown.value = false
            _count.value++
            _timestamps.add(now)
            lastCountTime = now
            Log.d(TAG, "!!! UP detected — count=${_count.value} (relPos=$relativePos, range=$range)")
        }
    }

    override fun reset() {
        _count.value = 0
        _isDown.value = false
        lastCountTime = 0L
        yHistory.clear()
        _timestamps.clear()
    }
}
