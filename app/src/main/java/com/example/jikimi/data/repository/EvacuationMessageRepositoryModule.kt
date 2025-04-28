package com.example.jikimi.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EvacuationMessageRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEvacuationMessageRepository(evacuationMessageRepositoryImpl: EvacuationMessageRepositoryImpl): EvacuationMessageRepository
}