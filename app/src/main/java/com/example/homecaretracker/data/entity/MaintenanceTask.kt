package com.example.homecaretracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Appliance::class,
            parentColumns = ["id"],
            childColumns = ["applianceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["applianceId"])]
)
data class MaintenanceTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applianceId: Long,
    val title: String,
    val description: String? = null,
    val intervalDays: Int,
    val nextDueDate: Long,
    val isActive: Boolean = true
)
