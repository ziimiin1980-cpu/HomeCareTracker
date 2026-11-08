package com.example.homecaretracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "maintenance_history",
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
data class MaintenanceHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applianceId: Long,
    @ColumnInfo(defaultValue = "''")
    val applianceName: String = "",
    val taskTitle: String,
    val description: String? = null,
    val completedAt: Long
)
