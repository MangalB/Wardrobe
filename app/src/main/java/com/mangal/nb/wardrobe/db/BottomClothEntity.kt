package com.mangal.nb.wardrobe.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bottom_cloth")
data class BottomClothEntity(
    @PrimaryKey(autoGenerate = true)
    override val id: Int?,
    override val path: String): BaseClothInterface{
    constructor(path: String) : this(null, path)
}