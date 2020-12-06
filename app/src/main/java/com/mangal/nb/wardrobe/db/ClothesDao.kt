package com.mangal.nb.wardrobe.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClothesDao {
    @Query("select * from top_cloth")
    fun getAllTopClothes(): LiveData<List<TopClothEntity>>

    @Query("select * from bottom_cloth")
    fun getAllBottomClothes(): LiveData<List<BottomClothEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTopCloth(cloth: TopClothEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBottomCloth(cloth: BottomClothEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavCloth(cloth: FavClothEntity)

    @Query("select count(*) from fav_cloth where topClothId = :topClothId AND bottomClothId = :bottomClothId")
    fun isFavExist(topClothId:Int, bottomClothId:Int): Int

}