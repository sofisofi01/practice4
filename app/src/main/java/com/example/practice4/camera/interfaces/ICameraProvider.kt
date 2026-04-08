package com.example.practice4.camera.interfaces

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner

interface ICameraProvider {
    fun getProvider(): ProcessCameraProvider?
    fun initialize(lifecycleOwner: LifecycleOwner, onReady: (ProcessCameraProvider) -> Unit)
}
