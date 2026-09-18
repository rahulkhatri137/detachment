package com.rk.detachment.service

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.rk.detachment.ui.screens.ActiveBlackoutCanvas
import com.rk.detachment.util.AppManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.rk.detachment.data.local.entities.AppLimitEntity

class PomodoroOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, PomodoroOverlayService::class.java)
            context.startService(intent)
        }
        fun stop(context: Context) {
            val intent = Intent(context, PomodoroOverlayService::class.java)
            context.stopService(intent)
        }
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isViewAttached = false
    private var lastAppliedShouldShow: Boolean? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupComposeView()
        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        scope.launch {
            PomodoroManager.state
                .map { Pair(it.isBlackoutActive, it.isOverlayHidden) }
                .distinctUntilChanged()
                .collectLatest { (isActive, isHidden) ->
                    updateViewVisibility(isActive, isHidden)
                }
        }
    }

    private fun setupComposeView() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        overlayParams = params

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PomodoroOverlayService)
            setViewTreeViewModelStoreOwner(this@PomodoroOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PomodoroOverlayService)
            fitsSystemWindows = false
            
            setContent {
                MaterialTheme {
                    val state by PomodoroManager.state.collectAsState()
                    var essentialApps by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList<AppLimitEntity>()) }
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        PomodoroManager.repository?.allApps?.collect { apps ->
                            essentialApps = apps.filter { it.isEssential }
                        }
                    }

                    if (state.isBlackoutActive && !state.isOverlayHidden) {
                        ActiveBlackoutCanvas(
                            blackoutTotalSeconds = state.blackoutTotalSeconds,
                            blackoutSecondsRemaining = state.blackoutSecondsRemaining,
                            isPomodoroRunning = state.isPomodoroRunning,
                            pomodoroSessionTag = state.pomodoroSessionTag,
                            essentialApps = essentialApps,
                            onPause = { PomodoroManager.pause() },
                            onResume = { PomodoroManager.resume() },
                            onRequestStop = { PomodoroManager.stop() },
                            onOpenEssentialApp = { app ->
                                PomodoroManager.setOverlayHidden(true)
                                AppManagerHelper.launchRealApp(this@PomodoroOverlayService, app.packageName)
                            }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
    
    private fun updateViewVisibility(isActive: Boolean, isHidden: Boolean) {
        if (!isActive) {
            if (isViewAttached) {
                try {
                    windowManager?.removeView(composeView)
                } catch (e: Exception) {}
                isViewAttached = false
            }
            lastAppliedShouldShow = null
            stopSelf()
            return
        }

        val shouldShow = !isHidden
        if (lastAppliedShouldShow == shouldShow && isViewAttached) {
            return
        }

        val params = overlayParams ?: return
        val view = composeView ?: return

        if (shouldShow) {
            params.alpha = 1.0f
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            view.visibility = View.VISIBLE

            if (!isViewAttached) {
                try {
                    windowManager?.addView(view, params)
                    isViewAttached = true
                } catch (e: Exception) {
                    Log.e("PomodoroOverlay", "Failed to add view", e)
                }
            } else {
                try {
                    windowManager?.updateViewLayout(view, params)
                } catch (e: Exception) {}
            }
        } else {
            params.alpha = 0.0f
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            view.visibility = View.GONE

            if (isViewAttached) {
                try {
                    windowManager?.updateViewLayout(view, params)
                } catch (e: Exception) {}
            }
        }
        lastAppliedShouldShow = shouldShow
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = PomodoroManager.state.value
        updateViewVisibility(state.isBlackoutActive, state.isOverlayHidden)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (isViewAttached) {
            try {
                windowManager?.removeView(composeView)
            } catch (e: Exception) {}
            isViewAttached = false
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
