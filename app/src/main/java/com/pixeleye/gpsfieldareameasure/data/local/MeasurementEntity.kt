package com.pixeleye.gpsfieldareameasure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val area: Double,
    val perimeter: Double,
    val unit: String,
    val pointsJson: String, // Stored as JSON string using Gson
    val timestamp: Long = System.currentTimeMillis()
)
