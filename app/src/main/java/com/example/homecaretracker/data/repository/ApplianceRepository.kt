package com.example.homecaretracker.data.repository

import com.example.homecaretracker.data.dao.ApplianceDao
import com.example.homecaretracker.data.entity.Appliance
import kotlinx.coroutines.flow.Flow

class ApplianceRepository(
    private val applianceDao: ApplianceDao
) {
    suspend fun add(appliance: Appliance): Long = applianceDao.insert(appliance)

    suspend fun update(appliance: Appliance): Int = applianceDao.update(appliance)

    suspend fun delete(id: Long): Int = applianceDao.deleteById(id)

    fun observeAll(): Flow<List<Appliance>> = applianceDao.observeAll()

    suspend fun getById(id: Long): Appliance? = applianceDao.getById(id)
}
