package com.example.frauddetector.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [BehaviorEventEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun behaviorEventDao(): BehaviorEventDao
}
