package com.example.frauddetector.di

import com.example.frauddetector.core.detection.FraudDetectionEngine
import com.example.frauddetector.core.detection.FraudTypeClassifier
import com.example.frauddetector.core.detection.FraudVerifier
import com.example.frauddetector.core.detection.StubFraudTypeClassifier
import com.example.frauddetector.core.detection.StubFraudVerifier
import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.core.source.FakeEventSource
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
    abstract fun bindEventSource(impl: FakeEventSource): EventSource
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
}
