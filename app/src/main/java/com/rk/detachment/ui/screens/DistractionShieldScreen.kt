package com.rk.detachment.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun DistractionShieldScreen(
    uiState: DetachmentUiState,
    onToggleShieldActive: (String, Boolean) -> Unit,
    onSetDelaySeconds: (Int) -> Unit = {},
    onToggleDelayForDistractingApps: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val shieldedCount = uiState.shieldActiveApps.size
    val currentDelay = uiState.delaySeconds

    RadialGlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "distraction_header") {
                Column {
                    Text(
                        text = "Distraction Shield",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Adds mindful friction delay and quotes before opening shielded apps",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            item(key = "delay_selector_card") {
                var dropdownExpanded by remember { mutableStateOf(false) }
                val delayOptions = listOf(
                    10 to "10 Seconds (Quick Pause)",
                    15 to "15 Seconds (Standard • Default)",
                    20 to "20 Seconds (Deeper Restraint)",
                    30 to "30 Seconds (Maximum Reflection)"
                )

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delay_selector_card"),
                    backgroundColor = GlassSurfaceHigh,
                    borderColor = AmberAccent.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Friction Delay Duration",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pause before opening shielded apps",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = FrostedBackgroundDarker,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                                    .testTag("delay_duration_dropdown_anchor")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(AmberAccent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${currentDelay}s",
                                                color = AmberAccent,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = delayOptions.find { it.first == currentDelay }?.second
                                                ?: "$currentDelay Seconds",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select delay duration",
                                        tint = AmberAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .background(FrostedBackgroundDarker)
                                    .border(1.dp, GlassBorderHigh, RoundedCornerShape(12.dp))
                                    .testTag("delay_duration_dropdown_menu")
                            ) {
                                delayOptions.forEach { (seconds, label) ->
                                    val isSelected = currentDelay == seconds
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) AmberAccent else TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = AmberAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onSetDelaySeconds(seconds)
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("delay_option_$seconds")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "stats_hero_card") {
                FrostedGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GlassSurfaceHigh,
                    borderColor = AmberAccent.copy(alpha = 0.40f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currentDelay}-SECOND DETACHMENT INTERCEPT",
                                    color = AmberAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${uiState.distractionsResistedCount} Distractions Resisted",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Every ${currentDelay}s delay gives your mind space to choose with intention",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AmberAccent.copy(alpha = 0.15f))
                                .border(1.dp, AmberAccent.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            item(key = "section_title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrostedBadge(
                        text = "Shielded Apps ${shieldedCount}",
                        color = AmberAccent,
                        backgroundColor = AmberAccent.copy(alpha = 0.20f),
                        borderColor = AmberAccent.copy(alpha = 0.35f)
                    )

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleDelayForDistractingApps(!uiState.isDelayForDistractingApps) }
                            .testTag("delay_for_distracting_badge_toggle"),
                        shape = RoundedCornerShape(6.dp),
                        color = if (uiState.isDelayForDistractingApps) AmberAccent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(
                            0.8.dp,
                            if (uiState.isDelayForDistractingApps) AmberAccent.copy(alpha = 0.35f) else GlassBorderHigh.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
                        ) {
                            Text(
                                text = "Delay Distracting Apps",
                                color = if (uiState.isDelayForDistractingApps) AmberAccent else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier.size(width = 30.dp, height = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Switch(
                                    checked = uiState.isDelayForDistractingApps,
                                    onCheckedChange = { onToggleDelayForDistractingApps(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AmberAccent,
                                        checkedTrackColor = AmberAccent.copy(alpha = 0.45f),
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = Color(0x331E293B)
                                    ),
                                    modifier = Modifier
                                        .scale(0.55f)
                                        .testTag("delay_for_distracting_switch")
                                )
                            }
                        }
                    }
                }
            }

            items(uiState.allApps, key = { it.packageName }) { app ->
                val isFromDistracting = uiState.isDelayForDistractingApps && app.isDistracting
                val isShieldActive = app.isShieldActive || isFromDistracting

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("distraction_row_${app.packageName.replace(".", "_")}"),
                    backgroundColor = GlassSurfaceHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                size = 42.dp,
                                cornerRadius = 12.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = app.appName,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isShieldActive) {
                                        if (isFromDistracting && !app.isShieldActive) "${currentDelay}s Delay (Blackout Distracting)"
                                        else "${currentDelay}s Mindful Friction Delay"
                                    } else "Instant direct launch",
                                    color = if (isShieldActive) AmberAccent else TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isShieldActive,
                            onCheckedChange = { checked ->
                                onToggleShieldActive(app.packageName, checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AmberAccent,
                                checkedTrackColor = AmberAccent.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0x331E293B)
                            )
                        )
                    }
                }
            }
        }
    }
}

