package com.myapp.jikimi.data.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.jikimi.data.local.AppDatabase
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.repository.Auth.AuthRepository
import com.myapp.jikimi.data.repository.Auth.AuthRepositoryImpl
import com.myapp.jikimi.data.repository.Comment.CommentRepository
import com.myapp.jikimi.data.repository.Comment.CommentRepositoryImpl
import com.myapp.jikimi.data.repository.Post.PostRepository
import com.myapp.jikimi.data.repository.Post.PostRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.myapp.jikimi.data.local.dao.DisasterDao
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
        .addMigrations(MIFGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,MIGRATION_4_5, MIGRATION_5_6)
        .build()

    @Singleton
    @Provides
    fun provideShelterDao(
        appDatabase: AppDatabase
    ) : ShelterDao = appDatabase.shelterDao()

    @Provides
    fun provideDisasterDao(
        appDatabase: AppDatabase
    ): DisasterDao = appDatabase.disasterDao()


    private val MIFGRATION_1_2 = object : Migration(1, 2){
        override fun migrate(database: SupportSQLiteDatabase){
            // 테이블이 이미 올바르게 존재하는 경우 구조적 변경이 필요하지 않음
            // 테이블을 수정해야 하는 경우 여기에서 수정할 수 있음
        }
    }

    // 2에서 3으로의 마이그레이션 추가
    private val MIGRATION_2_3 = object : Migration(2, 3){
        override fun migrate(database: SupportSQLiteDatabase){
            // 데이터베이스 버전 2에서 3으로 업그레이드하는 데 필요한 스키마 변경사항 구현
            // 실제 변경사항이 무엇인지 알 수 없어 비어있는 구현을 제공합니다.
            // 필요한 테이블 변경, 생성 등의 SQL 쿼리를 여기에 추가해야 합니다.
        }
    }

    // 3에서 4으로의 마이그레이션 추가
    private val MIGRATION_3_4 = object : Migration(3, 4){
        override fun migrate(database: SupportSQLiteDatabase){

        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5){
        override fun migrate(database: SupportSQLiteDatabase){

        }
    }

    // DisasterEntity 테이블 추가를 위한 마이그레이션
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS disaster_tips (
                    id TEXT PRIMARY KEY NOT NULL,
                    title TEXT NOT NULL,
                    subtitle TEXT NOT NULL,
                    category TEXT NOT NULL,
                    riskLevel TEXT NOT NULL,
                    detailedSteps TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    type TEXT NOT NULL
                )
            """)
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance() // FirebaseStorage 추가



    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth,firestore, storage)


    @Provides
    @Singleton
    fun providePostRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PostRepository = PostRepositoryImpl(firebaseAuth, firestore, storage)


    @Provides
    @Singleton
    fun provideCommentRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): CommentRepository = CommentRepositoryImpl(firebaseAuth, firestore)

}