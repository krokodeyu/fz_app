package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

class RunDetectionUseCase @Inject constructor(
    private val detector: FraudDetector
) {
    suspend operator fun invoke(sequence: BehaviorSequence): DetectionResult {
        return detector.detect(sequence)
    }
}
