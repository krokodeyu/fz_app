package com.example.frauddetector.di

import com.example.frauddetector.data.repo.BehaviorEventRepositoryImpl
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBehaviorEventRepository(
        impl: BehaviorEventRepositoryImpl
    ): BehaviorEventRepository
}
