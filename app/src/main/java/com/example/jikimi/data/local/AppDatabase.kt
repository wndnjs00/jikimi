package com.example.jikimi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jikimi.data.local.dao.ShelterDao
import com.example.jikimi.data.model.entity.LikeEntity

@Database(
    entities = [
        LikeEntity::class,
    ],
    version = 1
)


abstract class AppDatabase : RoomDatabase(){
    abstract fun shelterDao() : ShelterDao
}