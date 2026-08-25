package com.rk.detachment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import com.rk.detachment.data.model.YouVsYouComparison
import com.rk.detachment.data.repository.DetachmentRepository
import com.rk.detachment.util.AppManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetachmentUiState(
    val allApps: List<AppLimitEntity> = emptyList(),
    val scheduleRules: List<ScheduleRuleEntity> = emptyList(),
    val pomodoroSessions: List<PomodoroSessionEntity> = emptyList(),
    val masterPin: String = "1234",
    val distractionsResistedCount: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalSessionsCount: Int = 0,
    val hasUsagePermission: Boolean = false,
    val isAccessibilityActive: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val isSyncingApps: Boolean = false,
    val isBlackoutActive: Boolean = false,
    val blackoutSecondsRemaining: Int = 25 * 60,
    val blackoutTotalSeconds: Int = 25 * 60,
    val isPomodoroRunning: Boolean = false,
    val isPomodoroBreak: Boolean = false,
    val pomodoroSessionTag: String = "Deep Work",
    val delaySeconds: Int = 15,
    val unlockMinutes: Int = 15,
    val statusMessage: String? = null,
    val consciousnessComparison: YouVsYouComparison = YouVsYouComparison()
) {
    val totalScreenTimeTodayMinutes: Int
        get() = allApps.sumOf { it.usedTodayMinutes }

    val totalDailyLimitMinutes: Int
        get() = allApps.filter { it.dailyLimitMinutes > 0 }.sumOf { it.dailyLimitMinutes }

    val essentialApps: List<AppLimitEntity>
        get() = allApps.filter { it.isEssential }

    val distractingApps: List<AppLimitEntity>
        get() = allApps.filter { it.isDistracting }

    val shieldActiveApps: List<AppLimitEntity>
        get() = allApps.filter { it.isShieldActive }

    val lockedAppsCount: Int
        get() = allApps.count { it.isCurrentlyLocked() }

    val combinedFocusMinutes: Int
        get() {
            val pomodoroMins = pomodoroSessions.sumOf { it.durationMinutes }
            val productiveAppsMins = allApps.filter {
                val cat = it.category.lowercase()
                cat == "productivity" || cat == "utilities" || cat == "education" || cat == "reading" || cat == "work" || (it.isEssential && !it.isDistracting)
            }.sumOf { it.usedTodayMinutes }
            val phoneFreeMins = consciousnessComparison.today.longestPhoneFreeMinutes
            return (pomodoroMins + phoneFreeMins + productiveAppsMins).coerceAtLeast(totalFocusMinutes)
        }

    val activeSchedules: List<ScheduleRuleEntity>
        get() = scheduleRules.filter { it.isCurrentlyActive() }

    val isFullProtectionActive: Boolean
        get() = hasUsagePermission && isAccessibilityActive
}

typealias ScreenTimeUiState = DetachmentUiState

class DetachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DetachmentRepository(
        database.appLimitDao(),
        database.scheduleRuleDao(),
        database.pomodoroDao(),
        database.appSettingsDao()
    )

    private val _uiState = MutableStateFlow(
        DetachmentUiState(
            hasUsagePermission = AppManagerHelper.hasUsageStatsPermission(application),
            isAccessibilityActive = AppManagerHelper.isAccessibilityServiceEnabled(application),
            hasOverlayPermission = AppManagerHelper.hasOverlayPermission(application)
        )
    )
    val uiState: StateFlow<DetachmentUiState> = _uiState.asStateFlow()

    private var pomodoroJob: Job? = null
    private var activeTimeTickerJob: Job? = null

    init {
        checkPermissionsAndRefresh()

        viewModelScope.launch {
            repository.allApps.collect { apps ->
                _uiState.value = _uiState.value.copy(allApps = apps)
                updateConsciousnessData()
            }
        }
        viewModelScope.launch {
            repository.scheduleRules.collect { schedules ->
                _uiState.value = _uiState.value.copy(scheduleRules = schedules)
            }
        }
        viewModelScope.launch {
            repository.pomodoroSessions.collect { sessions ->
                _uiState.value = _uiState.value.copy(pomodoroSessions = sessions)
            }
        }
        viewModelScope.launch {
            repository.masterPin.collect { pin ->
                _uiState.value = _uiState.value.copy(masterPin = pin ?: "1234")
            }
        }
        viewModelScope.launch {
            repository.distractionsResisted.collect { resisted ->
                val count = resisted?.toIntOrNull() ?: 0
                _uiState.value = _uiState.value.copy(distractionsResistedCount = count)
                updateConsciousnessData()
            }
        }
        viewModelScope.launch {
            repository.totalFocusMinutes.collect { focusMins ->
                val mins = focusMins ?: 0
                _uiState.value = _uiState.value.copy(totalFocusMinutes = mins)
                updateConsciousnessData()
            }
        }
        viewModelScope.launch {
            repository.totalSessionsCount.collect { count ->
                _uiState.value = _uiState.value.copy(totalSessionsCount = count)
            }
        }
        viewModelScope.launch {
            repository.delaySeconds.collect { secStr ->
                val sec = secStr?.toIntOrNull() ?: 15
                _uiState.value = _uiState.value.copy(delaySeconds = sec)
            }
        }
        viewModelScope.launch {
            repository.unlockMinutes.collect { minStr ->
                val mins = minStr?.toIntOrNull() ?: 15
                _uiState.value = _uiState.value.copy(unlockMinutes = mins)
            }
        }

        scanAndSyncRealApps()

        activeTimeTickerJob = viewModelScope.launch {
            while (true) {
                delay(3000L)
                checkPermissionsAndRefresh()
            }
        }
    }

    fun checkPermissionsAndRefresh() {
        val app = getApplication<Application>()
        val hasUsage = AppManagerHelper.hasUsageStatsPermission(app)
        val hasAccess = AppManagerHelper.isAccessibilityServiceEnabled(app)
        val hasOverlay = AppManagerHelper.hasOverlayPermission(app)

        val changed = _uiState.value.hasUsagePermission != hasUsage ||
                _uiState.value.isAccessibilityActive != hasAccess ||
                _uiState.value.hasOverlayPermission != hasOverlay

        if (changed) {
            _uiState.value = _uiState.value.copy(
                hasUsagePermission = hasUsage,
                isAccessibilityActive = hasAccess,
                hasOverlayPermission = hasOverlay
            )
            if (hasUsage) {
                scanAndSyncRealApps()
            }
        }
    }

    fun scanAndSyncRealApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingApps = true)
            withContext(Dispatchers.IO) {
                val app = getApplication<Application>()
                val existing = repository.allApps.first()
                val scanned = AppManagerHelper.scanRealInstalledApps(app, existing)
                if (scanned.isNotEmpty()) {
                    repository.syncApps(scanned)
                    AppManagerHelper.preloadIcons(app, scanned.map { it.packageName })
                }
            }
            _uiState.value = _uiState.value.copy(isSyncingApps = false)
            updateConsciousnessData()
        }
    }

    fun updateConsciousnessData() {
        viewModelScope.launch(Dispatchers.Default) {
            val app = getApplication<Application>()
            val currentApps = _uiState.value.allApps
            val resisted = _uiState.value.distractionsResistedCount
            val focusMins = _uiState.value.combinedFocusMinutes
            val result = AppManagerHelper.calculateConsciousnessData(app, currentApps, resisted, focusMins)
            _uiState.value = _uiState.value.copy(consciousnessComparison = result)
        }
    }

    fun refreshConsciousnessMetrics() {
        scanAndSyncRealApps()
    }

    fun setDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            repository.setDelaySeconds(seconds)
            showMessage("Distraction friction delay set to ${seconds}s")
        }
    }

    fun scanInstalledApps() {
        scanAndSyncRealApps()
    }

    fun openUsageAccessSettings(context: android.content.Context? = null) {
        AppManagerHelper.openUsageAccessSettings(context ?: getApplication())
    }

    fun openAccessibilitySettings(context: android.content.Context? = null) {
        AppManagerHelper.openAccessibilitySettings(context ?: getApplication())
    }

    fun openOverlaySettings(context: android.content.Context? = null) {
        AppManagerHelper.openOverlaySettings(context ?: getApplication())
    }

    fun launchRealAppOrBlock(context: android.content.Context? = null, app: AppLimitEntity) {
        openRealApp(app)
    }

    fun openUsageSettings() {
        AppManagerHelper.openUsageAccessSettings(getApplication())
    }

    fun openAccessibilitySettings() {
        AppManagerHelper.openAccessibilitySettings(getApplication())
    }

    fun openOverlaySettings() {
        AppManagerHelper.openOverlaySettings(getApplication())
    }

    fun updateAppLimit(packageName: String, limitMinutes: Int) {
        viewModelScope.launch {
            repository.updateAppLimit(packageName, limitMinutes)
            showMessage("Updated limit to ${limitMinutes}m")
        }
    }

    fun updateAppCategory(packageName: String, category: String) {
        viewModelScope.launch {
            repository.updateAppCategory(packageName, category)
            showMessage("Updated category to $category")
            updateConsciousnessData()
        }
    }

    fun addUsage(packageName: String, additionalMinutes: Int) {
        viewModelScope.launch {
            repository.addUsage(packageName, additionalMinutes)
        }
    }

    fun toggleDistracting(packageName: String, isDistracting: Boolean) {
        viewModelScope.launch {
            repository.toggleDistracting(packageName, isDistracting)
        }
    }

    fun toggleShieldActive(packageName: String, isShieldActive: Boolean) {
        viewModelScope.launch {
            repository.toggleShieldActive(packageName, isShieldActive)
        }
    }

    fun toggleEssential(packageName: String, isEssential: Boolean) {
        viewModelScope.launch {
            val success = repository.toggleEssential(packageName, isEssential)
            if (!success) {
                showMessage("Maximum 10 essential apps allowed for Detachment Blackout mode!")
            }
        }
    }

    fun toggleManualLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            repository.toggleManualLock(packageName, isLocked)
        }
    }

    fun setUnlockMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.setUnlockMinutes(minutes)
            showMessage("Default unlock time period set to $minutes minutes")
        }
    }

    fun unlockApp(packageName: String, minutes: Int? = null) {
        val duration = minutes ?: uiState.value.unlockMinutes
        viewModelScope.launch {
            repository.unlockApp(packageName, duration)
            showMessage("Unlocked for $duration minutes! Detachment will relock automatically.")
            AppManagerHelper.launchRealApp(getApplication(), packageName)
        }
    }

    fun unlockAppFor15Minutes(packageName: String) {
        unlockApp(packageName, uiState.value.unlockMinutes)
    }

    fun relockApp(packageName: String) {
        viewModelScope.launch {
            repository.relockApp(packageName)
            showMessage("App has been locked by Detachment.")
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        return inputPin == uiState.value.masterPin
    }

    fun updateMasterPin(newPin: String) {
        viewModelScope.launch {
            repository.setMasterPin(newPin)
            showMessage("Master Passcode updated successfully!")
        }
    }

    fun recordDistractionResisted() {
        viewModelScope.launch {
            repository.incrementDistractionsResisted()
            showMessage("Detachment habit broken! Mindful focus preserved.")
        }
    }

    fun toggleScheduleRule(id: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleRule(id, isEnabled)
        }
    }

    fun saveScheduleRule(rule: ScheduleRuleEntity) {
        viewModelScope.launch {
            repository.insertOrUpdateRule(rule)
            showMessage("Saved focus schedule '${rule.title}'")
        }
    }

    fun deleteScheduleRule(rule: ScheduleRuleEntity) {
        viewModelScope.launch {
            repository.deleteRule(rule)
            showMessage("Deleted schedule rule")
        }
    }

    fun openRealApp(app: AppLimitEntity) {
        viewModelScope.launch {
            repository.incrementAppOpens(app.packageName)
        }

        if (uiState.value.isBlackoutActive && !app.isEssential) {
            showMessage("${app.appName} is blocked by active Detachment Blackout.")
            return
        }

        val activeSchedules = uiState.value.activeSchedules
        val isBlockedBySchedule = activeSchedules.any { rule ->
            when (rule.blockedTarget) {
                "DISTRACTING" -> app.isDistracting
                "ALL_NON_ESSENTIAL" -> !app.isEssential
                else -> true
            }
        }

        if (isBlockedBySchedule && !app.isTemporaryUnlocked()) {
            val rule = activeSchedules.first()
            showMessage("${app.appName} is locked by focus schedule '${rule.title}'.")
            return
        }

        if (app.isCurrentlyLocked()) {
            val reason = if (app.isLockedManually) {
                "${app.appName} is manually locked by Detachment."
            } else {
                "${app.appName} daily limit reached (${app.dailyLimitMinutes}m limit, ${app.usedTodayMinutes}m used)."
            }
            showMessage(reason)
            return
        }

        val launched = AppManagerHelper.launchRealApp(getApplication(), app.packageName)
        if (!launched) {
            showMessage("Could not open ${app.appName}. Launch intent unavailable.")
        }
    }

    fun startPomodoroBlackout(durationMinutes: Int = 25, tag: String = "Deep Work") {
        pomodoroJob?.cancel()
        val totalSecs = durationMinutes * 60
        _uiState.value = _uiState.value.copy(
            isBlackoutActive = true,
            isPomodoroRunning = true,
            blackoutTotalSeconds = totalSecs,
            blackoutSecondsRemaining = totalSecs,
            pomodoroSessionTag = tag,
            isPomodoroBreak = false
        )

        viewModelScope.launch {
            database.appSettingsDao().setSetting(AppSettingsEntity("is_blackout_active", "true"))
        }

        pomodoroJob = viewModelScope.launch {
            while (_uiState.value.blackoutSecondsRemaining > 0 && _uiState.value.isPomodoroRunning) {
                delay(1000L)
                val remaining = _uiState.value.blackoutSecondsRemaining - 1
                _uiState.value = _uiState.value.copy(
                    blackoutSecondsRemaining = remaining
                )
            }

            if (_uiState.value.blackoutSecondsRemaining <= 0) {
                repository.savePomodoroSession(durationMinutes, tag, 0)
                database.appSettingsDao().setSetting(AppSettingsEntity("is_blackout_active", "false"))
                showMessage("Detachment Blackout completed! +$durationMinutes min focus logged.")
                _uiState.value = _uiState.value.copy(
                    isPomodoroRunning = false,
                    isBlackoutActive = false
                )
            }
        }
    }

    fun pausePomodoro() {
        _uiState.value = _uiState.value.copy(
            isPomodoroRunning = false
        )
        pomodoroJob?.cancel()
    }

    fun resumePomodoro() {
        if (_uiState.value.blackoutSecondsRemaining > 0) {
            _uiState.value = _uiState.value.copy(
                isPomodoroRunning = true
            )
            viewModelScope.launch {
                database.appSettingsDao().setSetting(AppSettingsEntity("is_blackout_active", "true"))
            }
            val durationMinutes = _uiState.value.blackoutTotalSeconds / 60
            val tag = _uiState.value.pomodoroSessionTag
            pomodoroJob = viewModelScope.launch {
                while (_uiState.value.blackoutSecondsRemaining > 0 && _uiState.value.isPomodoroRunning) {
                    delay(1000L)
                    val remaining = _uiState.value.blackoutSecondsRemaining - 1
                    _uiState.value = _uiState.value.copy(
                        blackoutSecondsRemaining = remaining
                    )
                }

                if (_uiState.value.blackoutSecondsRemaining <= 0) {
                    repository.savePomodoroSession(durationMinutes, tag, 0)
                    database.appSettingsDao().setSetting(AppSettingsEntity("is_blackout_active", "false"))
                    showMessage("Detachment focus session completed!")
                    _uiState.value = _uiState.value.copy(
                        isPomodoroRunning = false,
                        isBlackoutActive = false
                    )
                }
            }
        }
    }

    fun stopBlackout() {
        pomodoroJob?.cancel()
        viewModelScope.launch {
            database.appSettingsDao().setSetting(AppSettingsEntity("is_blackout_active", "false"))
        }
        _uiState.value = _uiState.value.copy(
            isBlackoutActive = false,
            isPomodoroRunning = false
        )
        showMessage("Detachment Blackout ended.")
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    private fun showMessage(msg: String) {
        _uiState.value = _uiState.value.copy(statusMessage = msg)
    }

    override fun onCleared() {
        super.onCleared()
        pomodoroJob?.cancel()
        activeTimeTickerJob?.cancel()
    }
}

typealias ScreenTimeViewModel = DetachmentViewModel

