package com.myapp.jikimi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.myapp.jikimi.data.local.dao.DisasterDao
import com.myapp.jikimi.data.local.dao.ShelterDao
import com.myapp.jikimi.data.model.entity.DisasterEntity
import com.myapp.jikimi.data.model.entity.LikeEntity
import com.myapp.jikimi.data.model.entity.ShelterEntity

@Database(
    entities = [
        LikeEntity::class,
        ShelterEntity::class,
        DisasterEntity::class,
    ],
    version = 5
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun shelterDao(): ShelterDao
    abstract fun disasterDao(): DisasterDao
}