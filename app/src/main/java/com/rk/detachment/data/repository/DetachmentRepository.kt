package com.rk.detachment.data.repository

import com.rk.detachment.data.local.dao.AppLimitDao
import com.rk.detachment.data.local.dao.AppSettingsDao
import com.rk.detachment.data.local.dao.PomodoroDao
import com.rk.detachment.data.local.dao.ScheduleRuleDao
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DetachmentRepository(
    private val appLimitDao: AppLimitDao,
    private val scheduleRuleDao: ScheduleRuleDao,
    private val pomodoroDao: PomodoroDao,
    private val appSettingsDao: AppSettingsDao
) {
    val allApps: Flow<List<AppLimitEntity>> = appLimitDao.getAllApps()
    val distractingApps: Flow<List<AppLimitEntity>> = appLimitDao.getDistractingApps()
    val essentialApps: Flow<List<AppLimitEntity>> = appLimitDao.getEssentialApps()
    val scheduleRules: Flow<List<ScheduleRuleEntity>> = scheduleRuleDao.getAllRules()
    val pomodoroSessions: Flow<List<PomodoroSessionEntity>> = pomodoroDao.getAllSessions()
    val totalFocusMinutes: Flow<Int?> = pomodoroDao.getTotalFocusMinutes()
    val totalSessionsCount: Flow<Int> = pomodoroDao.getTotalSessionsCount()
    val masterPin: Flow<String?> = appSettingsDao.observeValue("master_pin")
    val distractionsResisted: Flow<String?> = appSettingsDao.observeValue("distractions_resisted")
    val delaySeconds: Flow<String?> = appSettingsDao.observeValue("key_delay_seconds")
    val unlockMinutes: Flow<String?> = appSettingsDao.observeValue("key_unlock_minutes")

    suspend fun setDelaySeconds(seconds: Int) {
        appSettingsDao.setSetting(AppSettingsEntity("key_delay_seconds", seconds.toString()))
    }

    suspend fun setUnlockMinutes(minutes: Int) {
        appSettingsDao.setSetting(AppSettingsEntity("key_unlock_minutes", minutes.toString()))
    }

    suspend fun updateAppLimit(packageName: String, limitMinutes: Int) {
        appLimitDao.updateLimit(packageName, limitMinutes)
    }

    suspend fun addUsage(packageName: String, minutes: Int) {
        appLimitDao.addUsage(packageName, minutes)
    }

    suspend fun toggleDistracting(packageName: String, isDistracting: Boolean) {
        appLimitDao.setDistracting(packageName, isDistracting)
    }

    suspend fun toggleEssential(packageName: String, isEssential: Boolean): Boolean {
        if (isEssential) {
            val currentCount = appLimitDao.getEssentialCount().first()
            if (currentCount >= 10) {
                return false
            }
        }
        appLimitDao.setEssential(packageName, isEssential)
        return true
    }

    suspend fun toggleManualLock(packageName: String, isLocked: Boolean) {
        appLimitDao.setManualLock(packageName, isLocked)
    }

    suspend fun unlockApp(packageName: String, minutes: Int = 15): Long {
        val durationMillis = minutes * 60 * 1000L
        val expiryTime = System.currentTimeMillis() + durationMillis
        appLimitDao.setTemporaryUnlock(packageName, expiryTime)
        return expiryTime
    }

    suspend fun unlockAppFor15Minutes(packageName: String): Long {
        return unlockApp(packageName, 15)
    }

    suspend fun relockApp(packageName: String) {
        appLimitDao.cancelTemporaryUnlock(packageName)
    }

    suspend fun verifyMasterPin(inputPin: String): Boolean {
        val currentPin = appSettingsDao.getValue("master_pin") ?: "1234"
        return inputPin == currentPin
    }

    suspend fun setMasterPin(newPin: String) {
        appSettingsDao.setSetting(AppSettingsEntity("master_pin", newPin))
    }

    suspend fun incrementDistractionsResisted() {
        val current = appSettingsDao.getValue("distractions_resisted")?.toIntOrNull() ?: 0
        appSettingsDao.setSetting(AppSettingsEntity("distractions_resisted", (current + 1).toString()))
    }

    suspend fun incrementAppOpens(packageName: String) {
        appLimitDao.incrementOpens(packageName)
    }

    suspend fun insertOrUpdateRule(rule: ScheduleRuleEntity) {
        if (rule.id == 0) {
            scheduleRuleDao.insertRule(rule)
        } else {
            scheduleRuleDao.updateRule(rule)
        }
    }

    suspend fun deleteRule(rule: ScheduleRuleEntity) {
        scheduleRuleDao.deleteRule(rule)
    }

    suspend fun toggleRule(id: Int, isEnabled: Boolean) {
        scheduleRuleDao.toggleRule(id, isEnabled)
    }

    suspend fun savePomodoroSession(durationMinutes: Int, tag: String, distractionsBlocked: Int) {
        pomodoroDao.insertSession(
            PomodoroSessionEntity(
                durationMinutes = durationMinutes,
                tag = tag,
                distractionsBlocked = distractionsBlocked
            )
        )
    }

    suspend fun addNewApp(app: AppLimitEntity) {
        appLimitDao.insertApp(app)
    }

    suspend fun syncApps(apps: List<AppLimitEntity>) {
        if (apps.isNotEmpty()) {
            appLimitDao.insertApps(apps)
            val packageNames = apps.map { it.packageName }
            appLimitDao.deleteAppsNotIn(packageNames)
        }
    }
}
