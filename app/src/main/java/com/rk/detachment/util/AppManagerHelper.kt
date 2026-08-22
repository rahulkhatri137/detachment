package com.rk.detachment.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.collection.LruCache
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.service.DetachmentAccessibilityService
import java.util.Calendar

object AppManagerHelper {

    private val iconCache = LruCache<String, Bitmap>(200)

    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        return try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            val myServiceName = "${context.packageName}/${DetachmentAccessibilityService::class.java.name}"
            enabledServices.any { service ->
                val id = service.id
                id.equals(myServiceName, ignoreCase = true) || id.contains(context.packageName)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openOverlaySettings(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun launchRealApp(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Queries real installed apps with launcher activities, categorizes them,
     * and joins with today's real foreground usage stats from UsageStatsManager.
     */
    fun scanRealInstalledApps(
        context: Context,
        existingEntities: List<AppLimitEntity>
    ): List<AppLimitEntity> {
        val pm = context.packageManager
        val existingMap = existingEntities.associateBy { it.packageName }
        val usageMap = getTodayUsageMinutesMap(context)

        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val seenPackages = mutableSetOf<String>()
        val resultList = mutableListOf<AppLimitEntity>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue
            if (seenPackages.contains(pkg)) continue
            seenPackages.add(pkg)

            val appLabel = resolveInfo.loadLabel(pm).toString().takeIf { it.isNotBlank() } ?: pkg
            val realMinutesToday = usageMap[pkg] ?: 0
            val existing = existingMap[pkg]

            if (existing != null) {
                resultList.add(
                    existing.copy(
                        appName = appLabel,
                        usedTodayMinutes = realMinutesToday
                    )
                )
            } else {
                val category = guessCategory(resolveInfo.activityInfo.applicationInfo, pkg, appLabel)

                resultList.add(
                    AppLimitEntity(
                        packageName = pkg,
                        appName = appLabel,
                        iconName = pkg,
                        category = category,
                        dailyLimitMinutes = 0,
                        usedTodayMinutes = realMinutesToday,
                        isDistracting = false,
                        isEssential = false,
                        isLockedManually = false,
                        todayOpens = 0
                    )
                )
            }
        }

        return resultList.sortedWith(
            compareByDescending<AppLimitEntity> { it.usedTodayMinutes }
                .thenBy { it.appName.lowercase() }
        )
    }

    /**
     * Queries UsageStatsManager for today's precise interactive foreground screen time from 00:00:00 to now.
     * Uses UsageEvents (ACTIVITY_RESUMED / ACTIVITY_PAUSED / SCREEN_NON_INTERACTIVE) to strictly match
     * Android Digital Wellbeing's calculation and completely eliminate inflated background service time.
     */
    fun getTodayUsageMinutesMap(context: Context): Map<String, Int> {
        val usageMap = mutableMapOf<String, Int>()
        if (!hasUsageStatsPermission(context)) {
            return usageMap
        }

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return usageMap

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            val eventUsageMillis = mutableMapOf<String, Long>()
            var currentInteractiveForeground: String? = null
            var currentForegroundStart: Long = 0L

            val event = UsageEvents.Event()
            var eventCount = 0

            while (usageEvents != null && usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                eventCount++
                val pkg = event.packageName ?: continue
                val time = event.timeStamp

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (currentInteractiveForeground != null && currentForegroundStart > 0L) {
                            val duration = (time - currentForegroundStart).coerceAtLeast(0L)
                            eventUsageMillis[currentInteractiveForeground!!] =
                                (eventUsageMillis[currentInteractiveForeground!!] ?: 0L) + duration
                        }
                        currentInteractiveForeground = pkg
                        currentForegroundStart = time
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (currentInteractiveForeground == pkg && currentForegroundStart > 0L) {
                            val duration = (time - currentForegroundStart).coerceAtLeast(0L)
                            eventUsageMillis[pkg] = (eventUsageMillis[pkg] ?: 0L) + duration
                            currentInteractiveForeground = null
                            currentForegroundStart = 0L
                        }
                    }
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE,
                    UsageEvents.Event.KEYGUARD_SHOWN,
                    UsageEvents.Event.DEVICE_SHUTDOWN -> {
                        if (currentInteractiveForeground != null && currentForegroundStart > 0L) {
                            val duration = (time - currentForegroundStart).coerceAtLeast(0L)
                            eventUsageMillis[currentInteractiveForeground!!] =
                                (eventUsageMillis[currentInteractiveForeground!!] ?: 0L) + duration
                            currentInteractiveForeground = null
                            currentForegroundStart = 0L
                        }
                    }
                }
            }

            // Close any currently running foreground app session up to endTime
            if (currentInteractiveForeground != null && currentForegroundStart > 0L) {
                val duration = (endTime - currentForegroundStart).coerceIn(0L, 12 * 3600 * 1000L)
                eventUsageMillis[currentInteractiveForeground!!] =
                    (eventUsageMillis[currentInteractiveForeground!!] ?: 0L) + duration
            }

            if (eventCount > 0 && eventUsageMillis.isNotEmpty()) {
                for ((pkg, ms) in eventUsageMillis) {
                    val mins = (ms / (1000 * 60)).toInt()
                    if (mins > 0) {
                        usageMap[pkg] = mins
                    }
                }
                return usageMap
            }

            val aggregateStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            if (!aggregateStats.isNullOrEmpty()) {
                for ((pkg, stat) in aggregateStats) {
                    if (stat.lastTimeUsed >= startTime) {
                        val totalMs = stat.totalTimeInForeground
                        val mins = (totalMs / (1000 * 60)).toInt()
                        if (mins > 0) {
                            usageMap[pkg] = mins
                        }
                    }
                }
                if (usageMap.isNotEmpty()) {
                    return usageMap
                }
            }

            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                startTime,
                endTime
            )
            if (stats != null) {
                for (stat in stats) {
                    if (stat.lastTimeUsed >= startTime) {
                        val totalMs = stat.totalTimeInForeground
                        val mins = (totalMs / (1000 * 60)).toInt()
                        val current = usageMap[stat.packageName] ?: 0
                        usageMap[stat.packageName] = maxOf(current, mins)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fallback if query fails
        }

        return usageMap
    }

    fun getAppIconBitmapFromMemory(packageName: String): Bitmap? {
        return iconCache.get(packageName)
    }

    fun preloadIcons(context: Context, packageNames: List<String>) {
        for (pkg in packageNames) {
            if (iconCache.get(pkg) == null) {
                getAppIconBitmap(context, pkg)
            }
        }
    }

    fun getAppIconBitmap(context: Context, packageName: String): Bitmap? {
        val cached = iconCache.get(packageName)
        if (cached != null) return cached

        return try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable)
            if (bitmap != null) {
                iconCache.put(packageName, bitmap)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width.coerceAtMost(192), height.coerceAtMost(192), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun guessCategory(appInfo: ApplicationInfo?, packageName: String, label: String): String {
        val pkg = packageName.lowercase()
        val lbl = label.lowercase()

        if (pkg.contains("instagram") || pkg.contains("tiktok") || pkg.contains("twitter") ||
            pkg.contains("facebook") || pkg.contains("reddit") || pkg.contains("snapchat") ||
            pkg.contains("threads") || pkg.contains("social") || pkg.contains("discord") ||
            pkg.contains("linkedin") || pkg.contains("pinterest")
        ) return "Social"

        if (pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("prime") ||
            pkg.contains("disney") || pkg.contains("hulu") || pkg.contains("twitch") ||
            pkg.contains("vimeo") || pkg.contains("hotstar") || pkg.contains("video")
        ) return "Video"

        if (pkg.contains("spotify") || pkg.contains("music") || pkg.contains("podcast") ||
            pkg.contains("soundcloud") || pkg.contains("audio") || pkg.contains("shazam")
        ) return "Audio"

        if (pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") ||
            pkg.contains("messenger") || pkg.contains("messaging") || pkg.contains("dialer") ||
            pkg.contains("phone") || pkg.contains("contacts")
        ) return "Communication"

        if (pkg.contains("game") || pkg.contains("clash") || pkg.contains("candy") ||
            pkg.contains("roblox") || pkg.contains("pubg") || pkg.contains("chess") ||
            pkg.contains("subway") || pkg.contains("supercell")
        ) return "Games"

        if (pkg.contains("chrome") || pkg.contains("browser") || pkg.contains("firefox") ||
            pkg.contains("settings") || pkg.contains("calculator") || pkg.contains("camera") ||
            pkg.contains("gallery") || pkg.contains("photos") || pkg.contains("clock")
        ) return "Utilities"

        if (pkg.contains("maps") || pkg.contains("uber") || pkg.contains("waze") ||
            pkg.contains("navigation") || pkg.contains("travel")
        ) return "Navigation"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && appInfo != null) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_GAME -> return "Games"
                ApplicationInfo.CATEGORY_AUDIO -> return "Audio"
                ApplicationInfo.CATEGORY_VIDEO -> return "Video"
                ApplicationInfo.CATEGORY_IMAGE -> return "Photos"
                ApplicationInfo.CATEGORY_SOCIAL -> return "Social"
                ApplicationInfo.CATEGORY_NEWS -> return "News"
                ApplicationInfo.CATEGORY_MAPS -> return "Navigation"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "Productivity"
            }
        }

        return "Productivity"
    }
}
