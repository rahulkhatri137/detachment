package com.rk.detachment.service

import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import com.rk.detachment.ui.BlockOverlayActivity
import com.rk.detachment.util.AppManagerHelper
import com.rk.detachment.util.HeadsUpNotchPillManager
import com.rk.detachment.util.TemporaryUnlockManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetachmentAccessibilityService : AccessibilityService() {
    private var cachedLauncherPackages: Set<String> = emptySet()
    private var lastLauncherCacheTime = 0L

    @Volatile
    private var cachedAppsMap: Map<String, AppLimitEntity> = emptyMap()
    @Volatile
    private var cachedSchedules: List<ScheduleRuleEntity> = emptyList()
    @Volatile
    private var cachedDelayForDistracting: Boolean = true
    @Volatile
    private var cachedDelaySeconds: Int = 15
    @Volatile
    private var cachedIsBlackout: Boolean = false
    @Volatile
    private var cachedSelfRestraintEndTime: Long = 0L
    @Volatile
    private var cachedSelfRestraintTarget: String = "DISTRACTING"
    @Volatile
    private var cachedIsParentalManaged: Boolean = false
    @Volatile
    private var cachedUninstallProtectionEnabled: Boolean = true

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: AppDatabase? = null

    private var currentForegroundPackage: String? = null
    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    private var monitoredPackage: String? = null
    private var monitoredAppName: String? = null
    private var monitoredBaseMinutes: Int = 0
    private var monitoredSessionStart: Long = 0L
    private var pillTickerJob: Job? = null
    private var blackoutJob: Job? = null

    private var isReceiverRegistered = false
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                TemporaryUnlockManager.clearAllDelaySessions()
                stopActiveAppMonitoring()
                currentForegroundPackage = null
            }
        }
    }

    companion object {
        const val TAG = "DetachmentBlocker"
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        HeadsUpNotchPillManager.registerAccessibilityService(this)
        val db = AppDatabase.getDatabase(applicationContext, serviceScope)
        database = db

        serviceScope.launch {
            val apps = db.appLimitDao().getAllApps().firstOrNull() ?: emptyList()
            cachedAppsMap = apps.associateBy { it.packageName }
            cachedSchedules = db.scheduleRuleDao().getAllRules().firstOrNull() ?: emptyList()
            cachedDelayForDistracting = (db.appSettingsDao().getValue("key_delay_for_distracting_apps") ?: "true") != "false"
            cachedDelaySeconds = db.appSettingsDao().getValue("key_delay_seconds")?.toIntOrNull() ?: 15
            cachedIsBlackout = db.appSettingsDao().getValue("is_blackout_active") == "true"
            cachedSelfRestraintEndTime = db.appSettingsDao().getValue("self_restraint_end_time")?.toLongOrNull() ?: 0L
            cachedSelfRestraintTarget = db.appSettingsDao().getValue("self_restraint_target") ?: "DISTRACTING"
        }

        serviceScope.launch {
            db.appLimitDao().getAllApps().collect { apps ->
                cachedAppsMap = apps.associateBy { it.packageName }
            }
        }

        serviceScope.launch {
            db.scheduleRuleDao().getAllRules().collect { rules ->
                cachedSchedules = rules
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("key_delay_for_distracting_apps").collect { value ->
                cachedDelayForDistracting = (value != "false")
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("key_delay_seconds").collect { value ->
                cachedDelaySeconds = value?.toIntOrNull() ?: 15
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("is_blackout_active").collect { value ->
                cachedIsBlackout = (value == "true")
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("self_restraint_end_time").collect { value ->
                cachedSelfRestraintEndTime = value?.toLongOrNull() ?: 0L
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("self_restraint_target").collect { value ->
                cachedSelfRestraintTarget = value ?: "DISTRACTING"
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue(com.rk.detachment.data.repository.ParentalControlRepository.KEY_PARENTAL_IS_MANAGED).collect { value ->
                cachedIsParentalManaged = (value == "true")
            }
        }

        serviceScope.launch {
            db.appSettingsDao().observeValue("uninstall_protection_enabled").collect { value ->
                cachedUninstallProtectionEnabled = (value != "false")
            }
        }

        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                registerReceiver(screenStateReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
            }
        }
    }


    private fun getCachedLauncherPackages(): Set<String> {
        val now = System.currentTimeMillis()
        if (cachedLauncherPackages.isEmpty() || now - lastLauncherCacheTime > 60000) {
            cachedLauncherPackages = AppManagerHelper.getHomeLauncherPackages(applicationContext)
            lastLauncherCacheTime = now
        }
        return cachedLauncherPackages
    }

    private fun isHomeScreenLauncher(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        if (getCachedLauncherPackages().contains(packageName)) return true
        val lower = packageName.lowercase()
        return lower.contains("launcher") ||
                lower.contains("quickstep") ||
                lower.contains("trebuchet") ||
                lower.contains("nexuslauncher") ||
                lower.contains(".home") ||
                lower.contains("miui.home") ||
                lower.contains("oneplus.launcher") ||
                lower.contains("sec.android.app.launcher") ||
                lower == "bitpit.launcher"
    }

    private fun isSystemUiOrSpecial(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        val lower = packageName.lowercase()
        return lower == "com.android.systemui" ||
                lower == "android" ||
                lower.contains("inputmethod") ||
                lower.contains("latin") ||
                lower.contains("gboard") ||
                lower.contains("systemui")
    }

    private fun isExcludedOrSystem(packageName: String): Boolean {
        return AppManagerHelper.isExcludedOrSystemPackage(packageName, getCachedLauncherPackages(), applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

        if (checkSettingsTamperAttempt(event, packageName)) {
            val now = System.currentTimeMillis()
            if (now - lastInterceptTime > 600L) {
                lastInterceptTime = now
                lastInterceptedPackage = packageName
                performGlobalAction(GLOBAL_ACTION_HOME)
                try {
                    val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                    am?.killBackgroundProcesses(packageName)
                } catch (e: Exception) {}
                val targetPkg = applicationContext.packageName
                val intent = Intent(applicationContext, BlockOverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, targetPkg)
                    putExtra(BlockOverlayActivity.EXTRA_APP_NAME, "Detachment Protection")
                    putExtra(BlockOverlayActivity.EXTRA_CATEGORY, "Uninstall Protection")
                    putExtra(BlockOverlayActivity.EXTRA_REASON, "Tamper & Uninstall Protection is active. Accessing App Info, deactivating Device Admin, or removing Detachment requires Master PIN.")
                    putExtra(BlockOverlayActivity.EXTRA_IS_HARD_MODE, false)
                }
                startActivity(intent)
            }
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return
        }

        val isBlackoutActive = cachedIsBlackout || PomodoroManager.state.value.isBlackoutActive

        if (packageName == this.packageName || 
            packageName == applicationContext.packageName || 
            packageName.startsWith("com.rk.detachment")) {
            stopActiveAppMonitoring()
            return
        }

        if (isHomeScreenLauncher(packageName)) {
            val prevPkg = currentForegroundPackage
            if (prevPkg != null && !isHomeScreenLauncher(prevPkg) && !isExcludedOrSystem(prevPkg)) {
                TemporaryUnlockManager.endDelaySession(prevPkg)
            }
            currentForegroundPackage = packageName
            lastInterceptedPackage = null
            lastInterceptTime = 0L
            stopActiveAppMonitoring()
            if (isBlackoutActive) {
                PomodoroManager.setOverlayHidden(false)
            }
            return
        }

        if (isBlackoutActive) {
            if (isSystemUiOrSpecial(packageName)) {
                return
            }
            val checkApp = cachedAppsMap[packageName]
            if (checkApp != null && checkApp.isEssential) {
                PomodoroManager.setOverlayHidden(true)
                currentForegroundPackage = packageName
                startActiveAppMonitoring(checkApp)
            } else {
                PomodoroManager.setOverlayHidden(false)
                stopActiveAppMonitoring()
                val now = System.currentTimeMillis()
                if (packageName != lastInterceptedPackage || (now - lastInterceptTime) > 600L) {
                    lastInterceptedPackage = packageName
                    lastInterceptTime = now
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    try {
                        val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        am?.killBackgroundProcesses(packageName)
                    } catch (e: Exception) {}
                }
            }
            return
        }

        if (isExcludedOrSystem(packageName)) {
            stopActiveAppMonitoring()
            return
        }

        val now = System.currentTimeMillis()

        if (TemporaryUnlockManager.isUnlocked(packageName, now)) {
            val isNewLaunch = (packageName != currentForegroundPackage)
            if (isNewLaunch) {
                val prevPkg = currentForegroundPackage
                if (prevPkg != null && !isHomeScreenLauncher(prevPkg) && !isExcludedOrSystem(prevPkg)) {
                    TemporaryUnlockManager.endDelaySession(prevPkg)
                }
                stopActiveAppMonitoring()
            }
            currentForegroundPackage = packageName
            lastInterceptedPackage = null
            lastInterceptTime = 0L
            cachedAppsMap[packageName]?.let { startActiveAppMonitoring(it) }
            return
        }

        if (TemporaryUnlockManager.isDelaySessionActive(packageName)) {
            val cachedApp = cachedAppsMap[packageName]
            if (cachedApp != null && !cachedApp.isCurrentlyLocked(now)) {
                val isNewLaunch = (packageName != currentForegroundPackage)
                if (isNewLaunch) {
                    val prevPkg = currentForegroundPackage
                    if (prevPkg != null && !isHomeScreenLauncher(prevPkg) && !isExcludedOrSystem(prevPkg)) {
                        TemporaryUnlockManager.endDelaySession(prevPkg)
                    }
                    stopActiveAppMonitoring()
                }
                currentForegroundPackage = packageName
                lastInterceptedPackage = null
                lastInterceptTime = 0L
                startActiveAppMonitoring(cachedApp)
                return
            }
        }

        val isOverlayShowing = BlockOverlayActivity.isActivityResumed &&
                BlockOverlayActivity.currentActivePackage == packageName

        if (isOverlayShowing) {
            return
        }

        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 150L) {
            return
        }

        val isNewLaunch = (packageName != currentForegroundPackage)
        if (isNewLaunch) {
            val prevPkg = currentForegroundPackage
            if (prevPkg != null && !isHomeScreenLauncher(prevPkg) && !isExcludedOrSystem(prevPkg)) {
                TemporaryUnlockManager.endDelaySession(prevPkg)
            }
            stopActiveAppMonitoring()
        }
        currentForegroundPackage = packageName

        val cachedApp = cachedAppsMap[packageName]
        if (cachedApp != null) {
            if (checkAndEnforceAppSynchronously(cachedApp, now)) {
                return
            }
        }

        serviceScope.launch {
            evaluateAndEnforceApp(packageName, isNewLaunch)
        }
    }

    private fun checkAndEnforceAppSynchronously(app: AppLimitEntity, now: Long): Boolean {
        if (app.isEssential) {
            startActiveAppMonitoring(app)
            return true
        }

        if (!app.isHardMode && (TemporaryUnlockManager.isUnlocked(app.packageName, now) || app.isTemporaryUnlocked(now))) {
            if (app.isTemporaryUnlocked(now)) {
                TemporaryUnlockManager.setUnlock(app.packageName, app.unlockExpiresAtMillis)
            }
            startActiveAppMonitoring(app)
            return true
        }

        val isSelfRestraintActive = (cachedSelfRestraintEndTime > now)
        if (isSelfRestraintActive) {
            val shouldBlock = when (cachedSelfRestraintTarget) {
                "ALL_NON_ESSENTIAL" -> !app.isEssential
                else -> app.isDistracting
            }
            if (shouldBlock) {
                val remainingMins = ((cachedSelfRestraintEndTime - now) / 60000L).coerceAtLeast(1)
                val targetDesc = if (cachedSelfRestraintTarget == "ALL_NON_ESSENTIAL") "Non-essential apps" else "Distracting apps"
                interceptBlockedApp(
                    app = app,
                    reason = "Locked by Self Restraint Mode ($remainingMins min left • $targetDesc)",
                    isFrictionDelay = false,
                    delaySeconds = 15
                )
                return true
            }
        }

        val activeSchedule = cachedSchedules.firstOrNull { rule ->
            if (rule.isCurrentlyActive()) {
                when (rule.blockedTarget) {
                    "DISTRACTING" -> app.isDistracting
                    "ALL_NON_ESSENTIAL" -> !app.isEssential
                    else -> true
                }
            } else false
        }

        if (activeSchedule != null) {
            interceptBlockedApp(
                app = app,
                reason = "Locked by focus schedule '${activeSchedule.title}' (${activeSchedule.formattedTimeRange()})",
                isFrictionDelay = false,
                delaySeconds = 15
            )
            return true
        }

        if (app.isCurrentlyLocked(now)) {
            val reason = if (app.isLockedManually) {
                "Manually locked by Detachment"
            } else {
                "Daily usage limit exceeded"
            }
            interceptBlockedApp(app = app, reason = reason, isFrictionDelay = false, delaySeconds = 15)
            return true
        }

        val isShieldActive = app.isEffectiveShieldActive(cachedDelayForDistracting)
        if (isShieldActive) {
            val isGraceActive = TemporaryUnlockManager.isDelayGraceActive(app.packageName, now)
            val isSessionActive = TemporaryUnlockManager.isDelaySessionActive(app.packageName)
            if (!isGraceActive && !isSessionActive) {
                interceptBlockedApp(
                    app = app,
                    reason = "Mindful Pause: Detachment Distraction Shield Active",
                    isFrictionDelay = true,
                    delaySeconds = cachedDelaySeconds
                )
                return true
            } else if (isGraceActive && !isSessionActive) {
                TemporaryUnlockManager.setDelaySessionActive(app.packageName)
            }
        }

        startActiveAppMonitoring(app)
        return true
    }

    private suspend fun evaluateAndEnforceApp(packageName: String, isNewLaunch: Boolean) {
        val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
        if (isExcludedOrSystem(packageName)) {
            db.appLimitDao().deleteApp(packageName)
            return
        }

        val now = System.currentTimeMillis()

        val isBlackoutActive = db.appSettingsDao().getValue("is_blackout_active") == "true" || PomodoroManager.state.value.isBlackoutActive
        val checkApp = db.appLimitDao().getAppByPackage(packageName)
        if (isBlackoutActive && (checkApp == null || !checkApp.isEssential)) {
            try {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                am?.killBackgroundProcesses(packageName)
            } catch (e: Exception) {}
            performGlobalAction(GLOBAL_ACTION_HOME)
            return
        }
        val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastUsageResetDate = db.appSettingsDao().getValue("key_last_usage_reset_date")
        if (lastUsageResetDate != todayDateString) {
            db.appLimitDao().resetDailyUsage()
            val currentResisted = db.appSettingsDao().getValue("distractions_resisted")?.toIntOrNull() ?: 0
            db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted_yesterday", currentResisted.toString()))
            db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted", "0"))
            db.appSettingsDao().setSetting(AppSettingsEntity("key_last_usage_reset_date", todayDateString))
        }

        var app = db.appLimitDao().getAppByPackage(packageName)
        if (app == null) {
            try {
                val pm = packageManager
                val launchIntent = pm.getLaunchIntentForPackage(packageName)
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                if (isSystem && launchIntent == null) {
                    return
                }

                val label = pm.getApplicationLabel(appInfo).toString()
                val newEntity = AppLimitEntity(
                    packageName = packageName,
                    appName = label,
                    iconName = packageName,
                    category = AppManagerHelper.guessCategory(appInfo, packageName, label),
                    dailyLimitMinutes = 0,
                    usedTodayMinutes = AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, packageName),
                    isDistracting = false,
                    isEssential = false,
                    isLockedManually = false,
                    todayOpens = 1
                )
                db.appLimitDao().insertApp(newEntity)
                app = newEntity
            } catch (e: Exception) {
                return
            }
        }

        val hasUsage = AppManagerHelper.hasUsageStatsPermission(this@DetachmentAccessibilityService)
        val liveUsage = if (hasUsage) AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, packageName) else 0
        val currentMins = maxOf(app.usedTodayMinutes, liveUsage)
        if (currentMins != app.usedTodayMinutes) {
            db.appLimitDao().updateUsedMinutes(packageName, currentMins)
            app = app.copy(usedTodayMinutes = currentMins)
        }

        if (app.isEssential) {
            startActiveAppMonitoring(app)
            return
        }

        val isUnlocked = !app.isHardMode && (TemporaryUnlockManager.isUnlocked(packageName, now) || app.isTemporaryUnlocked(now))
        if (isUnlocked) {
            if (app.isTemporaryUnlocked(now)) {
                TemporaryUnlockManager.setUnlock(packageName, app.unlockExpiresAtMillis)
            }
            startActiveAppMonitoring(app)
            return
        }

        val selfRestraintEnd = db.appSettingsDao().getValue("self_restraint_end_time")?.toLongOrNull() ?: 0L
        val selfRestraintTarget = db.appSettingsDao().getValue("self_restraint_target") ?: "DISTRACTING"
        if (selfRestraintEnd > now) {
            val shouldBlock = when (selfRestraintTarget) {
                "ALL_NON_ESSENTIAL" -> !app.isEssential
                else -> app.isDistracting
            }
            if (shouldBlock) {
                val remainingMins = ((selfRestraintEnd - now) / 60000L).coerceAtLeast(1)
                val targetDesc = if (selfRestraintTarget == "ALL_NON_ESSENTIAL") "Non-essential apps" else "Distracting apps"
                interceptBlockedApp(
                    app = app,
                    reason = "Locked by Self Restraint Mode ($remainingMins min left • $targetDesc)",
                    isFrictionDelay = false,
                    delaySeconds = 15
                )
                return
            }
        }

        val allSchedules = db.scheduleRuleDao().getAllRules().firstOrNull() ?: emptyList()
        val activeSchedule = allSchedules.firstOrNull { rule ->
            if (rule.isCurrentlyActive()) {
                when (rule.blockedTarget) {
                    "DISTRACTING" -> app.isDistracting
                    "ALL_NON_ESSENTIAL" -> !app.isEssential
                    else -> true
                }
            } else false
        }

        if (activeSchedule != null) {
            interceptBlockedApp(
                app = app,
                reason = "Locked by focus schedule '${activeSchedule.title}' (${activeSchedule.formattedTimeRange()})",
                isFrictionDelay = false,
                delaySeconds = 15
            )
            return
        }

        if (app.isCurrentlyLocked(now)) {
            val reason = if (app.isLockedManually) {
                "Manually locked by Detachment"
            } else {
                "Daily usage limit exceeded"
            }
            interceptBlockedApp(app = app, reason = reason, isFrictionDelay = false, delaySeconds = 15)
            return
        }

        val delayForDistracting = (db.appSettingsDao().getValue("key_delay_for_distracting_apps") ?: "true") != "false"
        val isShieldActive = app.isEffectiveShieldActive(delayForDistracting)

        if (isShieldActive) {
            val isGraceActive = TemporaryUnlockManager.isDelayGraceActive(packageName, now)
            val isSessionActive = TemporaryUnlockManager.isDelaySessionActive(packageName)
            if (!isGraceActive && !isSessionActive) {
                val delaySec = db.appSettingsDao().getValue("key_delay_seconds")?.toIntOrNull() ?: 15
                interceptBlockedApp(
                    app = app,
                    reason = "Mindful Pause: Detachment Distraction Shield Active",
                    isFrictionDelay = true,
                    delaySeconds = delaySec
                )
                return
            } else if (isGraceActive && !isSessionActive) {
                TemporaryUnlockManager.setDelaySessionActive(packageName)
            }
        }

        startActiveAppMonitoring(app)
    }

    private fun startActiveAppMonitoring(app: AppLimitEntity) {
        val currentPkg = app.packageName
        if (isExcludedOrSystem(currentPkg)) {
            stopActiveAppMonitoring()
            return
        }
        if (monitoredPackage == currentPkg && pillTickerJob?.isActive == true) {
            return
        }

        stopActiveAppMonitoring()

        val displayName = if (app.appName.isNotBlank()) app.appName else {
            try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(currentPkg, 0)).toString()
            } catch (e: Exception) {
                currentPkg
            }
        }

        val hasUsage = AppManagerHelper.hasUsageStatsPermission(this)
        val usageMins = if (hasUsage) AppManagerHelper.getAppUsageMinutesToday(this, currentPkg) else 0
        val initialMinutes = maxOf(app.usedTodayMinutes, usageMins)

        monitoredPackage = currentPkg
        monitoredAppName = displayName
        monitoredBaseMinutes = initialMinutes
        monitoredSessionStart = System.currentTimeMillis()

        serviceScope.launch {
            val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
            val todayDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastUsageResetDate = db.appSettingsDao().getValue("key_last_usage_reset_date")
            if (lastUsageResetDate != todayDateString) {
                db.appLimitDao().resetDailyUsage()
                val currentResisted = db.appSettingsDao().getValue("distractions_resisted")?.toIntOrNull() ?: 0
                db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted_yesterday", currentResisted.toString()))
                db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted", "0"))
                db.appSettingsDao().setSetting(AppSettingsEntity("key_last_usage_reset_date", todayDateString))
            }

            if (initialMinutes != app.usedTodayMinutes) {
                db.appLimitDao().updateUsedMinutes(currentPkg, initialMinutes)
            }

            val isPillEnabled = (db.appSettingsDao().getValue("key_heads_up_pill_enabled") ?: "true") != "false"
            if (!isPillEnabled) return@launch

            if (initialMinutes >= 15) {
                HeadsUpNotchPillManager.syncPastMilestones(
                    context = this@DetachmentAccessibilityService,
                    packageName = currentPkg,
                    currentTotalMinutes = initialMinutes,
                    intervalMinutes = 15
                )
                HeadsUpNotchPillManager.checkAndTriggerMilestone(
                    context = this@DetachmentAccessibilityService,
                    packageName = currentPkg,
                    appName = displayName,
                    minutesUsed = initialMinutes,
                    intervalMinutes = 15,
                )
            }
        }

        pillTickerJob = serviceScope.launch {
            var loopSeconds = 0
            while (monitoredPackage == currentPkg) {
                delay(1000L)
                if (monitoredPackage != currentPkg) break
                loopSeconds++

                val now = System.currentTimeMillis()
                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)

                val shouldUpdateUsage = (loopSeconds % 5 == 0)
                var currentDbApp = cachedAppsMap[currentPkg] ?: app
                val elapsedMins = ((now - monitoredSessionStart) / 60000L).toInt()
                val currentDbMinutes = currentDbApp.usedTodayMinutes
                val totalMins = maxOf(currentDbMinutes, monitoredBaseMinutes + elapsedMins)

                if (shouldUpdateUsage) {
                    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val lastUsageResetDate = db.appSettingsDao().getValue("key_last_usage_reset_date")
                    if (lastUsageResetDate != todayDate) {
                        db.appLimitDao().resetDailyUsage()
                        val currentResisted = db.appSettingsDao().getValue("distractions_resisted")?.toIntOrNull() ?: 0
                        db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted_yesterday", currentResisted.toString()))
                        db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted", "0"))
                        db.appSettingsDao().setSetting(AppSettingsEntity("key_last_usage_reset_date", todayDate))
                        monitoredBaseMinutes = 0
                        monitoredSessionStart = now
                    }

                    val hasUsage = AppManagerHelper.hasUsageStatsPermission(this@DetachmentAccessibilityService)
                    val liveUsage = if (hasUsage) AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, currentPkg) else 0
                    val finalMins = maxOf(totalMins, liveUsage)

                    if (finalMins != currentDbMinutes) {
                        db.appLimitDao().updateUsedMinutes(currentPkg, finalMins)
                        currentDbApp = currentDbApp.copy(usedTodayMinutes = finalMins)
                    }

                    val isPillEnabled = (db.appSettingsDao().getValue("key_heads_up_pill_enabled") ?: "true") != "false"
                    if (isPillEnabled && monitoredPackage == currentPkg) {
                        HeadsUpNotchPillManager.checkAndTriggerMilestone(
                            context = this@DetachmentAccessibilityService,
                            packageName = currentPkg,
                            appName = displayName,
                            minutesUsed = finalMins,
                            intervalMinutes = 15,
                        )
                    }
                }

                val isBlackoutActive = cachedIsBlackout || PomodoroManager.state.value.isBlackoutActive
                if (isBlackoutActive && !currentDbApp.isEssential) {
                    stopActiveAppMonitoring()
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    break
                }

                val isUnlocked = TemporaryUnlockManager.isUnlocked(currentPkg, now) || currentDbApp.isTemporaryUnlocked(now)
                if (!isUnlocked) {
                    val limitMinutes = currentDbApp.dailyLimitMinutes
                    val isLockedManually = currentDbApp.isLockedManually

                    if (isLockedManually) {
                        val updatedApp = currentDbApp.copy(
                            usedTodayMinutes = totalMins,
                            dailyLimitMinutes = limitMinutes,
                            isLockedManually = true
                        )
                        interceptBlockedApp(
                            app = updatedApp,
                            reason = "Manually locked by Detachment Shield",
                            isFrictionDelay = false,
                            delaySeconds = 15
                        )
                        break
                    }

                    if (limitMinutes > 0 && totalMins >= limitMinutes) {
                        val updatedApp = currentDbApp.copy(
                            usedTodayMinutes = totalMins,
                            dailyLimitMinutes = limitMinutes,
                            isLockedManually = isLockedManually
                        )
                        interceptBlockedApp(
                            app = updatedApp,
                            reason = "Daily usage limit exceeded.",
                            isFrictionDelay = false,
                            delaySeconds = 15
                        )
                        break
                    }

                    val allSchedules = cachedSchedules
                    val activeSchedule = allSchedules.firstOrNull { rule ->
                        if (rule.isCurrentlyActive()) {
                            when (rule.blockedTarget) {
                                "DISTRACTING" -> currentDbApp.isDistracting
                                "ALL_NON_ESSENTIAL" -> !currentDbApp.isEssential
                                else -> true
                            }
                        } else false
                    }

                    if (activeSchedule != null) {
                        val updatedApp = currentDbApp.copy(usedTodayMinutes = totalMins)
                        interceptBlockedApp(
                            app = updatedApp,
                            reason = "Locked by focus schedule '${activeSchedule.title}' (${activeSchedule.formattedTimeRange()})",
                            isFrictionDelay = false,
                            delaySeconds = 15
                        )
                        break
                    }
                }
            }
        }
    }

    private fun stopActiveAppMonitoring() {
        val pkg = monitoredPackage
        val sessionStart = monitoredSessionStart
        val baseMins = monitoredBaseMinutes

        pillTickerJob?.cancel()
        pillTickerJob = null
        monitoredPackage = null
        monitoredAppName = null
        monitoredSessionStart = 0L

        if (pkg != null && sessionStart > 0L) {
            val elapsedMins = ((System.currentTimeMillis() - sessionStart) / 60000L).toInt()
            val hasUsage = AppManagerHelper.hasUsageStatsPermission(this@DetachmentAccessibilityService)
            val liveUsage = if (hasUsage) AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, pkg) else 0
            serviceScope.launch {
                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
                val currentDbMinutes = db.appLimitDao().getAppByPackage(pkg)?.usedTodayMinutes ?: 0
                val totalMins = maxOf(currentDbMinutes, baseMins + elapsedMins, liveUsage)
                if (totalMins != currentDbMinutes) {
                    db.appLimitDao().updateUsedMinutes(pkg, totalMins)
                }
            }
        }
    }

    private fun interceptBlockedApp(
        app: AppLimitEntity,
        reason: String,
        isFrictionDelay: Boolean,
        delaySeconds: Int = 15
    ) {
        if (isExcludedOrSystem(app.packageName)) {
            return
        }
        stopActiveAppMonitoring()
        
        val now = System.currentTimeMillis()
        val isOverlayActiveOnTarget = BlockOverlayActivity.isActivityResumed &&
                BlockOverlayActivity.currentActivePackage == app.packageName
        
        if (isOverlayActiveOnTarget) {
            return
        }

        if (app.packageName == lastInterceptedPackage && (now - lastInterceptTime) < 150L) {
            return
        }
        
        lastInterceptedPackage = app.packageName
        lastInterceptTime = now

        if (!isFrictionDelay) {
            try {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                am?.killBackgroundProcesses(app.packageName)
            } catch (e: Exception) {}
        }
        
        try {
            val intent = Intent(this, BlockOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, app.packageName)
                putExtra(BlockOverlayActivity.EXTRA_APP_NAME, app.appName)
                putExtra(BlockOverlayActivity.EXTRA_CATEGORY, app.category)
                putExtra(BlockOverlayActivity.EXTRA_REASON, reason)
                putExtra(BlockOverlayActivity.EXTRA_IS_FRICTION_DELAY, isFrictionDelay)
                putExtra(BlockOverlayActivity.EXTRA_DELAY_SECONDS, delaySeconds)
                putExtra(BlockOverlayActivity.EXTRA_USED_MINUTES, app.usedTodayMinutes)
                putExtra(BlockOverlayActivity.EXTRA_LIMIT_MINUTES, app.dailyLimitMinutes)
                putExtra(BlockOverlayActivity.EXTRA_IS_HARD_MODE, app.isHardMode)
            }
            val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
            startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch BlockOverlayActivity", e)
        }
    }

    private fun checkSettingsTamperAttempt(event: AccessibilityEvent, packageName: String): Boolean {
        if (!cachedIsParentalManaged && !cachedUninstallProtectionEnabled) return false
        if (!com.rk.detachment.util.UninstallProtectionManager.isDeviceAdminActive(applicationContext)) return false
        val pkgLower = packageName.lowercase()
        val isSettingsOrInstaller = pkgLower == "com.android.settings" ||
                pkgLower == "com.google.android.settings" ||
                pkgLower == "com.google.android.packageinstaller" ||
                pkgLower == "com.android.packageinstaller" ||
                pkgLower == "com.samsung.android.packageinstaller" ||
                pkgLower == "com.android.vending" ||
                pkgLower.contains(".settings") ||
                pkgLower.contains("packageinstaller") ||
                pkgLower.contains("safecenter") ||
                pkgLower.contains("permissioncontroller")
        if (!isSettingsOrInstaller) return false

        val myPkg = applicationContext.packageName.lowercase()
        val now = System.currentTimeMillis()
        if (TemporaryUnlockManager.isUnlocked(myPkg, now) ||
            TemporaryUnlockManager.isUnlocked("com.rk.detachment", now) ||
            TemporaryUnlockManager.isUnlocked("com.rk.detachment.parental", now) ||
            TemporaryUnlockManager.isUnlocked("com.android.settings", now) ||
            TemporaryUnlockManager.isUnlocked(packageName, now)) {
            return false
        }

        val eventTexts = event.text.joinToString(" ").lowercase()
        val contentDesc = event.contentDescription?.toString()?.lowercase() ?: ""
        val className = event.className?.toString()?.lowercase() ?: ""
        val immediateText = "$eventTexts $contentDesc"

        val targetKeywords = listOf("detachment", myPkg, "com.rk.detachment", "com.rk.detachment.parental")
        val matchesOurAppImmediate = targetKeywords.any { immediateText.contains(it) }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (matchesOurAppImmediate) return true
            val sourceNode = try { event.source } catch (e: Exception) { null }
            if (scanNodeForTamper(sourceNode, targetKeywords)) return true
        }

        val rootNode = try { rootInActiveWindow } catch (e: Exception) { null }
        val eventNode = try { event.source } catch (e: Exception) { null }
        val allTokens = mutableListOf<String>()
        extractTextsFromNode(rootNode, allTokens)
        if (allTokens.isEmpty() && eventNode != null) {
            extractTextsFromNode(eventNode, allTokens)
        }
        val fullScreenText = (allTokens.joinToString(" ") + " " + immediateText).lowercase()

        val matchesOurApp = targetKeywords.any { fullScreenText.contains(it) }

        if (pkgLower.contains("packageinstaller")) {
            if (matchesOurApp || fullScreenText.contains("uninstall") || fullScreenText.contains("remove") || fullScreenText.contains("delete")) {
                if (matchesOurApp) return true
            }
        }

        if (pkgLower == "com.android.vending") {
            if (matchesOurApp && (fullScreenText.contains("uninstall") || fullScreenText.contains("remove"))) {
                return true
            }
        }

        val isDeviceAdminScreen = className.contains("deviceadmin") ||
                fullScreenText.contains("device admin") ||
                fullScreenText.contains("device administrator") ||
                fullScreenText.contains("special app access")
        if (isDeviceAdminScreen && (matchesOurApp || fullScreenText.contains("deactivate"))) {
            if (matchesOurApp) return true
        }

        val isAppInfoScreen = className.contains("installedappdetails") ||
                className.contains("appinfo") ||
                className.contains("applicationdetail") ||
                className.contains("appbuttonspreferencecontroller") ||
                fullScreenText.contains("app info") ||
                fullScreenText.contains("force stop") ||
                fullScreenText.contains("storage & cache") ||
                fullScreenText.contains("storage and cache") ||
                fullScreenText.contains("clear storage") ||
                fullScreenText.contains("clear data") ||
                fullScreenText.contains("uninstall")
        if (isAppInfoScreen && matchesOurApp) {
            return true
        }

        val isAccessibilityScreen = className.contains("accessibility") ||
                fullScreenText.contains("accessibility")
        if (isAccessibilityScreen && matchesOurApp) {
            return true
        }

        return false
    }

    private fun scanNodeForTamper(node: AccessibilityNodeInfo?, targetKeywords: List<String>): Boolean {
        if (node == null) return false
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.toString()?.lowercase() ?: ""
        val combined = "$text $desc $viewId"
        if (targetKeywords.any { combined.contains(it) }) {
            return true
        }
        val count = node.childCount
        for (i in 0 until count) {
            val child = try { node.getChild(i) } catch (e: Exception) { null }
            if (child != null) {
                val matched = scanNodeForTamper(child, targetKeywords)
                try { child.recycle() } catch (e: Exception) {}
                if (matched) return true
            }
        }
        return false
    }

    private fun extractTextsFromNode(
        node: AccessibilityNodeInfo?,
        out: MutableList<String>,
        depth: Int = 0,
        maxDepth: Int = 8,
        maxCount: Int = 80
    ) {
        if (node == null || depth > maxDepth || out.size >= maxCount) return
        try {
            val text = node.text?.toString()?.trim()
            if (!text.isNullOrEmpty()) {
                out.add(text)
            }
            val desc = node.contentDescription?.toString()?.trim()
            if (!desc.isNullOrEmpty()) {
                out.add(desc)
            }
            val count = node.childCount
            for (i in 0 until count) {
                if (out.size >= maxCount) break
                val child = try { node.getChild(i) } catch (e: Exception) { null }
                if (child != null) {
                    extractTextsFromNode(child, out, depth + 1, maxDepth, maxCount)
                    try { child.recycle() } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(screenStateReceiver)
            } catch (e: Exception) {
            }
            isReceiverRegistered = false
        }
        stopActiveAppMonitoring()
        HeadsUpNotchPillManager.unregisterAccessibilityService(this)
        HeadsUpNotchPillManager.dismissPill()
        isServiceRunning = false
    }
}
