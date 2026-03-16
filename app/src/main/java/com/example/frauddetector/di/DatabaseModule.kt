package com.example.frauddetector.di

import android.content.Context
import androidx.room.Room
import com.example.frauddetector.data.db.AppDatabase
import com.example.frauddetector.data.db.BehaviorEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "fraud_detector.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBehaviorEventDao(database: AppDatabase): BehaviorEventDao = database.behaviorEventDao()
}
