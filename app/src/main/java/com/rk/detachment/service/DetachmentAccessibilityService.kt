package com.rk.detachment.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.ui.BlockOverlayActivity
import com.rk.detachment.util.AppManagerHelper
import com.rk.detachment.util.HeadsUpNotchPillManager
import com.rk.detachment.util.TemporaryUnlockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetachmentAccessibilityService : AccessibilityService() {

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
        database = AppDatabase.getDatabase(applicationContext, serviceScope)

        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                registerReceiver(screenStateReceiver, filter)
                isReceiverRegistered = true
            } catch (e: Exception) {
            }
        }
    }

    private fun isHomeScreenLauncher(packageName: String): Boolean {
        val launcherPackages = AppManagerHelper.getHomeLauncherPackages(applicationContext)
        return AppManagerHelper.isHomeScreenLauncher(packageName, launcherPackages)
    }

    private fun isExcludedOrSystem(packageName: String): Boolean {
        val launcherPackages = AppManagerHelper.getHomeLauncherPackages(applicationContext)
        return AppManagerHelper.isExcludedOrSystemPackage(packageName, launcherPackages, applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

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
            serviceScope.launch {
                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
                val app = db.appLimitDao().getAppByPackage(packageName) ?: return@launch
                startActiveAppMonitoring(app)
            }
            return
        }

        if (TemporaryUnlockManager.isDelaySessionActive(packageName)) {
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
            serviceScope.launch {
                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
                val app = db.appLimitDao().getAppByPackage(packageName) ?: return@launch
                if (app.isCurrentlyLocked(now)) {
                    evaluateAndEnforceApp(packageName, isNewLaunch = false)
                } else {
                    startActiveAppMonitoring(app)
                }
            }
            return
        }

        if (BlockOverlayActivity.currentActivePackage == packageName && BlockOverlayActivity.activeInstance != null) {
            return
        }

        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 2500L) {
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

        serviceScope.launch {
            evaluateAndEnforceApp(packageName, isNewLaunch)
        }
    }

    private suspend fun evaluateAndEnforceApp(packageName: String, isNewLaunch: Boolean) {
        if (isExcludedOrSystem(packageName)) {
            val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
            db.appLimitDao().deleteApp(packageName)
            return
        }

        val now = System.currentTimeMillis()
        if (TemporaryUnlockManager.isUnlocked(packageName, now)) {
            return
        }

        val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
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

        if (app.isTemporaryUnlocked(now)) {
            TemporaryUnlockManager.setUnlock(packageName, app.unlockExpiresAtMillis)
            startActiveAppMonitoring(app)
            return
        }

        val isBlackoutActive = db.appSettingsDao().getValue("is_blackout_active") == "true"
        if (isBlackoutActive && !app.isEssential) {
            interceptBlockedApp(
                app = app,
                reason = "Blocked by active Detachment Blackout. Only essential apps permitted.",
                isFrictionDelay = false,
                delaySeconds = 15
            )
            return
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
                "Manually locked by Detachment Shield"
            } else {
                "Daily screen time limit of ${app.dailyLimitMinutes}m exceeded (${app.usedTodayMinutes}m used today)"
            }
            interceptBlockedApp(app = app, reason = reason, isFrictionDelay = false, delaySeconds = 15)
            return
        }

        val delayForDistracting = (db.appSettingsDao().getValue("key_delay_for_distracting_apps") ?: "true") != "false"
        val isDelayActive = app.isShieldActive || (delayForDistracting && app.isDistracting)

        if (isDelayActive) {
            if (isNewLaunch && !TemporaryUnlockManager.isDelaySessionActive(packageName)) {
                val delaySec = db.appSettingsDao().getValue("key_delay_seconds")?.toIntOrNull() ?: 15
                interceptBlockedApp(
                    app = app,
                    reason = "Mindful Pause: Detachment Distraction Shield Active",
                    isFrictionDelay = true,
                    delaySeconds = delaySec
                )
                return
            }
        }

        startActiveAppMonitoring(app)
    }

    private fun startActiveAppMonitoring(app: AppLimitEntity) {
        val currentPkg = app.packageName
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

        val usageMins = AppManagerHelper.getAppUsageMinutesToday(this, currentPkg)
        val initialMinutes = maxOf(usageMins, app.usedTodayMinutes)

        monitoredPackage = currentPkg
        monitoredAppName = displayName
        monitoredBaseMinutes = initialMinutes
        monitoredSessionStart = System.currentTimeMillis()

        serviceScope.launch {
            val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
            if (initialMinutes > app.usedTodayMinutes) {
                db.appLimitDao().updateUsedMinutes(currentPkg, initialMinutes)
            }

            val isPillEnabled = (db.appSettingsDao().getValue("key_heads_up_pill_enabled") ?: "true") != "false"
            if (!isPillEnabled) return@launch

            if (initialMinutes >= 15) {
                HeadsUpNotchPillManager.checkAndTriggerMilestone(
                    context = this@DetachmentAccessibilityService,
                    packageName = currentPkg,
                    appName = displayName,
                    minutesUsed = initialMinutes,
                    intervalMinutes = 15
                )
            }
        }

        pillTickerJob = serviceScope.launch {
            while (monitoredPackage == currentPkg) {
                delay(10000L)
                if (monitoredPackage != currentPkg) break

                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
                val isPillEnabled = (db.appSettingsDao().getValue("key_heads_up_pill_enabled") ?: "true") != "false"

                val elapsedMins = ((System.currentTimeMillis() - monitoredSessionStart) / 60000L).toInt()
                val liveUsage = AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, currentPkg)
                val totalMins = maxOf(liveUsage, monitoredBaseMinutes + elapsedMins)

                val currentDbMinutes = db.appLimitDao().getAppByPackage(currentPkg)?.usedTodayMinutes ?: 0
                if (totalMins > currentDbMinutes) {
                    db.appLimitDao().updateUsedMinutes(currentPkg, totalMins)
                }

                if (isPillEnabled && monitoredPackage == currentPkg) {
                    HeadsUpNotchPillManager.checkAndTriggerMilestone(
                        context = this@DetachmentAccessibilityService,
                        packageName = currentPkg,
                        appName = displayName,
                        minutesUsed = totalMins,
                        intervalMinutes = 15
                    )
                }

                if (app.dailyLimitMinutes > 0 && totalMins >= app.dailyLimitMinutes) {
                    val now = System.currentTimeMillis()
                    if (!TemporaryUnlockManager.isUnlocked(currentPkg, now)) {
                        evaluateAndEnforceApp(currentPkg, isNewLaunch = false)
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
            val liveUsage = AppManagerHelper.getAppUsageMinutesToday(this@DetachmentAccessibilityService, pkg)
            val totalMins = maxOf(liveUsage, baseMins + elapsedMins)
            serviceScope.launch {
                val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
                val currentDbMinutes = db.appLimitDao().getAppByPackage(pkg)?.usedTodayMinutes ?: 0
                if (totalMins > currentDbMinutes) {
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
        lastInterceptedPackage = app.packageName
        lastInterceptTime = System.currentTimeMillis()

        try {
            val intent = Intent(this, BlockOverlayActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, app.packageName)
                putExtra(BlockOverlayActivity.EXTRA_APP_NAME, app.appName)
                putExtra(BlockOverlayActivity.EXTRA_CATEGORY, app.category)
                putExtra(BlockOverlayActivity.EXTRA_REASON, reason)
                putExtra(BlockOverlayActivity.EXTRA_IS_FRICTION_DELAY, isFrictionDelay)
                putExtra(BlockOverlayActivity.EXTRA_DELAY_SECONDS, delaySeconds)
                putExtra(BlockOverlayActivity.EXTRA_USED_MINUTES, app.usedTodayMinutes)
                putExtra(BlockOverlayActivity.EXTRA_LIMIT_MINUTES, app.dailyLimitMinutes)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch BlockOverlayActivity", e)
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
        HeadsUpNotchPillManager.dismissPill()
        isServiceRunning = false
    }
}
