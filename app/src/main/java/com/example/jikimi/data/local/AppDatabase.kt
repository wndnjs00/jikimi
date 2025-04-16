package com.example.jikimi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.jikimi.data.local.dao.ShelterDao
import com.example.jikimi.data.model.entity.LikeEntity
import com.example.jikimi.data.model.entity.ShelterEntity

@Database(
    entities = [
        LikeEntity::class,
        ShelterEntity::class,
    ],
    version = 2
)


abstract class AppDatabase : RoomDatabase(){
    abstract fun shelterDao() : ShelterDao
}