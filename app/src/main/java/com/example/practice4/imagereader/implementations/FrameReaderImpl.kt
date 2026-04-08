package com.example.practice4.imagereader.implementations

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.practice4.imagereader.interfaces.IFrameReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameReaderImpl @Inject constructor() : IFrameReader {

    companion object {
        private const val TAG = "FrameReaderImpl"
    }

    override fun readFrame(imageProxy: ImageProxy): Bitmap? {
        return try {
            imageProxy.toBitmap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert ImageProxy to Bitmap", e)
            null
        }
    }
}
