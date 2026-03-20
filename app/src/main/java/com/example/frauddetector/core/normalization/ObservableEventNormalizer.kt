package com.example.frauddetector.core.normalization

import com.example.frauddetector.core.schema.ObservableSignal
import com.example.frauddetector.domain.model.BehaviorEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservableEventNormalizer @Inject constructor(
    private val appMetadataResolver: AppMetadataResolver
) {

    fun normalize(signal: ObservableSignal): BehaviorEvent {
        val appLabel = appMetadataResolver.resolveLabel(signal.packageName)
        val resolvedAppType = appMetadataResolver.resolveAppType(signal.packageName, appLabel)
        val mergedInformation = buildMap {
            putAll(signal.information)
            signal.packageName?.let { put("package_name", it) }
            signal.previousPackageName?.let { put("previous_package_name", it) }
        }
        return BehaviorEvent(
            timestamp = signal.timestamp,
            action = signal.action.schemaAction,
            app = appLabel ?: signal.packageName,
            appType = resolvedAppType,
            website = signal.website,
            websiteType = signal.websiteType,
            information = mergedInformation,
            online = signal.online && appMetadataResolver.isLikelyOnlineApp(signal.packageName),
            observable = signal.observable,
            source = signal.source,
            packageName = signal.packageName
        )
    }
}
