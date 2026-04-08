package com.example.practice4.camera.interfaces

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.practice4.exercise.interfaces.IExerciseDetector

interface ICameraManager {
    val exerciseDetector: IExerciseDetector
    fun bind(previewView: PreviewView, lifecycleOwner: LifecycleOwner)
    fun stopCamera()
}
