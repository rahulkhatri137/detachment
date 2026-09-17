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
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isViewAttached = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupComposeView()
        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        scope.launch {
            PomodoroManager.state.collectLatest { state ->
                updateViewVisibility(state)
            }
        }
    }

    private fun setupComposeView() {
        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PomodoroOverlayService)
            setViewTreeViewModelStoreOwner(this@PomodoroOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PomodoroOverlayService)
            
            setContent {
                MaterialTheme {
                    val state by PomodoroManager.state.collectAsState()
                    var essentialApps by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList<AppLimitEntity>()) }
                    
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        PomodoroManager.repository?.allApps?.collect { apps ->
                            essentialApps = apps.filter { it.isEssential }
                        }
                    }

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
                            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(launchIntent)
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun updateViewVisibility(state: PomodoroState) {
        val shouldShow = state.isBlackoutActive && !state.isOverlayHidden
        
        if (shouldShow && !isViewAttached) {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            
            try {
                windowManager?.addView(composeView, params)
                isViewAttached = true
            } catch (e: Exception) {
                Log.e("PomodoroOverlay", "Failed to add view", e)
            }
        } else if (!shouldShow && isViewAttached) {
            try {
                windowManager?.removeView(composeView)
                isViewAttached = false
            } catch (e: Exception) {
                Log.e("PomodoroOverlay", "Failed to remove view", e)
            }
        }
        
        if (!state.isBlackoutActive) {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
