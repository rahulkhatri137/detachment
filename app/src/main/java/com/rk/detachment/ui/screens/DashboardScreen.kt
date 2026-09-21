package com.rk.detachment.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.model.ImportSummary
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.GlowingProgressRing
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.components.SettingsBackupDialog
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.PurpleSoft
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun DashboardScreen(
    uiState: DetachmentUiState,
    onLaunchApp: (AppLimitEntity) -> Unit,
    onNavigateToLimits: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToBlackout: () -> Unit,
    onNavigateToDistractions: () -> Unit,
    onNavigateToConsciousness: () -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
    onOpenUsageSettings: () -> Unit = {},
    onOpenOverlaySettings: () -> Unit = {},
    onExportSettings: (((String) -> Unit, (String) -> Unit) -> Unit)? = null,
    onImportSettings: ((String, (Result<ImportSummary>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showSettingsBackupDialog by remember { mutableStateOf(false) }
    val totalMins = uiState.totalScreenTimeTodayMinutes
    val totalLimitMins = uiState.totalDailyLimitMinutes.coerceAtLeast(1)
    val progress = (totalMins.toFloat() / totalLimitMins.toFloat()).coerceIn(0f, 1f)

    val hours = totalMins / 60
    val minutes = totalMins % 60

    val allPermissionsGranted = uiState.hasUsagePermission && uiState.isAccessibilityActive && uiState.hasOverlayPermission

    RadialGlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "dash_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Detachment",
                                color = TextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FrostedBadge(
                                text = if (uiState.isAccessibilityActive) "LIVE GUARD" else "STANDBY",
                                color = if (uiState.isAccessibilityActive) EmeraldAccent else AmberAccent,
                                backgroundColor = if (uiState.isAccessibilityActive) EmeraldAccent.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                                borderColor = if (uiState.isAccessibilityActive) EmeraldAccent.copy(alpha = 0.35f) else AmberAccent.copy(alpha = 0.35f)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceHigh)
                            .border(1.dp, GlassBorderHigh, CircleShape)
                            .clickable { showSettingsBackupDialog = true }
                            .testTag("dashboard_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & Backup",
                            tint = EmeraldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!allPermissionsGranted) {
                item(key = "service_status_banner") {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("protection_status_card"),
                        backgroundColor = GlassSurfaceHigh,
                        borderColor = AmberAccent.copy(alpha = 0.4f),
                        cornerRadius = 20.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System Real-App Enforcement",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To physically block real apps on your phone and track exact screen time, enable these Android permissions:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            PermissionRow(
                                title = "Live Accessibility Blocker",
                                description = "Detects when distracting apps open",
                                isGranted = uiState.isAccessibilityActive,
                                onGrant = onOpenAccessibilitySettings
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            PermissionRow(
                                title = "Screen Time Usage Access",
                                description = "Reads exact minutes spent on each app",
                                isGranted = uiState.hasUsagePermission,
                                onGrant = onOpenUsageSettings
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            PermissionRow(
                                title = "Overlay Friction Lock Screen",
                                description = "Shows mindful delay and lock screens over apps",
                                isGranted = uiState.hasOverlayPermission,
                                onGrant = onOpenOverlaySettings
                            )
                        }
                    }
                }
            }

            item(key = "screen_time_meter") {
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("screen_time_meter_card"),
                    cornerRadius = 24.dp,
                    backgroundColor = GlassSurfaceHigh,
                    borderColor = GlassBorderHigh,
                    onClick = onNavigateToConsciousness
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlowingProgressRing(
                            progress = if (uiState.isBlackoutActive) {
                                val total = uiState.blackoutTotalSeconds.coerceAtLeast(1).toFloat()
                                (uiState.blackoutSecondsRemaining.toFloat() / total).coerceIn(0f, 1f)
                            } else progress,
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onNavigateToConsciousness)
                                .testTag("screentime_ring_button"),
                            strokeWidth = 10.dp,
                            primaryColor = if (uiState.isBlackoutActive) PurplePrimary else if (progress >= 1f) RoseAccent else PurplePrimary,
                            secondaryColor = if (uiState.isBlackoutActive) PurpleLight else if (progress >= 1f) AmberAccent else PurpleLight,
                            trackColor = Color.White.copy(alpha = 0.06f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (uiState.isBlackoutActive) {
                                    val remSecs = uiState.blackoutSecondsRemaining
                                    val bMins = remSecs / 60
                                    val bSecs = remSecs % 60
                                    Text(
                                        text = String.format("%02d:%02d", bMins, bSecs),
                                        color = TextPrimary,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "BLACKOUT",
                                        color = PurpleSoft,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.8.sp
                                    )
                                } else {
                                    Text(
                                        text = "${hours}h ${minutes}m",
                                        color = TextPrimary,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "TODAY'S SCREEN TIME",
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PurplePrimary.copy(alpha = 0.16f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable(onClick = onNavigateToConsciousness)
                                .testTag("btn_open_consciousness_score")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoGraph,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Consciousness Score: ${uiState.consciousnessComparison.today.score}/100",
                                    color = PurpleLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.lockedAppsCount}",
                                    color = if (uiState.lockedAppsCount > 0) RoseAccent else TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Locked Apps",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(GlassBorderMedium)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.distractionsResistedCount}",
                                    color = CyanAccent,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Distractions Resisted",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(GlassBorderMedium)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.combinedFocusMinutes}m",
                                    color = PurpleLight,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Focus Logged",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item(key = "quick_actions_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FrostedGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_blackout"),
                        cornerRadius = 20.dp,
                        backgroundColor = GlassSurfaceMedium,
                        borderColor = GlassBorderMedium,
                        onClick = onNavigateToBlackout
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimary.copy(alpha = 0.20f))
                                    .border(1.dp, PurplePrimary.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = PurpleSoft,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Blackout",
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (uiState.isBlackoutActive) "Active" else "Max 10 Apps",
                                    color = if (uiState.isBlackoutActive) EmeraldAccent else TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    FrostedGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_schedules"),
                        cornerRadius = 20.dp,
                        backgroundColor = GlassSurfaceMedium,
                        borderColor = GlassBorderMedium,
                        onClick = onNavigateToSchedules
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AmberAccent.copy(alpha = 0.15f))
                                    .border(1.dp, AmberAccent.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Schedules",
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${uiState.activeSchedules.size} rules active",
                                    color = if (uiState.activeSchedules.isNotEmpty()) PurpleSoft else TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item(key = "shield_banner") {
                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_action_shield"),
                    cornerRadius = 20.dp,
                    backgroundColor = GlassSurfaceLow,
                    borderColor = GlassBorderLow,
                    onClick = onNavigateToDistractions
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(RoseAccent.copy(alpha = 0.18f))
                                    .border(1.dp, RoseAccent.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = RoseAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Distraction Shield",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.shieldActiveApps.size} apps delayed for mindful pause",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            item(key = "apps_section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Used Apps",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "View All (${uiState.allApps.size})",
                        color = PurpleLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLimits)
                            .testTag("manage_all_limits_btn")
                    )
                }
            }

            items(uiState.allApps.take(6), key = { it.packageName }) { app ->
                AppUsageGlassTile(
                    app = app,
                    isDelayForDistractingApps = uiState.isDelayForDistractingApps,
                    onOpenApp = { onLaunchApp(app) }
                )
            }
        }

        if (showSettingsBackupDialog && onExportSettings != null && onImportSettings != null) {
            SettingsBackupDialog(
                onDismiss = { showSettingsBackupDialog = false },
                onExport = onExportSettings,
                onImport = onImportSettings,
                appLimitsCount = uiState.allApps.size,
                schedulesCount = uiState.scheduleRules.size
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isGranted) EmeraldAccent.copy(alpha = 0.08f) else Color(0x18FFFFFF))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isGranted) EmeraldAccent else AmberAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (!isGranted) {
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Enable", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Active", color = EmeraldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppUsageGlassTile(
    app: AppLimitEntity,
    isDelayForDistractingApps: Boolean = true,
    onOpenApp: () -> Unit
) {
    val isLocked = app.isCurrentlyLocked()
    val isShieldActive = app.isEffectiveShieldActive(isDelayForDistractingApps)
    val isTempUnlocked = app.isTemporaryUnlocked()
    val limit = app.dailyLimitMinutes
    val used = app.usedTodayMinutes
    val usagePercent = if (limit > 0) (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f

    val targetBorderColor = when {
        isLocked -> RoseAccent.copy(alpha = 0.50f)
        isShieldActive -> AmberAccent.copy(alpha = 0.50f)
        else -> GlassBorderLow
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 350),
        label = "dashboard_app_tile_border"
    )

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenApp)
            .testTag("app_tile_${app.packageName.replace(".", "_")}"),
        cornerRadius = 18.dp,
        backgroundColor = GlassSurfaceLow,
        borderColor = animatedBorderColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconView(
                packageName = app.packageName,
                appName = app.appName,
                size = 40.dp,
                isLocked = isLocked,
                isShieldActive = isShieldActive,
                cornerRadius = 10.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${used}m ${if (limit > 0) "/ ${limit}m" else ""}",
                        color = if (isLocked) RoseAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when {
                        isLocked -> RoseAccent
                        usagePercent > 0.8f -> AmberAccent
                        else -> PurplePrimary
                    },
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (app.isEssential) {
                            Text("Essential", color = EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 12.sp)
                        }
                        if (isShieldActive) {
                            Text("Distraction Shield", color = AmberAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 12.sp)
                        }
                    }

                    if (isTempUnlocked) {
                        val remainingGraceMinutes = (app.remainingUnlockSeconds() / 60).coerceAtLeast(1)
                        Text("${remainingGraceMinutes}m Pause", color = EmeraldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                    } else if (isLocked) {
                        Text("Locked", color = RoseAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Launch",
                    tint = PurpleLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}