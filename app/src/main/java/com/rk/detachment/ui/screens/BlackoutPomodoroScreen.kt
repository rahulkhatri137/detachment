package com.rk.detachment.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassButton
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.LiquidGlassAlertDialog
import com.rk.detachment.ui.components.LiquidGlassDialogButton
import com.rk.detachment.ui.components.LiquidGlassHoldButton
import com.rk.detachment.ui.components.LiquidGlassSwitch
import com.rk.detachment.ui.components.GlowingProgressRing
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun BlackoutPomodoroScreen(
    uiState: com.rk.detachment.viewmodel.ScreenTimeUiState,
    onStartBlackout: (Int, String) -> Unit,
    onPauseBlackout: () -> Unit,
    onResumeBlackout: () -> Unit,
    onStopBlackout: () -> Unit,
    onToggleEssential: (String, Boolean) -> Unit,
    onToggleDistracting: (String, Boolean) -> Unit,
    onOpenEssentialApp: (AppLimitEntity) -> Unit,
    onStartSelfRestraint: (Int, String) -> Unit = { _, _ -> },
    onStopSelfRestraint: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDurationMinutes by remember { mutableIntStateOf(30) }
    var selectedTag by remember { mutableStateOf("Deep Work") }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showEssentialAppsDialog by remember { mutableStateOf(false) }
    var showDistractingAppsDialog by remember { mutableStateOf(false) }
    var showSelfRestraintDialog by remember { mutableStateOf(false) }
    var showEndSelfRestraintDialog by remember { mutableStateOf(false) }
    var selectedSelfRestraintDuration by remember { mutableIntStateOf(30) }
    var selectedSelfRestraintTarget by remember { mutableStateOf("DISTRACTING") }

    val durations = listOf(15, 30, 45, 60, 90)
    val tags = listOf("Deep Work", "Study", "Reading", "Meditation", "Code")

    if (uiState.isBlackoutActive) {
        ActiveBlackoutCanvas(
            blackoutTotalSeconds = uiState.blackoutTotalSeconds,
            blackoutSecondsRemaining = uiState.blackoutSecondsRemaining,
            isPomodoroRunning = uiState.isPomodoroRunning,
            pomodoroSessionTag = uiState.pomodoroSessionTag,
            essentialApps = uiState.essentialApps,
            onPause = onPauseBlackout,
            onResume = onResumeBlackout,
            onRequestStop = onStopBlackout,
            onOpenEssentialApp = onOpenEssentialApp
        )

        if (showExitConfirmDialog) {
            LiquidGlassAlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                accentColor = RoseAccent,
                title = {
                    Text("Exit Pomodoro Blackout?", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Exiting now will end your Detachment blackout session early and unlock all apps.",
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                },
                confirmButton = {
                    LiquidGlassHoldButton(
                        text = "End Session",
                        holdingText = "Hold to commit",
                        accentColor = RoseAccent,
                        holdDurationMillis = 3000L,
                        onAction = {
                            showExitConfirmDialog = false
                            onStopBlackout()
                        },
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .height(44.dp)
                            .testTag("confirm_exit_blackout_btn")
                    )
                },
                dismissButton = {
                    LiquidGlassDialogButton(
                        onClick = { showExitConfirmDialog = false },
                        accentColor = PurpleLight,
                        modifier = Modifier.testTag("keep_focusing_exit_blackout_btn")
                    ) {
                        Text("Keep Focusing", color = PurpleLight)
                    }
                }
            )
        }
    } else {
        RadialGlassBackground(modifier = modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "header") {
                    Column {
                        Text(
                            text = "Pomodoro Blackout",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pitch black deep focus mode with max 10 essential apps",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                item(key = "durations") {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassSurfaceHigh) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FOCUS DURATION",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                durations.forEach { min ->
                                    val isSelected = selectedDurationMinutes == min
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) PurplePrimary else GlassSurfaceLow,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PurplePrimary else GlassBorderLow
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedDurationMinutes = min }
                                            .testTag("duration_btn_$min")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${min}m",
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "tags") {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassSurfaceHigh) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "SESSION INTENTION",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(tags) { tag ->
                                    val isSelected = selectedTag == tag
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) PurplePrimary.copy(alpha = 0.25f) else GlassSurfaceLow,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PurpleLight else GlassBorderLow
                                        ),
                                        modifier = Modifier
                                            .clickable { selectedTag = tag }
                                            .testTag("tag_chip_$tag")
                                    ) {
                                        Text(
                                            text = tag,
                                            color = if (isSelected) PurpleLight else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "start_button") {
                    FrostedGlassButton(
                        text = "Ignite Blackout",
                        icon = Icons.Default.FlashOn,
                        onClick = {
                            onStartBlackout(selectedDurationMinutes, selectedTag)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("start_blackout_btn")
                    )
                }

                item(key = "essential_apps_card") {
                    val essentialCount = uiState.essentialApps.size
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_essential_apps_card"),
                        backgroundColor = GlassSurfaceHigh,
                        borderColor = EmeraldAccent.copy(alpha = 0.4f),
                        onClick = { showEssentialAppsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Essential Apps",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$essentialCount of 10 Permitted Apps",
                                        color = if (essentialCount > 0) EmeraldAccent else TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FrostedBadge(
                                    text = "$essentialCount / 10",
                                    color = if (essentialCount == 10) EmeraldAccent else PurpleLight,
                                    backgroundColor = EmeraldAccent.copy(alpha = 0.18f),
                                    borderColor = EmeraldAccent.copy(alpha = 0.40f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Manage",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item(key = "distracting_apps_card") {
                    val distractingCount = uiState.distractingApps.size
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_distracting_apps_card"),
                        backgroundColor = GlassSurfaceHigh,
                        borderColor = AmberAccent.copy(alpha = 0.4f),
                        onClick = { showDistractingAppsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(AmberAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Distracting Apps",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$distractingCount Marked as Distracting",
                                        color = if (distractingCount > 0) AmberAccent else TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FrostedBadge(
                                    text = "$distractingCount APPS",
                                    color = AmberAccent,
                                    backgroundColor = AmberAccent.copy(alpha = 0.18f),
                                    borderColor = AmberAccent.copy(alpha = 0.35f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Manage",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                item(key = "self_restraint_mode_card") {
                    if (uiState.isSelfRestraintActive) {
                        val remainingSecs = uiState.selfRestraintSecondsRemaining
                        val totalSecs = uiState.selfRestraintTotalSeconds.coerceAtLeast(1)
                        val progress = (remainingSecs.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)
                        val hours = remainingSecs / 3600
                        val minutes = (remainingSecs % 3600) / 60
                        val seconds = remainingSecs % 60
                        val timeDisplay = if (hours > 0) {
                            String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format("%02d:%02d", minutes, seconds)
                        }
                        val targetLabel = if (uiState.selfRestraintTarget == "ALL_NON_ESSENTIAL") "Non-essential apps blocked" else "Distracting apps blocked"

                        FrostedGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("self_restraint_countdown_card"),
                            backgroundColor = GlassSurfaceHigh,
                            borderColor = PurplePrimary.copy(alpha = 0.65f),
                            onClick = { showEndSelfRestraintDialog = true }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldAccent)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SELF-RESTRAINT ACTIVE",
                                            color = PurpleLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    FrostedBadge(
                                        text = if (uiState.selfRestraintTarget == "ALL_NON_ESSENTIAL") "NON-ESSENTIAL" else "DISTRACTING",
                                        color = AmberAccent,
                                        backgroundColor = AmberAccent.copy(alpha = 0.18f),
                                        borderColor = AmberAccent.copy(alpha = 0.35f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                GlowingProgressRing(
                                    progress = progress,
                                    modifier = Modifier.size(160.dp),
                                    strokeWidth = 8.dp,
                                    primaryColor = PurplePrimary,
                                    secondaryColor = PurpleLight,
                                    trackColor = Color(0x1AFFFFFF)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = timeDisplay,
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "REMAINING",
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = targetLabel,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap countdown clock to end session early",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        FrostedGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("self_restraint_mode_card"),
                            backgroundColor = GlassSurfaceHigh,
                            borderColor = PurpleLight.copy(alpha = 0.4f),
                            onClick = { showSelfRestraintDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PurplePrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SelfImprovement,
                                            contentDescription = null,
                                            tint = PurpleLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Self-restraint Mode",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Flexible focus schedule without blackout canvas",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSelfRestraintDialog) {
            val selfRestraintDurations = listOf(15, 20, 30, 45, 60, 90)
            LiquidGlassAlertDialog(
                onDismissRequest = { showSelfRestraintDialog = false },
                accentColor = PurpleLight,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = PurpleLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Self-restraint Mode",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Enforce a temporary focus schedule to block chosen apps for a specific duration. Unblocked apps remain accessible normally.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Column {
                            Text(
                                text = "DURATION (MINUTES)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selfRestraintDurations.forEach { min ->
                                    val isSelected = selectedSelfRestraintDuration == min
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) PurplePrimary else GlassSurfaceLow,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) PurpleLight else GlassBorderLow
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedSelfRestraintDuration = min }
                                            .testTag("self_restraint_duration_$min")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${min}m",
                                                color = if (isSelected) Color.White else TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "CHOOSE TARGET APPS",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val isDistractingSelected = selectedSelfRestraintTarget == "DISTRACTING"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDistractingSelected) AmberAccent.copy(alpha = 0.22f) else GlassSurfaceLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isDistractingSelected) AmberAccent else GlassBorderLow
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSelfRestraintTarget = "DISTRACTING" }
                                        .testTag("self_restraint_target_distracting")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (isDistractingSelected) AmberAccent else TextSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Block Distracting",
                                            color = if (isDistractingSelected) Color.White else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }

                                val isNonEssentialSelected = selectedSelfRestraintTarget == "ALL_NON_ESSENTIAL"
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isNonEssentialSelected) PurplePrimary.copy(alpha = 0.25f) else GlassSurfaceLow,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isNonEssentialSelected) PurpleLight else GlassBorderLow
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedSelfRestraintTarget = "ALL_NON_ESSENTIAL" }
                                        .testTag("self_restraint_target_non_essential")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = if (isNonEssentialSelected) PurpleLight else TextSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Block Non-essential",
                                            color = if (isNonEssentialSelected) Color.White else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    LiquidGlassHoldButton(
                        text = "Hold to commit",
                        holdingText = "Hold to commit",
                        accentColor = PurpleLight,
                        holdDurationMillis = 3000L,
                        onAction = {
                            showSelfRestraintDialog = false
                            onStartSelfRestraint(selectedSelfRestraintDuration, selectedSelfRestraintTarget)
                        },
                        modifier = Modifier
                            .widthIn(min = 150.dp)
                            .height(44.dp)
                            .testTag("commit_self_restraint_btn")
                    )
                },
                dismissButton = {
                    LiquidGlassDialogButton(
                        onClick = { showSelfRestraintDialog = false },
                        accentColor = PurpleLight,
                        modifier = Modifier.testTag("cancel_self_restraint_btn")
                    ) {
                        Text("Cancel", color = PurpleLight)
                    }
                }
            )
        }

        if (showEndSelfRestraintDialog) {
            LiquidGlassAlertDialog(
                onDismissRequest = { showEndSelfRestraintDialog = false },
                accentColor = RoseAccent,
                title = {
                    Text("End Self-restraint Session?", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Are you sure you want to end this self-restraint focus session early? Blocked apps will be unlocked.",
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                },
                confirmButton = {
                    LiquidGlassHoldButton(
                        text = "End Session",
                        holdingText = "Hold to commit",
                        accentColor = RoseAccent,
                        holdDurationMillis = 3000L,
                        onAction = {
                            showEndSelfRestraintDialog = false
                            onStopSelfRestraint()
                        },
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .height(44.dp)
                            .testTag("confirm_end_self_restraint_btn")
                    )
                },
                dismissButton = {
                    LiquidGlassDialogButton(
                        onClick = { showEndSelfRestraintDialog = false },
                        accentColor = PurpleLight,
                        modifier = Modifier.testTag("cancel_end_self_restraint_btn")
                    ) {
                        Text("Keep Focusing", color = PurpleLight)
                    }
                }
            )
        }

        if (showEssentialAppsDialog) {
            EssentialAppsDialog(
                uiState = uiState,
                onToggleEssential = onToggleEssential,
                onDismiss = { showEssentialAppsDialog = false }
            )
        }

        if (showDistractingAppsDialog) {
            DistractingAppsDialog(
                uiState = uiState,
                onToggleDistracting = onToggleDistracting,
                onDismiss = { showDistractingAppsDialog = false }
            )
        }
    }
}

@Composable
private fun EssentialAppsDialog(
    uiState: com.rk.detachment.viewmodel.DetachmentUiState,
    onToggleEssential: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val essentialCount = uiState.essentialApps.size

    val filteredApps = remember(uiState.allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.allApps
        } else {
            uiState.allApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LiquidGlassAlertDialog(
        onDismissRequest = onDismiss,
        accentColor = EmeraldAccent,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Essential Apps Whitelist",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Permitted during Pomodoro Blackout",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                FrostedBadge(
                    text = "$essentialCount / 10",
                    color = if (essentialCount == 10) EmeraldAccent else PurpleLight,
                    backgroundColor = EmeraldAccent.copy(alpha = 0.18f),
                    borderColor = EmeraldAccent.copy(alpha = 0.40f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("search_essential_apps_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = GlassBorderMedium,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isEssential = app.isEssential
                        val canSelect = isEssential || essentialCount < 10

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEssential) EmeraldAccent.copy(alpha = 0.14f) else GlassSurfaceLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isEssential) EmeraldAccent.copy(alpha = 0.4f) else GlassBorderLow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isEssential || canSelect) {
                                        onToggleEssential(app.packageName, !isEssential)
                                    }
                                }
                                .testTag("essential_item_${app.packageName.replace(".", "_")}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AppIconView(
                                        packageName = app.packageName,
                                        appName = app.appName,
                                        size = 36.dp,
                                        isEssential = isEssential,
                                        cornerRadius = 8.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = app.appName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isEssential) "Essential App" else if (app.isDistracting) "Distracting App" else "Standard App",
                                            color = if (isEssential) EmeraldAccent else if (app.isDistracting) AmberAccent else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                LiquidGlassSwitch(
                                    checked = isEssential,
                                    onCheckedChange = { checked ->
                                        if (isEssential || canSelect) {
                                            onToggleEssential(app.packageName, checked)
                                        }
                                    },
                                    enabled = isEssential || canSelect,
                                    activeColor = EmeraldAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LiquidGlassDialogButton(
                onClick = onDismiss,
                accentColor = EmeraldAccent,
                modifier = Modifier.testTag("done_essential_apps_btn")
            ) {
                Text("Done", color = EmeraldAccent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DistractingAppsDialog(
    uiState: com.rk.detachment.viewmodel.DetachmentUiState,
    onToggleDistracting: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val distractingCount = uiState.distractingApps.size

    val filteredApps = remember(uiState.allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.allApps
        } else {
            uiState.allApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LiquidGlassAlertDialog(
        onDismissRequest = onDismiss,
        accentColor = AmberAccent,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distracting Apps Blocklist",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Designated Distracting Apps",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
                FrostedBadge(
                    text = "$distractingCount",
                    color = AmberAccent,
                    backgroundColor = AmberAccent.copy(alpha = 0.18f),
                    borderColor = AmberAccent.copy(alpha = 0.35f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("search_distracting_apps_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = GlassBorderMedium,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isDistracting = app.isDistracting

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDistracting) AmberAccent.copy(alpha = 0.14f) else GlassSurfaceLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDistracting) AmberAccent.copy(alpha = 0.4f) else GlassBorderLow
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!app.isEssential) {
                                        onToggleDistracting(app.packageName, !isDistracting)
                                    }
                                }
                                .testTag("distracting_item_${app.packageName.replace(".", "_")}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AppIconView(
                                        packageName = app.packageName,
                                        appName = app.appName,
                                        size = 36.dp,
                                        isShieldActive = isDistracting && !app.isEssential,
                                        isEssential = app.isEssential,
                                        cornerRadius = 8.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = app.appName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (app.isEssential) "Essential App" else if (isDistracting) "Distracting App" else "Standard App",
                                            color = if (app.isEssential) EmeraldAccent else if (isDistracting) AmberAccent else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                LiquidGlassSwitch(
                                    checked = if (app.isEssential) false else isDistracting,
                                    enabled = !app.isEssential,
                                    onCheckedChange = { checked ->
                                        if (!app.isEssential) {
                                            onToggleDistracting(app.packageName, checked)
                                        }
                                    },
                                    activeColor = AmberAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            LiquidGlassDialogButton(
                onClick = onDismiss,
                accentColor = AmberAccent,
                modifier = Modifier.testTag("done_distracting_apps_btn")
            ) {
                Text("Done", color = AmberAccent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ActiveBlackoutCanvas(
    blackoutTotalSeconds: Int,
    blackoutSecondsRemaining: Int,
    isPomodoroRunning: Boolean,
    pomodoroSessionTag: String,
    essentialApps: List<com.rk.detachment.data.local.entities.AppLimitEntity>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestStop: () -> Unit,
    onOpenEssentialApp: (com.rk.detachment.data.local.entities.AppLimitEntity) -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        insetsController?.hide(WindowInsetsCompat.Type.systemBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val totalSecs = blackoutTotalSeconds.coerceAtLeast(1)
    val remainingSecs = blackoutSecondsRemaining
    val progress = (remainingSecs.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)

    val minutes = remainingSecs / 60
    val seconds = remainingSecs % 60

    var showExitConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PurplePrimary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = PurpleLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pomodoroSessionTag.uppercase(),
                            color = PurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                IconButton(
                    onClick = { showExitConfirmation = true },
                    modifier = Modifier.testTag("exit_blackout_top_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Blackout",
                        tint = TextSecondary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlowingProgressRing(
                    progress = progress,
                    modifier = Modifier.size(260.dp),
                    strokeWidth = 12.dp,
                    primaryColor = PurplePrimary,
                    secondaryColor = PurpleLight,
                    trackColor = Color(0x1AFFFFFF)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isPomodoroRunning) "DETACHMENT BLACKOUT ACTIVE" else "PAUSED",
                            color = if (isPomodoroRunning) PurpleLight else AmberAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPomodoroRunning) {
                        Surface(
                            shape = CircleShape,
                            color = GlassSurfaceMedium,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable { onPause() }
                                .testTag("pause_blackout_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pause,
                                    contentDescription = "Pause",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = PurplePrimary,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable { onResume() }
                                .testTag("resume_blackout_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACCESSIBLE ESSENTIAL APPS (${essentialApps.size}/10)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    essentialApps.take(5).forEach { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenEssentialApp(app) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .widthIn(min = 48.dp)
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.appName,
                                size = 42.dp,
                                isEssential = true,
                                cornerRadius = 12.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    essentialApps.drop(5).forEach { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenEssentialApp(app) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .widthIn(min = 48.dp)
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.appName,
                                size = 42.dp,
                                isEssential = true,
                                cornerRadius = 12.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (showExitConfirmation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .border(2.dp, PurplePrimary.copy(alpha = 0.85f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = FrostedBackgroundDarker,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "End Blackout Session?",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Are you sure you want to end this focus session early?",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LiquidGlassDialogButton(
                                onClick = { showExitConfirmation = false },
                                accentColor = PurpleLight,
                                modifier = Modifier.testTag("cancel_exit_blackout_btn")
                            ) {
                                Text(
                                    text = "Keep Focusing",
                                    color = PurpleLight
                                )
                            }
                            LiquidGlassHoldButton(
                                text = "End Session",
                                holdingText = "Hold to commit",
                                accentColor = RoseAccent,
                                holdDurationMillis = 3000L,
                                onAction = {
                                    showExitConfirmation = false
                                    onRequestStop()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("confirm_exit_blackout_btn")
                            )
                        }
                    }
                }
            }
        }
    }
}
