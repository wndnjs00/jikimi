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

    @Delete
    suspend fun deleteData(likeEntity: LikeEntity)

    // vtAcmdfcltyNm(대피소명)을 기준으로 일치하는 첫번쨰값만 반환
    @Query("SELECT * FROM LikeEntity WHERE vtAcmdfcltyNm = :shelterName LIMIT 1")
    suspend fun deleteDataFromShelterName(shelterName: String): LikeEntity?

    // 검색 (중복 제거를 위해 DISTINCT 추가)
    @Query("SELECT DISTINCT * FROM ShelterEntity WHERE vtAcmdfcltyNm LIKE '%' || :query || '%'")
    fun searchShelters(query: String): Flow<List<ShelterEntity>>

    // 대피소 검색기능
    // 다수의 대피소정보 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelters(shelters: List<ShelterEntity>)

    // shelterType(대피소유형-"임시주거시설" 또는 "야외대피장소")의 대피소수를 계산
    // 특정유형의 대피소가 얼마나 저장되었는지 확인할때 사용
    @Query("SELECT COUNT(*) FROM ShelterEntity WHERE shelterType = :shelterType")
    suspend fun getShelterCountByType(shelterType: String): Int

    // shelterType(대피소유형-"임시주거시설" 또는 "야외대피장소")의 특정 유형의 대피소만 표시할 때 사용
    @Query("SELECT * FROM ShelterEntity WHERE shelterType = :shelterType")
    fun getSheltersByType(shelterType: String): Flow<List<ShelterEntity>>

    // shelterType(대피소유형-"임시주거시설" 또는 "야외대피장소")의 특정 유형의 대피소를 삭제하는데 사용
    @Query("DELETE FROM ShelterEntity WHERE shelterType = :shelterType")
    suspend fun deleteSheltersByType(shelterType: String)
}