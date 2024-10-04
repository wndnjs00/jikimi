package com.example.jikimi.data.local.di

import android.content.Context
import androidx.room.Room
import com.example.jikimi.data.local.AppDatabase
import com.example.jikimi.data.local.dao.ShelterDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app.db"
    ).build()


    @Singleton
    @Provides
    fun provideShelterDao(
        appDatabase: AppDatabase
    ) : ShelterDao = appDatabase.shelterDao()
}