package com.rk.detachment.util

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HeadsUpPillData(
    val packageName: String,
    val appName: String,
    val minutesUsed: Int
)

object HeadsUpNotchPillManager {

    private const val TAG = "HeadsUpNotchPill"
    private const val NOTIFICATION_CHANNEL_ID = "screen_time_pills_channel"
    private const val PREFS_NAME = "detachment_pill_milestones_prefs"
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _currentPillState = MutableStateFlow<HeadsUpPillData?>(null)
    val currentPillState: StateFlow<HeadsUpPillData?> = _currentPillState.asStateFlow()

    private val alertedMilestones = mutableMapOf<String, MutableSet<Int>>()

    private var activeOverlayView: View? = null
    private var activeWindowManager: WindowManager? = null
    private var dismissRunnable: Runnable? = null

    private fun getTodayKey(packageName: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val dateString = dateFormat.format(Date())
        return "${dateString}_$packageName"
    }

    private fun getAlertedSet(context: Context, todayKey: String): MutableSet<Int> {
        val memorySet = alertedMilestones[todayKey]
        if (memorySet != null) return memorySet

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(todayKey, null)
        val set = mutableSetOf<Int>()
        if (saved != null) {
            for (str in saved) {
                str.toIntOrNull()?.let { set.add(it) }
            }
        }
        alertedMilestones[todayKey] = set
        return set
    }

    private fun saveAlertedSet(context: Context, todayKey: String, set: Set<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringSet = set.map { it.toString() }.toSet()
        prefs.edit().putStringSet(todayKey, stringSet).apply()
    }

    fun syncPastMilestones(
        context: Context,
        packageName: String,
        currentTotalMinutes: Int,
        intervalMinutes: Int = 15
    ) {
        if (currentTotalMinutes < intervalMinutes) return
        val todayKey = getTodayKey(packageName)
        val set = getAlertedSet(context, todayKey)

        val highestPastMilestone = (currentTotalMinutes / intervalMinutes) * intervalMinutes
        var changed = false
        var m = intervalMinutes
        while (m < highestPastMilestone) {
            if (set.add(m)) {
                changed = true
            }
            m += intervalMinutes
        }
        if (changed) {
            saveAlertedSet(context, todayKey, set)
        }
    }

    fun checkAndTriggerMilestone(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int,
        intervalMinutes: Int = 15
    ): Boolean {
        if (minutesUsed < intervalMinutes) return false

        val milestone = (minutesUsed / intervalMinutes) * intervalMinutes
        val todayKey = getTodayKey(packageName)
        val set = getAlertedSet(context, todayKey)

        if (!set.contains(milestone)) {
            var prev = intervalMinutes
            while (prev < milestone) {
                set.add(prev)
                prev += intervalMinutes
            }
            set.add(milestone)
            saveAlertedSet(context, todayKey, set)
            showPill(context, packageName, appName, milestone)
            return true
        }
        return false
    }

    fun showPill(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int,
        windowManagerOverride: WindowManager? = null
    ) {
        mainHandler.post {
            _currentPillState.value = HeadsUpPillData(packageName, appName, minutesUsed)

            dismissRunnable?.let { mainHandler.removeCallbacks(it) }

            triggerHapticPulse(context)

            showWindowManagerOverlay(context, packageName, appName, minutesUsed, windowManagerOverride)

            dismissRunnable = Runnable {
                dismissPill()
            }
            mainHandler.postDelayed(dismissRunnable!!, 4800L)
        }
    }

    private fun triggerHapticPulse(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(45L)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun showWindowManagerOverlay(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int,
        windowManagerOverride: WindowManager? = null
    ) {
        removeOverlayView(immediate = true)

        val wmList = mutableListOf<Pair<WindowManager, Int>>()

        if (windowManagerOverride != null) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            wmList.add(windowManagerOverride to type)
        }

        if (context is AccessibilityService) {
            val a11yWm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (a11yWm != null) {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                wmList.add(a11yWm to type)
            }
        }

        val appWm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (appWm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wmList.add(appWm to WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                @Suppress("DEPRECATION")
                wmList.add(appWm to WindowManager.LayoutParams.TYPE_PHONE)
            }
        }

        var attachedSuccessfully = false
        val density = context.resources.displayMetrics.density
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val topMarginPx = if (isLandscape) {
            (14 * density).toInt()
        } else {
            var statusBarHeight = 0
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
            }
            if (statusBarHeight <= 0) {
                statusBarHeight = (38 * density).toInt()
            }
            maxOf(statusBarHeight + (10 * density).toInt(), (48 * density).toInt())
        }

