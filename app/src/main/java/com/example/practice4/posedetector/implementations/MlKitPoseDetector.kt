package com.example.practice4.posedetector.implementations

import android.graphics.Bitmap
import android.util.Log
import com.example.practice4.posedetector.interfaces.IPoseDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MlKitPoseDetector @Inject constructor() : IPoseDetector {

    private val detector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private val pushUpProcessor = PushUpProcessor()

    var lastCount = 0
        private set

    override fun process(bitmap: Bitmap, onResult: (Map<Int, Pair<Float, Float>>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()

        detector.process(image)
            .addOnSuccessListener { pose ->
                val landmarks = pose.allPoseLandmarks
                    .filter { it.inFrameLikelihood >= 0.5f }
                    .associate { lm ->
                        lm.landmarkType to (lm.position.x / w to lm.position.y / h)
                    }

                if (landmarks.isNotEmpty()) {
                    lastCount = pushUpProcessor.process(landmarks)
                }

                onResult(landmarks)
            }
            .addOnFailureListener { e ->
                Log.e("MlKitPoseDetector", "Pose detection failed", e)
            }
    }
}
