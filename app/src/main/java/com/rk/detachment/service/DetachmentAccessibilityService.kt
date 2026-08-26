package com.rk.detachment.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.ui.BlockOverlayActivity
import com.rk.detachment.util.AppManagerHelper
import com.rk.detachment.util.TemporaryUnlockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetachmentAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: AppDatabase? = null

    private var currentForegroundPackage: String? = null
    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0L

    companion object {
        const val TAG = "DetachmentBlocker"
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        database = AppDatabase.getDatabase(applicationContext, serviceScope)
    }

    private fun isLauncherOrSystem(packageName: String): Boolean {
        val launcherPackages = AppManagerHelper.getHomeLauncherPackages(applicationContext)
        return AppManagerHelper.isLauncherOrSystemPackage(packageName, launcherPackages)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

        // 1. DETACHMENT APP ITSELF (MainActivity or BlockOverlayActivity):
        // Never block our own app and never dismiss overlay from here
        if (packageName == this.packageName || 
            packageName == applicationContext.packageName || 
            packageName.startsWith("com.rk.detachment")) {
            return
        }

        // 2. LAUNCHER OR SYSTEM UI (Notifications, Quick Settings, Recents, Keyguard):
        if (isLauncherOrSystem(packageName)) {
            currentForegroundPackage = packageName
            lastInterceptedPackage = null
            lastInterceptTime = 0L
            return
        }

        val now = System.currentTimeMillis()

        // 3. TARGET APP - INSTANT IN-MEMORY UNLOCK CHECK:
        if (TemporaryUnlockManager.isUnlocked(packageName, now)) {
            currentForegroundPackage = packageName
            lastInterceptedPackage = null
            lastInterceptTime = 0L
            return
        }

        // 4. If overlay is ALREADY visibly active for this exact package, prevent re-triggering
        if (BlockOverlayActivity.currentActivePackage == packageName && BlockOverlayActivity.activeInstance != null) {
            return
        }

        // 5. Debounce rapid repeat events for the same blocked package (e.g. splash screen transition)
        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 2500L) {
            return
        }

        val isNewLaunch = (packageName != currentForegroundPackage)
        currentForegroundPackage = packageName

        serviceScope.launch {
            evaluateAndEnforceApp(packageName, isNewLaunch)
        }
    }

    private suspend fun evaluateAndEnforceApp(packageName: String, isNewLaunch: Boolean) {
        val now = System.currentTimeMillis()
        if (TemporaryUnlockManager.isUnlocked(packageName, now)) {
            return
        }

        val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
        val app = db.appLimitDao().getAppByPackage(packageName) ?: return

        if (app.isTemporaryUnlocked(now)) {
            TemporaryUnlockManager.setUnlock(packageName, app.unlockExpiresAtMillis)
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

        if (app.isShieldActive) {
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

    private fun interceptBlockedApp(
        app: AppLimitEntity,
        reason: String,
        isFrictionDelay: Boolean,
        delaySeconds: Int = 15
    ) {
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
        isServiceRunning = false
    }
}


