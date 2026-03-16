package com.example.frauddetector.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BehaviorEventEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun behaviorEventDao(): BehaviorEventDao
}
