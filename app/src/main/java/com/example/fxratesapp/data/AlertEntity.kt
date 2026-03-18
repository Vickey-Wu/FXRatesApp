package com.example.fxratesapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val base: String,
    val target: String,
    val amount: Double,
    val operator: String,
    val threshold: Double,
    val enabled: Boolean = true
)
