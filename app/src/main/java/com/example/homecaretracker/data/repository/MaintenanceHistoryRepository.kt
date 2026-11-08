package com.example.homecaretracker.data.repository

import com.example.homecaretracker.data.dao.MaintenanceHistoryDao
import com.example.homecaretracker.data.entity.MaintenanceHistory
import kotlinx.coroutines.flow.Flow

class MaintenanceHistoryRepository(
    private val maintenanceHistoryDao: MaintenanceHistoryDao
) {
    suspend fun add(history: MaintenanceHistory): Long = maintenanceHistoryDao.insert(history)

    suspend fun update(history: MaintenanceHistory): Int = maintenanceHistoryDao.update(history)

    suspend fun delete(id: Long): Int = maintenanceHistoryDao.deleteById(id)

    fun observeAll(): Flow<List<MaintenanceHistory>> = maintenanceHistoryDao.observeAll()

    suspend fun getById(id: Long): MaintenanceHistory? = maintenanceHistoryDao.getById(id)
}
