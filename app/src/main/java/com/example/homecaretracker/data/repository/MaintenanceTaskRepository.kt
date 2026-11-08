package com.example.homecaretracker.data.repository

import com.example.homecaretracker.data.dao.MaintenanceTaskDao
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.entity.MaintenanceTask
import kotlinx.coroutines.flow.Flow

class MaintenanceTaskRepository(
    private val maintenanceTaskDao: MaintenanceTaskDao
) {
    suspend fun add(task: MaintenanceTask): Long = maintenanceTaskDao.insert(task)

    suspend fun update(task: MaintenanceTask): Int = maintenanceTaskDao.update(task)

    suspend fun delete(id: Long): Int = maintenanceTaskDao.deleteById(id)

    fun observeAll(): Flow<List<MaintenanceTask>> = maintenanceTaskDao.observeAll()

    suspend fun getById(id: Long): MaintenanceTask? = maintenanceTaskDao.getById(id)

    suspend fun complete(
        task: MaintenanceTask,
        history: MaintenanceHistory
    ): Boolean = maintenanceTaskDao.completeTask(task, history)
}
