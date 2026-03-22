package com.example.frauddetector.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "behavior_events")
data class BehaviorEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val action: String,
    val app: String?,
    val appType: String?,
    val website: String?,
    val websiteType: String?,
    val information: Map<String, String>,
    val online: Boolean,
    val observable: Boolean,
    val source: String,
    val packageName: String?
)
