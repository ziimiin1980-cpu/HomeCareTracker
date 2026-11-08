package com.example.homecaretracker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.entity.MaintenanceTask
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceTaskDao {
    @Insert
    suspend fun insert(task: MaintenanceTask): Long

    @Update
    suspend fun update(task: MaintenanceTask): Int

    @Query("DELETE FROM maintenance_tasks WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM maintenance_tasks ORDER BY nextDueDate")
    fun observeAll(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE id = :id")
    suspend fun getById(id: Long): MaintenanceTask?

    @Transaction
    suspend fun completeTask(
        task: MaintenanceTask,
        history: MaintenanceHistory
    ): Boolean {
        if (update(task) == 0) return false
        insertHistory(history)
        return true
    }

    @Insert
    suspend fun insertHistory(history: MaintenanceHistory): Long
}
