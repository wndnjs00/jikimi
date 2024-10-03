package com.example.jikimi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.example.jikimi.data.model.entity.LikeEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Query

@Dao
interface ShelterDao {
    @Query("SELECT * FROM LikeEntity")
    fun getAllData() : Flow<List<LikeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertData(likeEntity: LikeEntity)

    @Update
    fun updataData(likeEntity: LikeEntity)

    @Delete
    fun deleteData(likeEntity: LikeEntity)

    // vtAcmdfcltyNm와 일치하는값 찾아서 1개만 반환
    @Query("SELECT * FROM LikeEntity WHERE vtAcmdfcltyNm = :shelterName LIMIT 1")
    fun deleteDataFromShelterName(shelterName: String): LikeEntity?
}