package com.example.homecaretracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.homecaretracker.data.entity.MaintenanceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceHistoryDao {
    @Insert
    suspend fun insert(history: MaintenanceHistory): Long

    @Update
    suspend fun update(history: MaintenanceHistory): Int

    @Query("DELETE FROM maintenance_history WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM maintenance_history ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<MaintenanceHistory>>

    @Query("SELECT * FROM maintenance_history WHERE id = :id")
    suspend fun getById(id: Long): MaintenanceHistory?
}
