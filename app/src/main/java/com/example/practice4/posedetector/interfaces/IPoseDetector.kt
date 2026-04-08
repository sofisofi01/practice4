package com.example.practice4.posedetector.interfaces

import android.graphics.Bitmap

interface IPoseDetector {
    fun process(bitmap: Bitmap, onResult: (landmarks: Map<Int, Pair<Float, Float>>) -> Unit)
}
