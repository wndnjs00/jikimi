package com.myapp.jikimi.data.repository.Disaster

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDisasterRepository(
        disasterRepositoryImpl: DisasterRepositoryImpl
    ): DisasterRepository
}