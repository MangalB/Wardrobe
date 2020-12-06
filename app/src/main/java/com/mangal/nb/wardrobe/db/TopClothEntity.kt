package com.mangal.nb.wardrobe.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "top_cloth")
data class TopClothEntity(
    @PrimaryKey(autoGenerate = true)
    override val id: Int?,
    override val path: String): BaseClothInterface {
    constructor(path: String) : this(null, path)
}