package com.example.fxratesapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY id DESC")
    fun getAll(): LiveData<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE enabled = 1")
    suspend fun getEnabled(): List<AlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long

    @Delete
    suspend fun delete(alert: AlertEntity)
}
