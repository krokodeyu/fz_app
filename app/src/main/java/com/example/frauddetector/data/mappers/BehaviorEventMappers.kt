package com.example.frauddetector.data.mappers

import com.example.frauddetector.data.db.BehaviorEventEntity
import com.example.frauddetector.domain.model.BehaviorEvent

fun BehaviorEventEntity.toDomain(): BehaviorEvent {
    return BehaviorEvent(
        timestamp = timestamp,
        action = action,
        app = app,
        appType = appType,
        website = website,
        websiteType = websiteType,
        information = information,
        online = online,
        observable = observable,
        source = source,
        packageName = packageName
    )
}

fun BehaviorEvent.toEntity(): BehaviorEventEntity {
    return BehaviorEventEntity(
        timestamp = timestamp,
        action = action,
        app = app,
        appType = appType,
        website = website,
        websiteType = websiteType,
        information = information,
        online = online,
        observable = observable,
        source = source,
        packageName = packageName
    )
}
