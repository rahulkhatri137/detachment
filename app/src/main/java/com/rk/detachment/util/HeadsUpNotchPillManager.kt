package com.rk.detachment.util

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
        val set = alertedMilestones.getOrPut(todayKey) { mutableSetOf() }

        if (!set.contains(milestone)) {
            set.add(milestone)
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

            showWindowManagerOverlay(context, packageName, appName, minutesUsed, windowManagerOverride)

            dismissRunnable = Runnable {
                dismissPill()
            }
            mainHandler.postDelayed(dismissRunnable!!, 4500L)
        }
    }

    private fun showWindowManagerOverlay(
        context: Context,
        packageName: String,
        appName: String,
        minutesUsed: Int,
        windowManagerOverride: WindowManager? = null
    ) {
        try {
            removeOverlayView()

            val wm = windowManagerOverride
                ?: (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)
                ?: return

            val windowType = when {
                context is AccessibilityService -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Settings.canDrawOverlays(context) -> {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                }
            }

            val density = context.resources.displayMetrics.density
            val topMarginPx = (14 * density).toInt()

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = topMarginPx
            }

            val pillView = createPillView(context, packageName, appName, minutesUsed) {
                dismissPill()
            }

            activeOverlayView = pillView
            activeWindowManager = wm

            wm.addView(pillView, params)

            pillView.translationY = -140f * density
            pillView.alpha = 0f
            pillView.scaleX = 0.85f
            pillView.scaleY = 0.85f
            pillView.animate()
                .translationY(0f)
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        } catch (e: Exception) {
            Log.d(TAG, "Window overlay could not be attached directly: ${e.message}")
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
            clipToOutline = true
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

        val formattedTime = if (minutesUsed >= 60) {
            val h = minutesUsed / 60
            val m = minutesUsed % 60
            if (m == 0) "${h}h" else "${h}h ${m}m"
        } else {
            "$minutesUsed min"
        }

        val timeText = TextView(context).apply {
            text = formattedTime
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

    private fun removeOverlayView() {
        val view = activeOverlayView ?: return
        val wm = activeWindowManager ?: return
        activeOverlayView = null
        activeWindowManager = null

        val density = view.context.resources.displayMetrics.density
        view.animate()
            .translationY(-140f * density)
            .alpha(0f)
            .scaleX(0.85f)
            .scaleY(0.85f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                try {
                    wm.removeView(view)
                } catch (e: Exception) {
                }
            }
            .start()
    }
}
