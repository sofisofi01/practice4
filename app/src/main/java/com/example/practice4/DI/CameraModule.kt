package com.example.practice4.DI

import com.example.practice4.camera.implementations.CameraManagerImpl
import com.example.practice4.camera.implementations.CameraProviderImpl
import com.example.practice4.camera.implementations.CameraSessionImpl
import com.example.practice4.camera.interfaces.ICameraManager
import com.example.practice4.camera.interfaces.ICameraProvider
import com.example.practice4.camera.interfaces.ICameraSession
import com.example.practice4.exercise.implementations.PushUpExerciseDetector
import com.example.practice4.exercise.interfaces.IExerciseDetector
import com.example.practice4.imagereader.implementations.FrameReaderImpl
import com.example.practice4.imagereader.interfaces.IFrameReader
import com.example.practice4.posedetector.implementations.MlKitPoseDetector
import com.example.practice4.posedetector.interfaces.IPoseDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {

    @Binds @Singleton
    abstract fun bindCameraManager(impl: CameraManagerImpl): ICameraManager

    @Binds @Singleton
    abstract fun bindCameraProvider(impl: CameraProviderImpl): ICameraProvider

    @Binds @Singleton
    abstract fun bindCameraSession(impl: CameraSessionImpl): ICameraSession

    @Binds @Singleton
    abstract fun bindFrameReader(impl: FrameReaderImpl): IFrameReader

    @Binds @Singleton
    abstract fun bindPoseDetector(impl: MlKitPoseDetector): IPoseDetector

    @Binds @Singleton
    abstract fun bindExerciseDetector(impl: PushUpExerciseDetector): IExerciseDetector
}
