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
    version = 1,
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

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detachment_database"
                )
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
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val scheduleDao = database.scheduleRuleDao()
            val settingsDao = database.appSettingsDao()

            // Seed Master PIN
            settingsDao.setSetting(AppSettingsEntity("master_pin", "1234"))
            settingsDao.setSetting(AppSettingsEntity("distractions_resisted", "0"))

            // Seed Default Focus Schedules
            val initialSchedules = listOf(
                ScheduleRuleEntity(
                    title = "Study Hours Focus",
                    type = "STUDY",
                    startHour = 9,
                    startMinute = 0,
                    endHour = 17,
                    endMinute = 0,
                    activeDays = "MON,TUE,WED,THU,FRI",
                    isEnabled = true,
                    blockedTarget = "DISTRACTING"
                ),
                ScheduleRuleEntity(
                    title = "Night Sleep Sanctuary",
                    type = "SLEEP",
                    startHour = 22,
                    startMinute = 30,
                    endHour = 7,
                    endMinute = 0,
                    activeDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                    isEnabled = true,
                    blockedTarget = "ALL_NON_ESSENTIAL"
                ),
                ScheduleRuleEntity(
                    title = "Deep Work Session",
                    type = "WORK",
                    startHour = 19,
                    startMinute = 0,
                    endHour = 21,
                    endMinute = 0,
                    activeDays = "MON,TUE,WED,THU",
                    isEnabled = false,
                    blockedTarget = "ALL_NON_ESSENTIAL"
                )
            )
            scheduleDao.insertRules(initialSchedules)
        }
    }
}
