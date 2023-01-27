package com.example.saveplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [SavePlacesEntity::class],version = 1 , exportSchema = false)
abstract class SavePlacesDatabase : RoomDatabase() {
    abstract fun savePlacesDao(): SavePlacesDao

    companion object {
        @Volatile
        private var INSTANCE: SavePlacesDatabase? = null
        fun getInstance(context: Context): SavePlacesDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, SavePlacesDatabase::class.java,
                        "savePlace_database") .fallbackToDestructiveMigration() .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}


