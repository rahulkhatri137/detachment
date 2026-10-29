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
import com.rk.detachment.data.local.dao.WebsiteLimitDao
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import com.rk.detachment.data.local.entities.WebsiteLimitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppLimitEntity::class,
        ScheduleRuleEntity::class,
        PomodoroSessionEntity::class,
        AppSettingsEntity::class,
        WebsiteLimitEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLimitDao(): AppLimitDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun websiteLimitDao(): WebsiteLimitDao

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

        val DEFAULT_WEBSITES = listOf(
            WebsiteLimitEntity(domain = "instagram.com", displayName = "Instagram", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "facebook.com", displayName = "Facebook", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "x.com", displayName = "X (Twitter)", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "tiktok.com", displayName = "TikTok", category = "Video", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "threads.net", displayName = "Threads", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "youtube.com", displayName = "YouTube", category = "Video", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "snapchat.com", displayName = "Snapchat", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true),
            WebsiteLimitEntity(domain = "reddit.com", displayName = "Reddit", category = "Social", dailyLimitMinutes = 0, isDistracting = true, isEnabled = true)
        )

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detachment_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
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
                        syncDefaultWebsitesIfNeeded(database)
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

        suspend fun syncDefaultWebsitesIfNeeded(database: AppDatabase) {
            val websiteDao = database.websiteLimitDao()
            val settingsDao = database.appSettingsDao()
            val presetVersion = settingsDao.getValue("key_websites_schema_preset_version")
            if (presetVersion != "3") {
                websiteDao.resetAllLimitsToZero()
                websiteDao.insertDefaultWebsites(DEFAULT_WEBSITES)
                settingsDao.setSetting(AppSettingsEntity("key_websites_schema_preset_version", "3"))
            } else {
                val count = websiteDao.getWebsitesCount()
                if (count == 0) {
                    websiteDao.insertDefaultWebsites(DEFAULT_WEBSITES)
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val scheduleDao = database.scheduleRuleDao()
            val settingsDao = database.appSettingsDao()
            val websiteDao = database.websiteLimitDao()

            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            settingsDao.setSetting(AppSettingsEntity("master_pin", "1234"))
            settingsDao.setSetting(AppSettingsEntity("distractions_resisted", "0"))
            settingsDao.setSetting(AppSettingsEntity("distractions_resisted_yesterday", "0"))
            settingsDao.setSetting(AppSettingsEntity("key_last_usage_reset_date", today))
            settingsDao.setSetting(AppSettingsEntity("key_schedules_schema_version", "2"))

            scheduleDao.deleteAllRules()
            scheduleDao.insertRules(DEFAULT_SCHEDULES)
            websiteDao.insertDefaultWebsites(DEFAULT_WEBSITES)
        }
    }
}
