package com.rk.detachment.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.ui.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DetachmentAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: AppDatabase? = null

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
        Log.i(TAG, "DetachmentAccessibilityService connected and actively protecting.")
    }

    private fun isLauncherOrSystem(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return packageName == this.packageName ||
                packageName == applicationContext.packageName ||
                lower.contains("detachment") ||
                packageName == "com.android.systemui" ||
                lower.contains("launcher") ||
                lower.contains("quickstep") ||
                lower.contains("trebuchet") ||
                lower.contains("nexuslauncher") ||
                lower.contains("inputmethod") ||
                lower.contains("recents") ||
                lower.contains(".home")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

        if (isLauncherOrSystem(packageName)) {
            lastInterceptedPackage = null
            lastInterceptTime = 0L
            return
        }

        val now = System.currentTimeMillis()
        if (packageName == lastInterceptedPackage && (now - lastInterceptTime) < 800L) {
            return
        }

        serviceScope.launch {
            evaluateAndEnforceApp(packageName)
        }
    }

    private suspend fun evaluateAndEnforceApp(packageName: String) {
        val db = database ?: AppDatabase.getDatabase(applicationContext, serviceScope)
        val app = db.appLimitDao().getAppByPackage(packageName) ?: return

        if (app.isTemporaryUnlocked()) {
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

        if (app.isCurrentlyLocked()) {
            val reason = if (app.isLockedManually) {
                "Manually locked by Detachment Shield"
            } else {
                "Daily screen time limit of ${app.dailyLimitMinutes}m exceeded (${app.usedTodayMinutes}m used today)"
            }
            interceptBlockedApp(app = app, reason = reason, isFrictionDelay = false, delaySeconds = 15)
            return
        }

        if (app.isShieldActive) {
            val lastPassed = app.unlockExpiresAtMillis
            val now = System.currentTimeMillis()
            if (lastPassed < now) {
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
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
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
        Log.w(TAG, "DetachmentAccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}

