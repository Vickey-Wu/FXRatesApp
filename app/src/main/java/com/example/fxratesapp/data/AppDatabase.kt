package com.example.fxratesapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlertEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fx_rates.db"
                ).build().also { instance = it }
            }
        }
    }
}
