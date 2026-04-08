package com.example.practice4.imagereader.interfaces

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

interface IFrameReader {
    fun readFrame(imageProxy: ImageProxy): Bitmap?
}
