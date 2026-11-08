package com.example.homecaretracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.homecaretracker.data.dao.ApplianceDao
import com.example.homecaretracker.data.dao.MaintenanceHistoryDao
import com.example.homecaretracker.data.dao.MaintenanceTaskDao
import com.example.homecaretracker.data.entity.Appliance
import com.example.homecaretracker.data.entity.MaintenanceHistory
import com.example.homecaretracker.data.entity.MaintenanceTask

@Database(
    entities = [
        Appliance::class,
        MaintenanceTask::class,
        MaintenanceHistory::class
    ],
    version = 2,
    exportSchema = true
)
abstract class HomeCareDatabase : RoomDatabase() {
    abstract fun applianceDao(): ApplianceDao

    abstract fun maintenanceTaskDao(): MaintenanceTaskDao

    abstract fun maintenanceHistoryDao(): MaintenanceHistoryDao

    companion object {
        private const val DATABASE_NAME = "home_care_tracker.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE maintenance_history " +
                        "ADD COLUMN applianceName TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    """
                    UPDATE maintenance_history
                    SET applianceName = (
                        SELECT name FROM appliances
                        WHERE appliances.id = maintenance_history.applianceId
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var instance: HomeCareDatabase? = null

        fun getDatabase(context: Context): HomeCareDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeCareDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