        for ((wm, windowType) in wmList) {
            try {
                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    windowType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    y = topMarginPx
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }

                val pillView = createPillView(context, packageName, appName, minutesUsed) {
                    dismissPill()
                }

                pillView.translationY = -90f * density
                pillView.alpha = 0f
                pillView.scaleX = 0.85f
                pillView.scaleY = 0.85f

                wm.addView(pillView, params)
                activeOverlayView = pillView
                activeWindowManager = wm
                attachedSuccessfully = true

                pillView.post {
                    pillView.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(360)
                        .setInterpolator(DecelerateInterpolator(1.5f))
                        .start()
                }
                break
            } catch (e: Exception) {
                Log.w(TAG, "Attempt with window type $windowType failed: ${e.message}")
            }
        }

        if (!attachedSuccessfully) {
            showNotificationFallback(context, packageName, appName, minutesUsed)
        }
    }

    private fun showNotificationFallback(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Screen Time Pill Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Screen time awareness notifications"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val formattedTime = formatMinutes(minutesUsed)
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(appName)
                .setContentText("Screen time milestone: $formattedTime today")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setTimeoutAfter(5000L)
                .build()

            val notificationId = 88000 + (packageName.hashCode() % 1000)
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
        }
    }

    private fun formatMinutes(minutes: Int): String {
        return if (minutes >= 60) {
            val h = minutes / 60
            val m = minutes % 60
            if (m == 0) "${h}h" else "${h}h ${m}m"
        } else {
            "$minutes min"
        }
    }

    private fun createPillView(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int,
        onDismiss: () -> Unit
    ): View {
        val density = context.resources.displayMetrics.density

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val padH = (8 * density).toInt()
            val padV = (5 * density).toInt()
            setPadding(padH, padV, padH, padV)

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 22 * density
                setColor(0xFF2563EB.toInt())
            }
            background = bg
            elevation = 12 * density
        }

        val iconContainer = FrameLayout(context).apply {
            val sizePx = (32 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                marginEnd = (8 * density).toInt()
            }
            val circleBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF93C5FD.toInt())
            }
            background = circleBg
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
        }

        val iconView = ImageView(context).apply {
            val iconSizePx = (24 * density).toInt()
            layoutParams = FrameLayout.LayoutParams(iconSizePx, iconSizePx, Gravity.CENTER)
            scaleType = ImageView.ScaleType.FIT_CENTER

            val cachedBitmap = AppManagerHelper.getAppIconBitmapFromMemory(packageName)
            if (cachedBitmap != null) {
                setImageBitmap(cachedBitmap)
            } else {
                val appDrawable = try {
                    context.packageManager.getApplicationIcon(packageName)
                } catch (e: Exception) {
                    null
                }
                if (appDrawable != null) {
                    setImageDrawable(appDrawable)
                } else {
                    setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }
        iconContainer.addView(iconView)
        rootLayout.addView(iconContainer)

        val textCapsule = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val padCapsuleH = (12 * density).toInt()
            val padCapsuleV = (4 * density).toInt()
            setPadding(padCapsuleH, padCapsuleV, padCapsuleH, padCapsuleV)

            val capsuleBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setColor(Color.WHITE)
            }
            background = capsuleBg
        }

        val timeText = TextView(context).apply {
            text = formatMinutes(minutesUsed)
            setTextColor(0xFF1D4ED8.toInt())
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        textCapsule.addView(timeText)
        rootLayout.addView(textCapsule)

        rootLayout.setOnClickListener {
            onDismiss()
        }

        return rootLayout
    }

    fun dismissPill() {
        mainHandler.post {
            dismissRunnable?.let { mainHandler.removeCallbacks(it) }
            dismissRunnable = null
            _currentPillState.value = null
            removeOverlayView()
        }
    }

    private fun removeOverlayView(immediate: Boolean = false) {
        val view = activeOverlayView ?: return
        val wm = activeWindowManager ?: return
        activeOverlayView = null
        activeWindowManager = null

        if (immediate) {
            view.animate().cancel()
            view.visibility = View.GONE
            try {
                wm.removeViewImmediate(view)
            } catch (e: Exception) {
                try {
                    wm.removeView(view)
                } catch (e2: Exception) {
                }
            }
            return
        }

        val density = view.context.resources.displayMetrics.density
        view.animate().cancel()
        view.animate()
            .translationY(-90f * density)
            .alpha(0f)
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(260)
            .setInterpolator(AccelerateInterpolator(1.5f))
            .withEndAction {
                view.visibility = View.GONE
                try {
                    wm.removeViewImmediate(view)
                } catch (e: Exception) {
                    try {
                        wm.removeView(view)
                    } catch (e2: Exception) {
                    }
                }
            }
            .start()
    }
}
