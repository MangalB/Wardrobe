package com.mangal.nb.wardrobe.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_cloth")
data class FavClothEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val topClothId: Int,
    val bottomClothId: Int
) {
    constructor(topClothId: Int, bottomClothId: Int) : this(null, topClothId, bottomClothId)
}