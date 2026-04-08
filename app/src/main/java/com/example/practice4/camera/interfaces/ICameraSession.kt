package com.example.practice4.camera.interfaces

import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

interface ICameraSession {
    fun startSession(
        provider: ProcessCameraProvider,
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer
    )
    fun stopSession(provider: ProcessCameraProvider)
}
