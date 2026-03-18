package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetectionEngine
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

class RunDetectionUseCase @Inject constructor(
    private val engine: FraudDetectionEngine
) {
    suspend operator fun invoke(sequence: BehaviorSequence): DetectionResult {
        return engine.detect(sequence)
    }
}
