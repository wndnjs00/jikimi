package com.myapp.jikimi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.myapp.jikimi.data.model.entity.DisasterEntity

@Dao
interface DisasterDao {
    @Query("SELECT * FROM disaster_tips WHERE type = 'today' ORDER BY createdAt DESC")
    suspend fun getTodayDisasters(): List<DisasterEntity>

    @Query("SELECT * FROM disaster_tips WHERE type = 'search' AND title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' LIMIT 1")
    suspend fun searchDisaster(query: String): DisasterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisasters(disasters: List<DisasterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisaster(disaster: DisasterEntity)

    @Query("DELETE FROM disaster_tips WHERE type = 'today'")
    suspend fun clearTodayDisasters()

    @Query("SELECT MAX(createdAt) FROM disaster_tips WHERE type = 'today'")
    suspend fun getLastUpdateTime(): Long?

    @Query("DELETE FROM disaster_tips WHERE createdAt < :timestamp")
    suspend fun deleteOldData(timestamp: Long)
}