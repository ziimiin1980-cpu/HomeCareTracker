package com.example.homecaretracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.homecaretracker.data.entity.Appliance
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceDao {
    @Insert
    suspend fun insert(appliance: Appliance): Long

    @Update
    suspend fun update(appliance: Appliance): Int

    @Query("DELETE FROM appliances WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM appliances ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Appliance>>

    @Query("SELECT * FROM appliances WHERE id = :id")
    suspend fun getById(id: Long): Appliance?
}
