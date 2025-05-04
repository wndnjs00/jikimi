package com.myapp.jikimi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.myapp.jikimi.data.model.entity.LikeEntity
import kotlinx.coroutines.flow.Flow
import androidx.room.Query
import com.myapp.jikimi.data.model.entity.ShelterEntity

@Dao
interface ShelterDao {
    @Query("SELECT * FROM LikeEntity")
    fun getAllData() : Flow<List<LikeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(likeEntity: LikeEntity)

    @Update
    suspend fun updataData(likeEntity: LikeEntity)

    @Delete
    suspend fun deleteData(likeEntity: LikeEntity)

    // vtAcmdfcltyNm와 일치하는값 찾아서 1개만 반환
    @Query("SELECT * FROM LikeEntity WHERE vtAcmdfcltyNm = :shelterName LIMIT 1")
    suspend fun deleteDataFromShelterName(shelterName: String): LikeEntity?


    // 대피소 검색기능
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelter(shelterEntity: ShelterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelters(shelters: List<ShelterEntity>)

    @Query("SELECT * FROM ShelterEntity")
    fun getAllShelters(): Flow<List<ShelterEntity>>

    // 검색 (중복 제거를 위해 DISTINCT 추가)
    @Query("SELECT DISTINCT * FROM ShelterEntity WHERE vtAcmdfcltyNm LIKE '%' || :query || '%'")
    fun searchShelters(query: String): Flow<List<ShelterEntity>>

    // 중복된 대피소가 있는지 확인하기위해 (대피소 이름과 위치로 존재 여부 확인)
    @Query("SELECT * FROM ShelterEntity WHERE vtAcmdfcltyNm = :name AND latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun findShelterByNameAndLocation(name: String, latitude: Double, longitude: Double): ShelterEntity?

    // 데이터베이스 초기화 (선택적으로 사용)
    @Query("DELETE FROM ShelterEntity")
    suspend fun clearAllShelters()


    @Query("SELECT COUNT(*) FROM ShelterEntity WHERE shelterType = :shelterType")
    suspend fun getShelterCountByType(shelterType: String): Int

    @Query("SELECT * FROM ShelterEntity WHERE shelterType = :shelterType")
    fun getSheltersByType(shelterType: String): Flow<List<ShelterEntity>>

    @Query("DELETE FROM ShelterEntity WHERE shelterType = :shelterType")
    suspend fun deleteSheltersByType(shelterType: String)
}