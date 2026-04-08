package com.example.practice4.view_model

import androidx.lifecycle.ViewModel
import com.example.practice4.camera.interfaces.ICameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    val cameraManager: ICameraManager
) : ViewModel() {

    val count = cameraManager.exerciseDetector.count
    val isDown = cameraManager.exerciseDetector.isDown
    val timestamps get() = cameraManager.exerciseDetector.timestamps

    fun startTracking() = cameraManager.exerciseDetector.reset()

    fun stopTracking() = cameraManager.stopCamera()

    override fun onCleared() {
        super.onCleared()
        cameraManager.stopCamera()
    }
}
