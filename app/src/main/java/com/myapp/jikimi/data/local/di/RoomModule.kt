package com.myapp.jikimi.data.local.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.myapp.jikimi.data.local.AppDatabase
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.repository.AuthRepository
import com.myapp.jikimi.data.repository.AuthRepositoryImpl
import com.myapp.jikimi.data.repository.CommentRepository
import com.myapp.jikimi.data.repository.CommentRepositoryImpl
import com.myapp.jikimi.data.repository.PostRepository
import com.myapp.jikimi.data.repository.PostRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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


    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance() // FirebaseStorage 추가



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