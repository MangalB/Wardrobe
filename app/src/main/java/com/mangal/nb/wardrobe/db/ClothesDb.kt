package com.mangal.nb.wardrobe.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TopClothEntity::class, BottomClothEntity::class, FavClothEntity::class], version = 1, exportSchema = false)
abstract class ClothesDb : RoomDatabase() {

    abstract fun taskDao(): ClothesDao

    companion object {
        @Volatile
        private var INSTANTCE: ClothesDb? = null

        fun getDatabase(context: Context): ClothesDb {
            val temp = INSTANTCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClothesDb::class.java,
                    "clothes"
                ).build()
                INSTANTCE = instance
                return instance
            }
        }
    }
}