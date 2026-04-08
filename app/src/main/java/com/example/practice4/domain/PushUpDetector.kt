package com.example.practice4.domain

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.math.sqrt

class PushUpDetector @Inject constructor(
    private val sensorManager: SensorManager
) {
    private var count = 0
    private var isDown = false
    private var lastCountTime = 0L
    private val downThreshold = 12.0f
    private val upThreshold = 9.0f
    private val minTimeBetweenPushUps = 800L
    private val stateFlow = MutableStateFlow(false)
    private val _pushUpTimestamps = mutableListOf<Long>()
    val pushUpTimestamps: List<Long> get() = _pushUpTimestamps.toList()
    
    fun detectPushUps(): Flow<Int> = callbackFlow {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                val magnitude = sqrt(x * x + y * y + z * z)
                val currentTime = System.currentTimeMillis()
                
                if (!isDown && magnitude > downThreshold) {
                    isDown = true
                    stateFlow.value = true
                } else if (isDown && magnitude < upThreshold && 
                          (currentTime - lastCountTime) >= minTimeBetweenPushUps) {
                    isDown = false
                    stateFlow.value = false
                    count++
                    _pushUpTimestamps.add(currentTime)
                    lastCountTime = currentTime
                    trySend(count)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )
        
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
    
    fun reset() {
        count = 0
        isDown = false
        lastCountTime = 0L
        stateFlow.value = false
        _pushUpTimestamps.clear()
    }
    
    fun getCurrentCount() = count
    
    fun getPushUpState() = stateFlow
}
