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

    var lastCount = 0
        private set

    override fun process(bitmap: Bitmap, onResult: (Map<Int, Pair<Float, Float>>) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()

        detector.process(image)
            .addOnSuccessListener { pose ->
                val allLandmarks = pose.allPoseLandmarks
                
                // Логируем для отладки, если вообще ничего не найдено
                if (allLandmarks.isEmpty()) {
                    if (System.currentTimeMillis() % 100 == 0L) {
                        Log.w("MlKitPoseDetector", "ML Kit returned ZERO landmarks. Bitmap size: ${bitmap.width}x${bitmap.height}")
                    }
                }

                val landmarks = allLandmarks
                    .filter { it.inFrameLikelihood >= 0.1f } // Снижаем до минимума для теста
                    .associate { lm ->
                        lm.landmarkType to (lm.position.x / w to lm.position.y / h)
                    }

                if (landmarks.isNotEmpty()) {
                    Log.d("MlKitPoseDetector", "Detected ${landmarks.size} landmarks (min likelihood 0.1)")
                }
                
                onResult(landmarks)
            }
            .addOnFailureListener { e ->
                Log.e("MlKitPoseDetector", "Pose detection failed", e)
            }
    }
}
