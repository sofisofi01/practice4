package com.example.practice4.camera.implementations

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.practice4.camera.interfaces.ICameraSession
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraSessionImpl @Inject constructor() : ICameraSession {

    companion object {
        private const val TAG = "CameraSessionImpl"
    }

    private val analysisExecutor = Executors.newSingleThreadExecutor()

    override fun startSession(
        provider: ProcessCameraProvider,
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        analyzer: ImageAnalysis.Analyzer
    ) {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { it.setAnalyzer(analysisExecutor, analyzer) }

        try {
            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, analysis)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Front camera not available, trying back camera", e)
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (e2: Exception) {
                Log.e(TAG, "No camera available", e2)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "bindToLifecycle failed", e)
        }
    }

    override fun stopSession(provider: ProcessCameraProvider) {
        try {
            provider.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "stopSession failed", e)
        }
    }
}
