package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence

interface FraudDetector {
    suspend fun detect(sequence: BehaviorSequence): DetectionResult
}
