package com.rk.detachment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rk.detachment.ui.BlockOverlayActivity
import com.rk.detachment.ui.components.AppLaunchSecurityScreen
import com.rk.detachment.ui.components.HeadsUpNotchPillOverlay
import com.rk.detachment.ui.components.LiquidGlassBottomNavBar
import com.rk.detachment.ui.components.LotusBloomContainer
import com.rk.detachment.ui.screens.AppLimitsScreen
import com.rk.detachment.ui.screens.BlackoutPomodoroScreen
import com.rk.detachment.ui.screens.ConsciousnessScoreScreen
import com.rk.detachment.ui.screens.DashboardScreen
import com.rk.detachment.ui.screens.DistractionShieldScreen
import com.rk.detachment.ui.screens.SchedulesScreen
import com.rk.detachment.ui.screens.WebsiteLimitsScreen
import com.rk.detachment.ui.theme.DetachmentTheme
import com.rk.detachment.ui.theme.FrostedBackground
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.util.HeadsUpNotchPillManager
import com.rk.detachment.viewmodel.DetachmentViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Usage", Icons.Default.Home, "tab_dashboard"),
    LIMITS("Limits", Icons.Default.LockClock, "tab_limits"),
    SCHEDULES("Schedules", Icons.Default.Schedule, "tab_schedules"),
    BLACKOUT("Blackout", Icons.Default.Timer, "tab_blackout"),
    DISTRACTIONS("Shield", Icons.Default.Shield, "tab_distractions")
}

