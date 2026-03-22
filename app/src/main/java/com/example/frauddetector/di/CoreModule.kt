package com.example.frauddetector.di

import com.example.frauddetector.core.detection.FraudDetectionEngine
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.core.detection.FraudTypeClassifier
import com.example.frauddetector.core.detection.FraudVerifier
import com.example.frauddetector.core.detection.StubFraudTypeClassifier
import com.example.frauddetector.core.detection.StubFraudVerifier
import com.example.frauddetector.core.capture.CollectionRuntimeController
import com.example.frauddetector.core.capture.CollectionServiceController
import com.example.frauddetector.core.detection.impl.FallbackFraudDetector
import com.example.frauddetector.core.inference.LlamaCppInferenceEngine
import com.example.frauddetector.core.inference.LocalInferenceEngine
import com.example.frauddetector.core.recording.DefaultEventRecordingPolicy
import com.example.frauddetector.core.recording.EventRecordingPolicy
import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.core.source.real.RealObservableEventSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreBindingsModule {

    @Binds
    @Singleton
    abstract fun bindFraudTypeClassifier(impl: StubFraudTypeClassifier): FraudTypeClassifier

    @Binds
    @Singleton
    abstract fun bindFraudVerifier(impl: StubFraudVerifier): FraudVerifier

    @Binds
    @Singleton
    abstract fun bindEventRecordingPolicy(impl: DefaultEventRecordingPolicy): EventRecordingPolicy

    @Binds
    @Singleton
    abstract fun bindFraudDetector(impl: FallbackFraudDetector): FraudDetector

    @Binds
    @Singleton
    abstract fun bindLocalInferenceEngine(impl: LlamaCppInferenceEngine): LocalInferenceEngine

    @Binds
    @Singleton
    abstract fun bindCollectionRuntimeController(impl: CollectionServiceController): CollectionRuntimeController
}

@Module
@InstallIn(SingletonComponent::class)
object CoreProvidesModule {

    @Provides
    @Singleton
    fun provideFraudDetectionEngine(
        classifier: FraudTypeClassifier,
        verifier: FraudVerifier
    ): FraudDetectionEngine = FraudDetectionEngine(classifier, verifier)

    @Provides
    @Singleton
    fun provideEventSource(realObservableEventSource: RealObservableEventSource): EventSource =
        realObservableEventSource
}
