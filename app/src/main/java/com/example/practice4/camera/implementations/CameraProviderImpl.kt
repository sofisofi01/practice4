package com.example.practice4.camera.implementations

import android.content.Context
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.practice4.camera.interfaces.ICameraProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ICameraProvider {

    companion object {
        private const val TAG = "CameraProviderImpl"
    }

    private var provider: ProcessCameraProvider? = null

    override fun getProvider() = provider

    override fun initialize(lifecycleOwner: LifecycleOwner, onReady: (ProcessCameraProvider) -> Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                provider = future.get()
                onReady(provider!!)
            } catch (e: ExecutionException) {
                Log.e(TAG, "CameraProvider initialization failed", e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "CameraProvider interrupted", e)
                Thread.currentThread().interrupt()
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
