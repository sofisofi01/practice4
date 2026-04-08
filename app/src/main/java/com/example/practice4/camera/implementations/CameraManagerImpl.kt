package com.example.practice4.camera.implementations

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.practice4.camera.interfaces.ICameraManager
import com.example.practice4.camera.interfaces.ICameraProvider
import com.example.practice4.camera.interfaces.ICameraSession
import com.example.practice4.exercise.interfaces.IExerciseDetector
import com.example.practice4.imagereader.interfaces.IFrameReader
import com.example.practice4.posedetector.interfaces.IPoseDetector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManagerImpl @Inject constructor(
    private val cameraProvider: ICameraProvider,
    private val cameraSession: ICameraSession,
    private val frameReader: IFrameReader,
    private val poseDetector: IPoseDetector,
    override val exerciseDetector: IExerciseDetector
) : ICameraManager {

    companion object {
        private const val TAG = "CameraManagerImpl"
    }

    override fun bind(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraProvider.initialize(lifecycleOwner) { provider ->
            cameraSession.startSession(provider, previewView, lifecycleOwner, ::processFrame)
        }
    }

    private fun processFrame(imageProxy: ImageProxy) {
        val bitmap = frameReader.readFrame(imageProxy) ?: return
        poseDetector.process(bitmap) { landmarks ->
            exerciseDetector.analyze(landmarks)
        }
    }

    override fun stopCamera() {
        val provider = cameraProvider.getProvider() ?: run {
            Log.e(TAG, "stopCamera: provider is null")
            return
        }
        cameraSession.stopSession(provider)
    }
}
