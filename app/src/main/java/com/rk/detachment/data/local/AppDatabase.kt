package com.rk.detachment.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rk.detachment.data.local.dao.AppLimitDao
import com.rk.detachment.data.local.dao.AppSettingsDao
import com.rk.detachment.data.local.dao.PomodoroDao
import com.rk.detachment.data.local.dao.ScheduleRuleDao
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppLimitEntity::class,
        ScheduleRuleEntity::class,
        PomodoroSessionEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val DEFAULT_SCHEDULES = listOf(
            ScheduleRuleEntity(
                id = 1,
                title = "Work Focus",
                type = "WORK",
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                activeDays = "MON,TUE,WED,THU,FRI",
                isEnabled = false,
                blockedTarget = "DISTRACTING"
            ),
            ScheduleRuleEntity(
                id = 2,
                title = "Night Sanctrum",
                type = "SLEEP",
                startHour = 21,
                startMinute = 0,
                endHour = 7,
                endMinute = 0,
                activeDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                isEnabled = true,
                blockedTarget = "ALL_NON_ESSENTIAL"
            ),
            ScheduleRuleEntity(
                id = 3,
                title = "Morning Tranquility",
                type = "SLEEP",
                startHour = 7,
                startMinute = 0,
                endHour = 8,
                endMinute = 0,
                activeDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                isEnabled = true,
                blockedTarget = "ALL_NON_ESSENTIAL"
            )
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detachment_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        syncDefaultSchedulesIfNeeded(database)
                    }
                }
            }
        }

        suspend fun syncDefaultSchedulesIfNeeded(database: AppDatabase) {
            val scheduleDao = database.scheduleRuleDao()
            val settingsDao = database.appSettingsDao()
            val syncedVersion = settingsDao.getValue("key_schedules_schema_version")
            val count = scheduleDao.getRulesCount()
            if (syncedVersion != "2" || count == 0) {
                scheduleDao.deleteAllRules()
                scheduleDao.insertRules(DEFAULT_SCHEDULES)
                settingsDao.setSetting(AppSettingsEntity("key_schedules_schema_version", "2"))
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val scheduleDao = database.scheduleRuleDao()
            val settingsDao = database.appSettingsDao()

            settingsDao.setSetting(AppSettingsEntity("master_pin", "1234"))
            settingsDao.setSetting(AppSettingsEntity("distractions_resisted", "0"))
            settingsDao.setSetting(AppSettingsEntity("key_schedules_schema_version", "2"))

            scheduleDao.deleteAllRules()
            scheduleDao.insertRules(DEFAULT_SCHEDULES)
        }
    }
}
