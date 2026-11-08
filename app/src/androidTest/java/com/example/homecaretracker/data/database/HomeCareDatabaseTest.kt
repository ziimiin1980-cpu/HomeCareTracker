package com.example.homecaretracker.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.entity.MaintenanceTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeCareDatabaseTest {
    private lateinit var database: HomeCareDatabase

    @Before
    fun createDatabase() {
        val context: Context =
            InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            HomeCareDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun entitiesSupportCrudAndEmitUpdatedLists() = runBlocking {
        val applianceId = database.applianceDao().insert(
            Appliance(name = "Стиральная машина", category = "Крупная техника")
        )
        val appliance = database.applianceDao().getById(applianceId)
        assertEquals("Стиральная машина", appliance?.name)
        assertEquals(1, database.applianceDao().observeAll().first().size)

        assertEquals(
            1,
            database.applianceDao().update(
                appliance!!.copy(name = "Стиральная машина Bosch")
            )
        )
        assertEquals(
            "Стиральная машина Bosch",
            database.applianceDao().getById(applianceId)?.name
        )

        val taskId = database.maintenanceTaskDao().insert(
            MaintenanceTask(
                applianceId = applianceId,
                title = "Очистить фильтр",
                intervalDays = 90,
                nextDueDate = 1_800_000_000_000
            )
        )
        val task = database.maintenanceTaskDao().getById(taskId)
        assertEquals("Очистить фильтр", task?.title)
        assertEquals(1, database.maintenanceTaskDao().observeAll().first().size)
        assertEquals(
            1,
            database.maintenanceTaskDao().update(task!!.copy(isActive = false))
        )
        assertEquals(false, database.maintenanceTaskDao().getById(taskId)?.isActive)

        val historyId = database.maintenanceHistoryDao().insert(
            MaintenanceHistory(
                applianceId = applianceId,
                taskTitle = "Очистить фильтр",
                completedAt = 1_700_000_000_000
            )
        )
        val history = database.maintenanceHistoryDao().getById(historyId)
        assertEquals("Очистить фильтр", history?.taskTitle)
        assertEquals(1, database.maintenanceHistoryDao().observeAll().first().size)
        assertEquals(
            1,
            database.maintenanceHistoryDao().update(
                history!!.copy(description = "Фильтр очищен")
            )
        )
        assertEquals(
            "Фильтр очищен",
            database.maintenanceHistoryDao().getById(historyId)?.description
        )

        assertEquals(1, database.maintenanceTaskDao().deleteById(taskId))
        assertEquals(1, database.maintenanceHistoryDao().deleteById(historyId))
        assertEquals(1, database.applianceDao().deleteById(applianceId))
        assertNull(database.applianceDao().getById(applianceId))
    }

    @Test
    fun deletingApplianceCascadesToTasksAndHistory() = runBlocking {
        val applianceId = database.applianceDao().insert(
            Appliance(name = "Холодильник", category = "Крупная техника")
        )
        val taskId = database.maintenanceTaskDao().insert(
            MaintenanceTask(
                applianceId = applianceId,
                title = "Проверить уплотнитель",
                intervalDays = 180,
                nextDueDate = 1_800_000_000_000
            )
        )
        val historyId = database.maintenanceHistoryDao().insert(
            MaintenanceHistory(
                applianceId = applianceId,
                taskTitle = "Проверить уплотнитель",
                completedAt = 1_700_000_000_000
            )
        )

        database.applianceDao().deleteById(applianceId)

        assertNull(database.maintenanceTaskDao().getById(taskId))
        assertNull(database.maintenanceHistoryDao().getById(historyId))
    }

    @Test
    fun completingTaskAtomicallyUpdatesScheduleAndAddsHistorySnapshot() = runBlocking {
        val applianceId = database.applianceDao().insert(
            Appliance(name = "Холодильник", category = "Кухня")
        )
        val taskId = database.maintenanceTaskDao().insert(
            MaintenanceTask(
                applianceId = applianceId,
                title = "Очистить решётку",
                description = "Снять пыль",
                intervalDays = 30,
                nextDueDate = 1_800_000_000_000
            )
        )
        val task = database.maintenanceTaskDao().getById(taskId)!!
        val appliance = database.applianceDao().getById(applianceId)!!
        val history = MaintenanceHistory(
            applianceId = applianceId,
            applianceName = appliance.name,
            taskTitle = task.title,
            description = task.description,
            completedAt = 1_700_000_000_000
        )

        assertTrue(
            database.maintenanceTaskDao().completeTask(
                task.copy(nextDueDate = 1_900_000_000_000),
                history
            )
        )
        assertEquals(
            1_900_000_000_000,
            database.maintenanceTaskDao().getById(taskId)?.nextDueDate
        )
        val savedHistory = database.maintenanceHistoryDao().observeAll().first().single()
        assertEquals("Холодильник", savedHistory.applianceName)
        assertEquals("Очистить решётку", savedHistory.taskTitle)

        database.maintenanceTaskDao().update(task.copy(isActive = false))
        assertEquals(false, database.maintenanceTaskDao().getById(taskId)?.isActive)
        database.maintenanceTaskDao().update(task.copy(isActive = true))
        assertEquals(true, database.maintenanceTaskDao().getById(taskId)?.isActive)
        database.applianceDao().update(appliance.copy(name = "Новый холодильник"))
        database.maintenanceTaskDao().update(task.copy(title = "Новая задача"))
        val preservedHistory = database.maintenanceHistoryDao().getById(savedHistory.id)
        assertEquals("Холодильник", preservedHistory?.applianceName)
        assertEquals("Очистить решётку", preservedHistory?.taskTitle)
    }

    @Test
    fun failedHistoryInsertRollsBackTaskUpdate() = runBlocking {
        val applianceId = database.applianceDao().insert(
            Appliance(name = "Пылесос", category = "Уборка")
        )
        val taskId = database.maintenanceTaskDao().insert(
            MaintenanceTask(
                applianceId = applianceId,
                title = "Проверить фильтр",
                intervalDays = 14,
                nextDueDate = 1_800_000_000_000
            )
        )
        val task = database.maintenanceTaskDao().getById(taskId)!!
        try {
            database.maintenanceTaskDao().completeTask(
                task.copy(nextDueDate = 1_900_000_000_000),
                MaintenanceHistory(
                    applianceId = -1,
                    applianceName = "Не существует",
                    taskTitle = task.title,
                    completedAt = 1_700_000_000_000
                )
            )
            throw AssertionError("Expected foreign-key failure")
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            assertEquals(
                task.nextDueDate,
                database.maintenanceTaskDao().getById(taskId)?.nextDueDate
            )
            assertEquals(0, database.maintenanceHistoryDao().observeAll().first().size)
        }
    }

    @Test
    fun migrationAddsApplianceNameSnapshotToExistingHistory() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "migration-test-${System.currentTimeMillis()}.db"
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()
        val oldDatabase = SQLiteDatabase.openOrCreateDatabase(file, null)
        oldDatabase.execSQL(
            "CREATE TABLE appliances (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT NOT NULL, category TEXT NOT NULL, manufacturer TEXT, model TEXT, " +
                "purchaseDate INTEGER, notes TEXT)"
        )
        oldDatabase.execSQL(
            "CREATE INDEX index_appliances_category ON appliances (category)"
        )
        oldDatabase.execSQL(
            "CREATE TABLE maintenance_tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "applianceId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT, " +
                "intervalDays INTEGER NOT NULL, nextDueDate INTEGER NOT NULL, " +
                "isActive INTEGER NOT NULL, FOREIGN KEY(applianceId) REFERENCES appliances(id) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        oldDatabase.execSQL(
            "CREATE INDEX index_maintenance_tasks_applianceId " +
                "ON maintenance_tasks (applianceId)"
        )
        oldDatabase.execSQL(
            "CREATE TABLE maintenance_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "applianceId INTEGER NOT NULL, taskTitle TEXT NOT NULL, description TEXT, " +
                "completedAt INTEGER NOT NULL, FOREIGN KEY(applianceId) REFERENCES appliances(id) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        oldDatabase.execSQL(
            "CREATE INDEX index_maintenance_history_applianceId " +
                "ON maintenance_history (applianceId)"
        )
        oldDatabase.execSQL(
            "INSERT INTO appliances (id, name, category) VALUES (1, 'Старый прибор', 'Дом')"
        )
        oldDatabase.execSQL(
            "INSERT INTO maintenance_tasks " +
                "(id, applianceId, title, intervalDays, nextDueDate, isActive) " +
                "VALUES (1, 1, 'Старая задача', 30, 1800000000000, 1)"
        )
        oldDatabase.execSQL(
            "INSERT INTO maintenance_history (applianceId, taskTitle, completedAt) " +
                "VALUES (1, 'Старая задача', 1700000000000)"
        )
        oldDatabase.version = 1
        oldDatabase.close()

        val migratedDatabase = Room.databaseBuilder(
            context,
            HomeCareDatabase::class.java,
            databaseName
        ).addMigrations(
            HomeCareDatabase.MIGRATION_1_2,
            HomeCareDatabase.MIGRATION_2_3
        ).build()
        try {
            val record = migratedDatabase.maintenanceHistoryDao().getById(1L)
            assertEquals("Старый прибор", record?.applianceName)
            assertEquals("Старая задача", record?.taskTitle)
            assertEquals(
                false,
                migratedDatabase.maintenanceTaskDao().getById(1L)?.notificationsEnabled
            )
        } finally {
            migratedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }
}
