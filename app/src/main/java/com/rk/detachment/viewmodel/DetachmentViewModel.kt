package com.rk.detachment.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import com.rk.detachment.data.local.entities.WebsiteLimitEntity
import com.rk.detachment.data.model.ImportSummary
import com.rk.detachment.data.model.YouVsYouComparison
import com.rk.detachment.data.repository.DetachmentRepository
import com.rk.detachment.util.AppManagerHelper
import com.rk.detachment.util.HeadsUpNotchPillManager
import com.rk.detachment.util.TemporaryUnlockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetachmentUiState(
    val allApps: List<AppLimitEntity> = emptyList(),
    val allWebsites: List<WebsiteLimitEntity> = emptyList(),
    val scheduleRules: List<ScheduleRuleEntity> = emptyList(),
    val pomodoroSessions: List<PomodoroSessionEntity> = emptyList(),
    val masterPin: String = "1234",
    val isAppAuthEnabled: Boolean = false,
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
    val isDelayForDistractingApps: Boolean = true,
    val isHeadsUpPillEnabled: Boolean = true,
    val isSelfRestraintActive: Boolean = false,
    val selfRestraintSecondsRemaining: Int = 0,
    val selfRestraintTotalSeconds: Int = 0,
    val selfRestraintTarget: String = "DISTRACTING",
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
        get() = allApps.filter { it.isEffectiveShieldActive(isDelayForDistractingApps) }

    fun isShieldActive(packageName: String): Boolean {
        return allApps.find { it.packageName == packageName }?.isEffectiveShieldActive(isDelayForDistractingApps) ?: false
    }

    fun isShieldActive(app: AppLimitEntity): Boolean {
        return app.isEffectiveShieldActive(isDelayForDistractingApps)
    }

    val lockedAppsCount: Int
        get() = allApps.count { it.isCurrentlyLocked() }

    val combinedFocusMinutes: Int
        get() {
            val pomodoroMins = pomodoroSessions.sumOf { it.durationMinutes }
            val productiveAppsMins = allApps.filter {
                val cat = it.category.lowercase()
                cat == "productivity" || cat == "education" || cat == "reading" || cat == "work" || (it.isEssential && !it.isDistracting)
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
        database.appSettingsDao(),
        database.websiteLimitDao()
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
    private var selfRestraintTickerJob: Job? = null

    init {
        com.rk.detachment.service.PomodoroManager.initialize(application, repository, database)
        com.rk.detachment.service.PomodoroManager.onShowMessage = { showMessage(it) }
        checkPermissionsAndRefresh()

        viewModelScope.launch {
            AppDatabase.syncDefaultSchedulesIfNeeded(database)
            AppDatabase.syncDefaultWebsitesIfNeeded(database)
        }

        viewModelScope.launch {
            repository.allWebsites.collect { websites ->
                _uiState.value = _uiState.value.copy(allWebsites = websites)
            }
        }

        viewModelScope.launch {
            repository.allApps.collect { apps ->
                val filtered = apps.filter { !AppManagerHelper.isLauncherOrSystemPackage(it.packageName, emptySet(), application) }
                _uiState.value = _uiState.value.copy(allApps = filtered)
                updateConsciousnessData()
            }
        }
        
        viewModelScope.launch {
            com.rk.detachment.service.PomodoroManager.state.collect { pState ->
                _uiState.value = _uiState.value.copy(
                    isBlackoutActive = pState.isBlackoutActive,
                    isPomodoroRunning = pState.isPomodoroRunning,
                    blackoutTotalSeconds = pState.blackoutTotalSeconds,
                    blackoutSecondsRemaining = pState.blackoutSecondsRemaining,
                    pomodoroSessionTag = pState.pomodoroSessionTag
                )
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
            repository.isAppAuthEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isAppAuthEnabled = enabled)
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

        viewModelScope.launch {
            repository.isDelayForDistractingApps.collect { enabled ->
                _uiState.value = _uiState.value.copy(isDelayForDistractingApps = enabled)
            }
        }

        viewModelScope.launch {
            repository.isHeadsUpPillEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isHeadsUpPillEnabled = enabled)
            }
        }

        viewModelScope.launch {
            repository.selfRestraintEndTime.collect { endStr ->
                val endTime = endStr?.toLongOrNull() ?: 0L
                val now = System.currentTimeMillis()
                if (endTime > now) {
                    val totalSec = repository.getSetting("self_restraint_total_seconds")?.toIntOrNull() ?: 1800
                    val target = repository.getSetting("self_restraint_target") ?: "DISTRACTING"
                    val remainingSec = ((endTime - now) / 1000L).toInt().coerceAtLeast(0)
                    _uiState.value = _uiState.value.copy(
                        isSelfRestraintActive = true,
                        selfRestraintSecondsRemaining = remainingSec,
                        selfRestraintTotalSeconds = totalSec,
                        selfRestraintTarget = target
                    )
                    startSelfRestraintTicker(endTime)
                } else {
                    if (_uiState.value.isSelfRestraintActive) {
                        _uiState.value = _uiState.value.copy(
                            isSelfRestraintActive = false,
                            selfRestraintSecondsRemaining = 0
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.selfRestraintTarget.collect { target ->
                if (target != null) {
                    _uiState.value = _uiState.value.copy(selfRestraintTarget = target)
                }
            }
        }

        checkDailyResetAndRefreshUsage()
        scanAndSyncRealApps()

        activeTimeTickerJob = viewModelScope.launch {
            while (true) {
                delay(10000L)
                checkDailyResetAndRefreshUsage()
            }
        }
    }
    fun checkDailyResetAndRefreshUsage() {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val wasReset = repository.checkAndResetDailyUsageIfNeeded()
            if (wasReset) {
                val existing = repository.allApps.first()
                val scanned = AppManagerHelper.scanRealInstalledApps(app, existing)
                if (scanned.isNotEmpty()) {
                    repository.syncApps(scanned)
                }
                updateConsciousnessData()
            } else if (AppManagerHelper.hasUsageStatsPermission(app)) {
                val todayUsageMap = AppManagerHelper.getTodayUsageMinutesMap(app)
                val currentApps = repository.allApps.first()
                val toUpdate = mutableListOf<Pair<String, Int>>()
                for (installedApp in currentApps) {
                    val realMins = todayUsageMap[installedApp.packageName] ?: 0
                    val targetMins = maxOf(installedApp.usedTodayMinutes, realMins)
                    if (targetMins != installedApp.usedTodayMinutes) {
                        toUpdate.add(installedApp.packageName to targetMins)
                    }
                }
                for ((pkg, mins) in toUpdate) {
                    repository.updateUsedMinutes(pkg, mins)
                }
            }
        }
    }

    fun checkPermissionsAndRefresh() {
        checkDailyResetAndRefreshUsage()
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
                repository.checkAndResetDailyUsageIfNeeded()
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
            val yesterdayResisted = repository.getYesterdayDistractionsResisted()
            val pinUnlocksToday = repository.getEmergencyPinUnlocksToday()
            val pinUnlocksYesterday = repository.getEmergencyPinUnlocksYesterday()
            val result = AppManagerHelper.calculateConsciousnessData(
                context = app,
                allApps = currentApps,
                distractionsResisted = resisted,
                totalFocusMinutes = focusMins,
                yesterdayDistractionsResisted = yesterdayResisted,
                emergencyPinUnlocksToday = pinUnlocksToday,
                emergencyPinUnlocksYesterday = pinUnlocksYesterday
            )
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

    fun updateAppLimit(packageName: String, limitMinutes: Int, isHardMode: Boolean = false) {
        viewModelScope.launch {
            repository.updateAppLimit(packageName, limitMinutes, isHardMode)
            val suffix = if (isHardMode) " (Hard mode)" else ""
            showMessage("Updated limit to ${limitMinutes}m$suffix")
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
        val app = _uiState.value.allApps.find { it.packageName == packageName }
        if (app?.isEssential == true && isDistracting) {
            showMessage("${app.appName} is an Essential app and cannot be marked as distracting.")
            return
        }
        viewModelScope.launch {
            repository.toggleDistracting(packageName, isDistracting)
        }
    }

    fun toggleShieldActive(packageName: String, isShieldActive: Boolean) {
        val app = _uiState.value.allApps.find { it.packageName == packageName }
        if (app?.isEssential == true && isShieldActive) {
            showMessage("${app.appName} is an Essential app and is exempt from Distraction Shield.")
            return
        }
        viewModelScope.launch {
            repository.toggleShieldActive(packageName, isShieldActive)
            if (!isShieldActive && _uiState.value.isDelayForDistractingApps) {
                if (app?.isDistracting == true) {
                    repository.toggleDistracting(packageName, false)
                }
            }
        }
    }

    fun setDelayForDistractingApps(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDelayForDistractingApps(enabled)
        }
    }

    fun toggleEssential(packageName: String, isEssential: Boolean) {
        viewModelScope.launch {
            val success = repository.toggleEssential(packageName, isEssential)
            if (!success) {
                showMessage("Maximum 10 essential apps allowed for Detachment Blackout mode!")
            } else if (isEssential) {
                val app = _uiState.value.allApps.find { it.packageName == packageName }
                showMessage("${app?.appName ?: "App"} marked as Essential (immune to limits and shields).")
            }
        }
    }

    fun toggleManualLock(packageName: String, isLocked: Boolean) {
        val app = _uiState.value.allApps.find { it.packageName == packageName }
        if (app?.isEssential == true && isLocked) {
            showMessage("${app.appName} is an Essential app and cannot be locked.")
            return
        }
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

    fun setAppAuthEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAppAuthEnabled(enabled)
            if (enabled) {
                showMessage("App launch authentication enabled.")
            } else {
                showMessage("App launch authentication disabled.")
            }
        }
    }

    fun updateMasterPin(newPin: String) {
        viewModelScope.launch {
            repository.setMasterPin(newPin)
            showMessage("Master Passcode updated successfully!")
        }
    }

    fun updateWebsiteLimit(domain: String, limitMinutes: Int, isHardMode: Boolean) {
        viewModelScope.launch {
            repository.updateWebsiteLimit(domain, limitMinutes, isHardMode)
            showMessage("Updated limit for $domain")
        }
    }

    fun toggleWebsiteLock(domain: String, isLocked: Boolean) {
        viewModelScope.launch {
            repository.toggleWebsiteLock(domain, isLocked)
            showMessage(if (isLocked) "Locked $domain" else "Unlocked $domain")
        }
    }

    fun toggleWebsiteEnabled(domain: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleWebsiteEnabled(domain, isEnabled)
            showMessage(if (isEnabled) "Enabled blocking for $domain" else "Disabled blocking for $domain")
        }
    }

    fun unlockWebsite(domain: String, minutes: Int) {
        viewModelScope.launch {
            val expiry = System.currentTimeMillis() + minutes * 60 * 1000L
            TemporaryUnlockManager.setUnlock(domain, expiry)
            repository.unlockWebsite(domain, expiry)
            showMessage("Unlocked $domain for $minutes minutes")
        }
    }

    fun relockWebsite(domain: String) {
        viewModelScope.launch {
            TemporaryUnlockManager.removeUnlock(domain)
            repository.relockWebsite(domain)
            showMessage("Relocked $domain")
        }
    }

    fun addCustomWebsite(domain: String, displayName: String, category: String, limitMinutes: Int) {
        viewModelScope.launch {
            val cleanDomain = domain.trim().lowercase()
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
                .substringBefore("/")
                .trim()
            if (cleanDomain.isBlank()) {
                showMessage("Invalid website domain")
                return@launch
            }
            val name = displayName.trim().ifBlank { cleanDomain }
            val website = WebsiteLimitEntity(
                domain = cleanDomain,
                displayName = name,
                category = category.trim().ifBlank { "Social" },
                dailyLimitMinutes = limitMinutes,
                isEnabled = true
            )
            repository.addCustomWebsite(website)
            showMessage("Added $cleanDomain to website limits")
        }
    }

    fun deleteCustomWebsite(domain: String) {
        viewModelScope.launch {
            repository.deleteCustomWebsite(domain)
            showMessage("Removed $domain from website limits")
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

    fun resetSchedulesToDefaults() {
        viewModelScope.launch {
            repository.resetSchedulesToDefaults()
            showMessage("Focus schedules reset to default database rules")
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

        if (uiState.value.isBlackoutActive && app.isEssential) {
            com.rk.detachment.service.PomodoroManager.setOverlayHidden(true)
        }

        if (uiState.value.isSelfRestraintActive) {
            val shouldBlock = when (uiState.value.selfRestraintTarget) {
                "ALL_NON_ESSENTIAL" -> !app.isEssential
                else -> app.isDistracting
            }
            if (shouldBlock && !app.isTemporaryUnlocked()) {
                val targetDesc = if (uiState.value.selfRestraintTarget == "ALL_NON_ESSENTIAL") "non-essential app" else "distracting app"
                showMessage("${app.appName} is locked by active Self Restraint Mode ($targetDesc).")
                return
            }
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
                "${app.appName} daily usage limit exceeded."
            }
            showMessage(reason)
            return
        }

        val launched = AppManagerHelper.launchRealApp(getApplication(), app.packageName)
        if (!launched) {
            showMessage("Could not open ${app.appName}. Launch intent unavailable.")
        }
    }

    fun startSelfRestraint(durationMinutes: Int, target: String) {
        selfRestraintTickerJob?.cancel()
        val totalSecs = durationMinutes * 60
        val endTime = System.currentTimeMillis() + totalSecs * 1000L
        viewModelScope.launch {
            repository.startSelfRestraint(endTime, totalSecs, target)
            _uiState.value = _uiState.value.copy(
                isSelfRestraintActive = true,
                selfRestraintSecondsRemaining = totalSecs,
                selfRestraintTotalSeconds = totalSecs,
                selfRestraintTarget = target
            )
            showMessage("Self Restraint Mode started for ${durationMinutes}m")
            startSelfRestraintTicker(endTime, durationMinutes)
        }
    }

    private fun startSelfRestraintTicker(endTime: Long, durationMinutes: Int = 0) {
        selfRestraintTickerJob?.cancel()
        selfRestraintTickerJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remaining = ((endTime - now) / 1000L).toInt().coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(selfRestraintSecondsRemaining = remaining)
                if (remaining <= 0) {
                    val mins = if (durationMinutes > 0) durationMinutes else (_uiState.value.selfRestraintTotalSeconds / 60)
                    repository.stopSelfRestraint()
                    repository.savePomodoroSession(mins, "Self Restraint", 0)
                    _uiState.value = _uiState.value.copy(
                        isSelfRestraintActive = false,
                        selfRestraintSecondsRemaining = 0
                    )
                    showMessage("Self Restraint completed! +$mins min focus logged.")
                    break
                }
                delay(1000L)
            }
        }
    }

    fun stopSelfRestraint() {
        selfRestraintTickerJob?.cancel()
        viewModelScope.launch {
            repository.stopSelfRestraint()
            _uiState.value = _uiState.value.copy(
                isSelfRestraintActive = false,
                selfRestraintSecondsRemaining = 0
            )
            showMessage("Self Restraint session ended.")
        }
    }

    fun startPomodoroBlackout(durationMinutes: Int = 25, tag: String = "Deep Work") {
        com.rk.detachment.service.PomodoroManager.startBlackout(durationMinutes, tag)
    }

    fun pausePomodoro() {
        com.rk.detachment.service.PomodoroManager.pause()
    }

    fun resumePomodoro() {
        com.rk.detachment.service.PomodoroManager.resume()
    }

    fun stopBlackout() {
        com.rk.detachment.service.PomodoroManager.stop()
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun setHeadsUpPillEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHeadsUpPillEnabled(enabled)
        }
    }

    fun exportSettings(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupData()
                onSuccess(json)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to export settings")
            }
        }
    }

    fun importSettings(jsonString: String, onResult: (Result<ImportSummary>) -> Unit) {
        viewModelScope.launch {
            try {
                val summary = repository.importBackupData(jsonString)
                showMessage("Restored ${summary.appsUpdated} apps, ${summary.schedulesRestored} schedules, and settings!")
                onResult(Result.success(summary))
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
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