class MainActivity : ComponentActivity() {
    private val viewModel: DetachmentViewModel by viewModels()
    private val isAppUnlockedState = mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        BlockOverlayActivity.dismissIfActive()
        viewModel.checkPermissionsAndRefresh()
    }

    override fun onStop() {
        super.onStop()
        isAppUnlockedState.value = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        BlockOverlayActivity.dismissIfActive()
        enableEdgeToEdge()
        setContent {
            DetachmentTheme {
                LotusBloomContainer {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var isAppUnlocked by remember { isAppUnlockedState }
                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
                var showConsciousnessScreen by remember { mutableStateOf(false) }
                var showWebsiteLimitsScreen by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusMessage()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    val requiresAppAuth = uiState.isAppAuthEnabled && !isAppUnlocked

                    if (requiresAppAuth) {
                        AppLaunchSecurityScreen(
                            masterPin = uiState.masterPin,
                            onUnlocked = { isAppUnlocked = true }
                        )
                    } else {
                        Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = FrostedBackground,
                        snackbarHost = {
                            SnackbarHost(snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = FrostedBackgroundDarker,
                                    contentColor = TextPrimary,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding())
                        ) {
                            AnimatedContent(
                                targetState = when {
                                    showConsciousnessScreen -> "CONSCIOUSNESS"
                                    showWebsiteLimitsScreen -> "WEBSITES"
                                    else -> currentTab.name
                                },
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "tab_switch_animation",
                                modifier = Modifier.fillMaxSize()
                            ) { screenKey ->
                        when (screenKey) {
                            "CONSCIOUSNESS" -> {
                                ConsciousnessScoreScreen(
                                    uiState = uiState,
                                    onNavigateBack = { showConsciousnessScreen = false },
                                    onNavigateToShield = {
                                        showConsciousnessScreen = false
                                        currentTab = NavigationTab.DISTRACTIONS
                                    },
                                    onNavigateToLimits = {
                                        showConsciousnessScreen = false
                                        currentTab = NavigationTab.LIMITS
                                    },
                                    onRefresh = { viewModel.refreshConsciousnessMetrics() }
                                )
                            }
                            "WEBSITES" -> {
                                WebsiteLimitsScreen(
                                    uiState = uiState,
                                    onNavigateBack = { showWebsiteLimitsScreen = false },
                                    onUpdateLimit = { domain, limit, isHardMode -> viewModel.updateWebsiteLimit(domain, limit, isHardMode) },
                                    onToggleLock = { domain, isLocked -> viewModel.toggleWebsiteLock(domain, isLocked) },
                                    onToggleEnabled = { domain, isEnabled -> viewModel.toggleWebsiteEnabled(domain, isEnabled) },
                                    onUnlockWebsite = { domain, min -> viewModel.unlockWebsite(domain, min) },
                                    onRelockWebsite = { domain -> viewModel.relockWebsite(domain) },
                                    onAddWebsite = { domain, name, cat, limit -> viewModel.addCustomWebsite(domain, name, cat, limit) },
                                    onDeleteWebsite = { domain -> viewModel.deleteCustomWebsite(domain) },
                                    onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                                    onUpdateMasterPin = { pin -> viewModel.updateMasterPin(pin) }
                                )
                            }
                            NavigationTab.DASHBOARD.name -> {
                                DashboardScreen(
                                    uiState = uiState,
                                    onLaunchApp = { app -> viewModel.launchRealAppOrBlock(this@MainActivity, app) },
                                    onNavigateToLimits = { currentTab = NavigationTab.LIMITS },
                                    onNavigateToSchedules = { currentTab = NavigationTab.SCHEDULES },
                                    onNavigateToBlackout = { currentTab = NavigationTab.BLACKOUT },
                                    onNavigateToDistractions = { currentTab = NavigationTab.DISTRACTIONS },
                                    onNavigateToConsciousness = { showConsciousnessScreen = true },
                                    onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings(this@MainActivity) },
                                    onOpenUsageSettings = { viewModel.openUsageAccessSettings(this@MainActivity) },
                                    onOpenOverlaySettings = { viewModel.openOverlaySettings(this@MainActivity) },
                                    onExportSettings = { onSuccess, onError -> viewModel.exportSettings(onSuccess, onError) },
                                    onImportSettings = { json, onResult -> viewModel.importSettings(json, onResult) }
                                )
                            }
                            NavigationTab.LIMITS.name -> {
                                AppLimitsScreen(
                                    uiState = uiState,
                                    onUpdateLimit = { pkg, limit, isHardMode -> viewModel.updateAppLimit(pkg, limit, isHardMode) },
                                    onToggleLock = { pkg, isLocked -> viewModel.toggleManualLock(pkg, isLocked) },
                                    onUnlock15Min = { pkg -> viewModel.unlockAppFor15Minutes(pkg) },
                                    onUnlockApp = { pkg, min -> viewModel.unlockApp(pkg, min) },
                                    onSetUnlockMinutes = { min -> viewModel.setUnlockMinutes(min) },
                                    onRelockApp = { pkg -> viewModel.relockApp(pkg) },
                                    onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                                    onUpdateMasterPin = { pin -> viewModel.updateMasterPin(pin) },
                                    onSetAppAuthEnabled = { enabled -> viewModel.setAppAuthEnabled(enabled) },
                                    onLaunchApp = { app -> viewModel.launchRealAppOrBlock(this@MainActivity, app) },
                                    onRefreshApps = { viewModel.scanInstalledApps() },
                                    onOpenUsageSettings = { viewModel.openUsageAccessSettings(this@MainActivity) },
                                    onUpdateCategory = { pkg, cat -> viewModel.updateAppCategory(pkg, cat) },
                                    onToggleHeadsUpPill = { enabled -> viewModel.setHeadsUpPillEnabled(enabled) },
                                    onNavigateToWebsiteLimits = { showWebsiteLimitsScreen = true }
                                )
                            }
                            NavigationTab.SCHEDULES.name -> {
                                SchedulesScreen(
                                    uiState = uiState,
                                    onToggleRule = { id, enabled -> viewModel.toggleScheduleRule(id, enabled) },
                                    onNavigateToBlackout = { currentTab = NavigationTab.BLACKOUT },
                                    onSaveRule = { rule -> viewModel.saveScheduleRule(rule) },
                                    onDeleteRule = { rule -> viewModel.deleteScheduleRule(rule) },
                                    onResetDefaults = { viewModel.resetSchedulesToDefaults() }
                                )
                            }
                            NavigationTab.BLACKOUT.name -> {
                                BlackoutPomodoroScreen(
                                    uiState = uiState,
                                    onStartBlackout = { duration, tag -> viewModel.startPomodoroBlackout(duration, tag) },
                                    onPauseBlackout = { viewModel.pausePomodoro() },
                                    onResumeBlackout = { viewModel.resumePomodoro() },
                                    onStopBlackout = { viewModel.stopBlackout() },
                                    onToggleEssential = { pkg, isEss -> viewModel.toggleEssential(pkg, isEss) },
                                    onToggleDistracting = { pkg, isDis -> viewModel.toggleDistracting(pkg, isDis) },
                                    onOpenEssentialApp = { app -> viewModel.launchRealAppOrBlock(this@MainActivity, app) },
                                    onStartSelfRestraint = { duration, target -> viewModel.startSelfRestraint(duration, target) },
                                    onStopSelfRestraint = { viewModel.stopSelfRestraint() }
                                )
                            }
                            NavigationTab.DISTRACTIONS.name -> {
                                DistractionShieldScreen(
                                    uiState = uiState,
                                    onToggleShieldActive = { pkg, isShield -> viewModel.toggleShieldActive(pkg, isShield) },
                                    onSetDelaySeconds = { sec -> viewModel.setDelaySeconds(sec) },
                                    onToggleDelayForDistractingApps = { enabled -> viewModel.setDelayForDistractingApps(enabled) }
                                )
                            }
                        }
                    }

                    if (!uiState.isBlackoutActive) {
                        LiquidGlassBottomNavBar(
                            currentTab = currentTab,
                            showConsciousnessScreen = showConsciousnessScreen,
                            onTabSelected = { tab ->
                                currentTab = tab
                                showConsciousnessScreen = false
                                showWebsiteLimitsScreen = false
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }

                val headsUpPillState by HeadsUpNotchPillManager.currentPillState.collectAsStateWithLifecycle()
                HeadsUpNotchPillOverlay(
                    pillData = headsUpPillState,
                    onDismiss = { HeadsUpNotchPillManager.dismissPill() }
                )
            }
        }
    }
}
}
}
