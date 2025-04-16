package com.example.jikimi.data.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    )
        .addMigrations(MIFGRATION_1_2)
        .build()


    @Singleton
    @Provides
    fun provideShelterDao(
        appDatabase: AppDatabase
    ) : ShelterDao = appDatabase.shelterDao()

    private val MIFGRATION_1_2 = object : Migration(1, 2){
        override fun migrate(database: SupportSQLiteDatabase){
            // 테이블이 이미 올바르게 존재하는 경우 구조적 변경이 필요하지 않음
            // 테이블을 수정해야 하는 경우 여기에서 수정할 수 있음
        }
    }
}