package com.rk.detachment.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassButton
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.GlowingProgressRing
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun BlackoutPomodoroScreen(
    uiState: DetachmentUiState,
    onStartBlackout: (Int, String) -> Unit,
    onPauseBlackout: () -> Unit,
    onResumeBlackout: () -> Unit,
    onStopBlackout: () -> Unit,
    onToggleEssential: (String, Boolean) -> Unit,
    onToggleDistracting: (String, Boolean) -> Unit,
    onOpenEssentialApp: (AppLimitEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDurationMinutes by remember { mutableIntStateOf(25) }
    var selectedTag by remember { mutableStateOf("Deep Work") }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showEssentialAppsDialog by remember { mutableStateOf(false) }
    var showDistractingAppsDialog by remember { mutableStateOf(false) }

    val durations = listOf(15, 25, 45, 60, 90)
    val tags = listOf("Deep Work", "Study", "Reading", "Meditation", "Code")

    if (uiState.isBlackoutActive) {
        ActiveBlackoutCanvas(
            uiState = uiState,
            onPause = onPauseBlackout,
            onResume = onResumeBlackout,
            onRequestStop = { showExitConfirmDialog = true },
            onOpenEssentialApp = onOpenEssentialApp
        )

        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                containerColor = FrostedBackgroundDarker,
                title = {
                    Text("Exit Pomodoro Blackout?", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Exiting now will end your Detachment blackout session early and unlock all apps.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitConfirmDialog = false
                            onStopBlackout()
                        }
                    ) {
                        Text("Exit Blackout", color = RoseAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text("Keep Focusing", color = IndigoLight)
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
                contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            fontSize = 12.sp
                        )
                    }
                }

                item(key = "durations") {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassSurfaceHigh) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "FOCUS DURATION",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                durations.forEach { min ->
                                    val isSelected = selectedDurationMinutes == min
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) IndigoPrimary else GlassSurfaceLow,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) IndigoPrimary else GlassBorderLow
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
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "activity_tags") {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = GlassSurfaceHigh) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "FOCUS ACTIVITY",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tags) { tag ->
                                    val isSelected = selectedTag == tag
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) IndigoPrimary else GlassSurfaceLow,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) IndigoPrimary else GlassBorderLow
                                        ),
                                        modifier = Modifier.clickable { selectedTag = tag }
                                    ) {
                                        Text(
                                            text = tag,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "start_btn") {
                    FrostedGlassButton(
                        text = "Initiate Pomodoro Blackout",
                        onClick = {
                            onStartBlackout(selectedDurationMinutes, selectedTag)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Timer,
                        isPrimary = true,
                        testTag = "start_blackout_btn"
                    )
                }

                item(key = "rules_section_header") {
                    Text(
                        text = "FOCUS APPLICATION RULES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                    )
                }

                val essentialCount = uiState.essentialApps.size
                item(key = "select_essential_btn") {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEssentialAppsDialog = true }
                            .testTag("select_essential_apps_btn"),
                        backgroundColor = GlassSurfaceHigh,
                        borderColor = if (essentialCount > 0) EmeraldAccent.copy(alpha = 0.35f) else GlassBorderLow
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldAccent.copy(alpha = 0.18f))
                                        .border(1.dp, EmeraldAccent.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Essential Apps (Whitelist)",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$essentialCount / 10 Allowed in Blackout",
                                        color = if (essentialCount > 0) EmeraldAccent else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FrostedBadge(
                                    text = "$essentialCount / 10",
                                    color = if (essentialCount == 10) EmeraldAccent else IndigoLight,
                                    backgroundColor = (if (essentialCount == 10) EmeraldAccent else IndigoPrimary).copy(alpha = 0.18f),
                                    borderColor = (if (essentialCount == 10) EmeraldAccent else IndigoPrimary).copy(alpha = 0.35f)
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

                val distractingCount = uiState.distractingApps.size
                item(key = "select_distracting_btn") {
                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDistractingAppsDialog = true }
                            .testTag("select_distracting_apps_btn"),
                        backgroundColor = GlassSurfaceHigh,
                        borderColor = if (distractingCount > 0) AmberAccent.copy(alpha = 0.35f) else GlassBorderLow
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(AmberAccent.copy(alpha = 0.18f))
                                        .border(1.dp, AmberAccent.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Distracting Apps (Blocklist)",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$distractingCount Apps Distraction Delayed",
                                        color = if (distractingCount > 0) AmberAccent else TextSecondary,
                                        fontSize = 12.sp
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
            }
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
    uiState: DetachmentUiState,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedBackgroundDarker,
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
                        fontSize = 11.sp
                    )
                }
                FrostedBadge(
                    text = "$essentialCount / 10",
                    color = if (essentialCount == 10) EmeraldAccent else IndigoLight,
                    backgroundColor = (if (essentialCount == 10) EmeraldAccent else IndigoPrimary).copy(alpha = 0.18f),
                    borderColor = (if (essentialCount == 10) EmeraldAccent else IndigoPrimary).copy(alpha = 0.40f)
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
                        focusedBorderColor = IndigoLight,
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
                                            text = if (isEssential) "Allowed during blackout" else "${app.usedTodayMinutes}m used today",
                                            color = if (isEssential) EmeraldAccent else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isEssential,
                                    onCheckedChange = { checked ->
                                        if (checked && essentialCount >= 10 && !isEssential) {
                                        } else {
                                            onToggleEssential(app.packageName, checked)
                                        }
                                    },
                                    enabled = canSelect,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = EmeraldAccent,
                                        checkmarkColor = Color.Black,
                                        uncheckedColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("done_essential_apps_btn")
            ) {
                Text("Done", color = IndigoLight, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DistractingAppsDialog(
    uiState: DetachmentUiState,
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedBackgroundDarker,
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
                        text = "Shielded apps with mindful friction delay",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                FrostedBadge(
                    text = "$distractingCount APPS",
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
                                    onToggleDistracting(app.packageName, !isDistracting)
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
                                            text = if (isDistracting) "Distraction shield active" else "Direct instant launch",
                                            color = if (isDistracting) AmberAccent else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isDistracting,
                                    onCheckedChange = { checked ->
                                        onToggleDistracting(app.packageName, checked)
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
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("done_distracting_apps_btn")
            ) {
                Text("Done", color = AmberAccent, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ActiveBlackoutCanvas(
    uiState: DetachmentUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestStop: () -> Unit,
    onOpenEssentialApp: (AppLimitEntity) -> Unit
) {
    val totalSecs = uiState.blackoutTotalSeconds.coerceAtLeast(1)
    val remainingSecs = uiState.blackoutSecondsRemaining
    val progress = (remainingSecs.toFloat() / totalSecs.toFloat()).coerceIn(0f, 1f)

    val minutes = remainingSecs / 60
    val seconds = remainingSecs % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IndigoPrimary.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.pomodoroSessionTag.uppercase(),
                            color = IndigoLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                IconButton(
                    onClick = onRequestStop,
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
                    modifier = Modifier.size(280.dp),
                    strokeWidth = 12.dp,
                    primaryColor = IndigoPrimary,
                    secondaryColor = IndigoLight,
                    trackColor = Color(0x1AFFFFFF)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (uiState.isPomodoroRunning) "DETACHMENT BLACKOUT ACTIVE" else "PAUSED",
                            color = if (uiState.isPomodoroRunning) IndigoLight else AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.isPomodoroRunning) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x331E293B),
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
                            color = IndigoPrimary,
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
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACCESSIBLE ESSENTIAL APPS (${uiState.essentialApps.size}/10)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.essentialApps.take(5).forEach { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpenEssentialApp(app) }
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.appName,
                                size = 42.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.essentialApps.drop(5).forEach { app ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onOpenEssentialApp(app) }
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.appName,
                                size = 42.dp,
                                cornerRadius = 14.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
