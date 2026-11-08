package com.example.homecaretracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appliances",
    indices = [Index(value = ["category"])]
)
data class Appliance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val manufacturer: String? = null,
    val model: String? = null,
    val purchaseDate: Long? = null,
    val notes: String? = null
)
