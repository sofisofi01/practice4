package com.example.practice4.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice4.view_model.CameraViewModel
import com.example.practice4.data.PushUpEntity
import com.example.practice4.data.PushUpRepository
import com.example.practice4.domain.PushUpDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TrackingMode { SENSOR, CAMERA }

@HiltViewModel
class PushUpViewModel @Inject constructor(
    private val sensorDetector: PushUpDetector,
    private val repository: PushUpRepository
) : ViewModel() {

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _currentCount = MutableStateFlow(0)
    val currentCount = _currentCount.asStateFlow()

    private val _trackingMode = MutableStateFlow(TrackingMode.SENSOR)
    val trackingMode = _trackingMode.asStateFlow()

    private val _replayIntervals = MutableStateFlow<List<Long>?>(null)
    val replayIntervals = _replayIntervals.asStateFlow()

    private var trackingJob: Job? = null
    private var cameraViewModel: CameraViewModel? = null

    val history = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun attachCameraViewModel(vm: CameraViewModel) {
        cameraViewModel = vm
    }

    fun setMode(mode: TrackingMode) {
        if (!_isTracking.value) _trackingMode.value = mode
    }

    fun startTracking() {
        _isTracking.value = true
        _replayIntervals.value = null
        _currentCount.value = 0

        when (_trackingMode.value) {
            TrackingMode.SENSOR -> {
                sensorDetector.reset()
                trackingJob = viewModelScope.launch {
                    sensorDetector.detectPushUps().collect { _currentCount.value = it }
                }
            }
            TrackingMode.CAMERA -> {
                cameraViewModel?.startTracking()
                trackingJob = viewModelScope.launch {
                    cameraViewModel?.count?.collect { _currentCount.value = it }
                }
            }
        }
    }

    fun stopTracking() {
        _isTracking.value = false
        trackingJob?.cancel()
        trackingJob = null

        val timestamps = when (_trackingMode.value) {
            TrackingMode.SENSOR -> sensorDetector.pushUpTimestamps
            TrackingMode.CAMERA -> cameraViewModel?.timestamps ?: emptyList()
        }
        _replayIntervals.value = if (timestamps.size >= 2)
            timestamps.zipWithNext { a, b -> b - a }
        else emptyList()

        if (_trackingMode.value == TrackingMode.CAMERA) {
            cameraViewModel?.stopTracking()
        }

        val count = _currentCount.value
        if (count > 0) viewModelScope.launch {
            repository.insert(count, System.currentTimeMillis())
        }
        _currentCount.value = 0
    }

    fun updateRecord(entity: PushUpEntity) {
        viewModelScope.launch { repository.update(entity) }
    }

    fun deleteRecord(entity: PushUpEntity) {
        viewModelScope.launch { repository.delete(entity) }
    }
}
