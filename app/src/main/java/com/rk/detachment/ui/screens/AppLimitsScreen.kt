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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.CategoryBadge
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.PasscodeUnlockDialog
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun AppLimitsScreen(
    uiState: DetachmentUiState,
    onUpdateLimit: (String, Int) -> Unit,
    onToggleLock: (String, Boolean) -> Unit,
    onUnlock15Min: (String) -> Unit,
    onUnlockApp: (String, Int) -> Unit = { pkg, min -> onUnlock15Min(pkg) },
    onSetUnlockMinutes: (Int) -> Unit = {},
    onRelockApp: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onUpdateMasterPin: (String) -> Unit,
    onLaunchApp: (AppLimitEntity) -> Unit,
    onRefreshApps: () -> Unit = {},
    onOpenUsageSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var editingApp by remember { mutableStateOf<AppLimitEntity?>(null) }
    var unlockingApp by remember { mutableStateOf<AppLimitEntity?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("All", "Social", "Video", "Entertainment", "Games", "Communication", "Productivity", "Utilities")
    }

    val filteredApps = remember(uiState.allApps, searchQuery, selectedCategory) {
        uiState.allApps.filter { app ->
            val matchesCategory = (selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() || app.appName.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    RadialGlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "header_item") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Limits",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.allApps.size} Installed Apps",
                            color = IndigoLight,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRefreshApps,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassSurfaceLow)
                                .testTag("refresh_apps_btn")
                        ) {
                            if (uiState.isSyncingApps) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = IndigoLight,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Apps",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = IndigoPrimary.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { showChangePinDialog = true }
                                .testTag("change_pin_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Security PIN",
                                    tint = IndigoLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "PIN",
                                    color = IndigoLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (!uiState.hasUsagePermission) {
                item(key = "usage_permission_banner") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AmberAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usage Access Required",
                                    color = AmberAccent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enable Usage Access to see live screen time for your installed apps.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onOpenUsageSettings,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item(key = "unlock_duration_selector_card") {
                var dropdownExpanded by remember { mutableStateOf(false) }
                val currentUnlockMinutes = uiState.unlockMinutes
                val unlockOptions = listOf(
                    10 to "10 Minutes (Quick Window)",
                    15 to "15 Minutes (Standard • Default)",
                    30 to "30 Minutes (Extended Session)",
                    60 to "60 Minutes (1 Hour)"
                )

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("unlock_selector_card"),
                    backgroundColor = GlassSurfaceHigh,
                    borderColor = IndigoLight.copy(alpha = 0.5f)
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
                                    imageVector = Icons.Default.LockClock,
                                    contentDescription = null,
                                    tint = IndigoLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Unlock Time Period",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Default time period for unlock button",
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
                                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { dropdownExpanded = true }
                                    .testTag("unlock_duration_dropdown_anchor")
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
                                                .background(IndigoPrimary.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${currentUnlockMinutes}m",
                                                color = IndigoLight,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = unlockOptions.find { it.first == currentUnlockMinutes }?.second
                                                ?: "$currentUnlockMinutes Minutes",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select unlock period",
                                        tint = IndigoLight,
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
                                    .testTag("unlock_duration_dropdown_menu")
                            ) {
                                unlockOptions.forEach { (minutes, label) ->
                                    val isSelected = currentUnlockMinutes == minutes
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) IndigoLight else TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = IndigoLight,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onSetUnlockMinutes(minutes)
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("unlock_option_$minutes")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "search_bar_item") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_search_field"),
                    placeholder = { Text("Search ${uiState.allApps.size} installed apps...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = IndigoLight
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassSurfaceMedium,
                        unfocusedContainerColor = GlassSurfaceMedium,
                        focusedBorderColor = IndigoLight,
                        unfocusedBorderColor = GlassBorderMedium,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            item(key = "category_chips_item") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories, key = { it }) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) IndigoPrimary else GlassSurfaceLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) IndigoPrimary else GlassBorderLow
                            ),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            items(filteredApps, key = { it.packageName }) { app ->
                val isLocked = app.isCurrentlyLocked()
                val isTempUnlocked = app.isTemporaryUnlocked()
                val isExceeded = app.isLimitExceeded

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("limit_card_${app.packageName.replace(".", "_")}"),
                    backgroundColor = GlassSurfaceHigh
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconView(
                                packageName = app.packageName,
                                appName = app.appName,
                                size = 44.dp,
                                isLocked = isLocked,
                                cornerRadius = 12.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = app.appName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    CategoryBadge(
                                        text = app.category,
                                        color = IndigoLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Limit: ${if (app.dailyLimitMinutes > 0) "${app.dailyLimitMinutes}m" else "None"}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Used: ${app.usedTodayMinutes}m today",
                                        color = if (isExceeded) RoseAccent else if (app.usedTodayMinutes > 0) EmeraldAccent else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (app.usedTodayMinutes > 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }

                            Switch(
                                checked = isLocked,
                                onCheckedChange = { checked ->
                                    onToggleLock(app.packageName, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RoseAccent,
                                    checkedTrackColor = RoseAccent.copy(alpha = 0.35f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = Color(0x331E293B)
                                )
                            )
                        }

                        if (app.dailyLimitMinutes > 0) {
                            val prog = (app.usedTodayMinutes.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { prog },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isExceeded) RoseAccent else IndigoPrimary,
                                trackColor = Color(0x22FFFFFF)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x18FFFFFF),
                                modifier = Modifier.clickable { editingApp = app }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Time Limit",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Time Limit",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isTempUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldAccent.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { onRelockApp(app.packageName) }
                                        .testTag("relock_btn_${app.packageName.replace(".", "_")}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LockClock,
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Paused • Relock",
                                            color = EmeraldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (isLocked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberAccent.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { unlockingApp = app }
                                        .testTag("unlock_btn_${app.packageName.replace(".", "_")}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = AmberAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Unlock (${uiState.unlockMinutes}m)",
                                            color = AmberAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IndigoPrimary.copy(alpha = 0.25f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable { onLaunchApp(app) }
                                    .testTag("launch_app_${app.packageName.replace(".", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = IndigoLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Launch",
                                        color = IndigoLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        editingApp?.let { app ->
            var currentLimit by remember { mutableIntStateOf(app.dailyLimitMinutes) }
            val quickPresets = remember { listOf(15, 30, 45, 60, 90, 120, 0) }

            AlertDialog(
                onDismissRequest = { editingApp = null },
                containerColor = FrostedBackgroundDarker,
                title = {
                    Text(
                        text = "Daily Screen Time Limit",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIconView(packageName = app.packageName, appName = app.appName, size = 32.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = app.appName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }

                        Text(
                            text = if (currentLimit == 0) "No Limit (Unlimited)" else "$currentLimit minutes per day",
                            color = IndigoLight,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Slider(
                            value = currentLimit.toFloat(),
                            onValueChange = { currentLimit = it.toInt() },
                            valueRange = 0f..180f,
                            steps = 35,
                            colors = SliderDefaults.colors(
                                thumbColor = IndigoPrimary,
                                activeTrackColor = IndigoPrimary,
                                inactiveTrackColor = Color(0x33FFFFFF)
                            )
                        )

                        Text("Quick Presets:", color = TextSecondary, fontSize = 12.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickPresets.take(5).forEach { min ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (currentLimit == min) IndigoPrimary else Color(0x22FFFFFF),
                                    modifier = Modifier.clickable { currentLimit = min }
                                ) {
                                    Text(
                                        text = if (min == 0) "Off" else "${min}m",
                                        color = if (currentLimit == min) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUpdateLimit(app.packageName, currentLimit)
                            editingApp = null
                        }
                    ) {
                        Text("Save Limit", color = IndigoLight, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingApp = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        unlockingApp?.let { app ->
            PasscodeUnlockDialog(
                appName = app.appName,
                unlockMinutes = uiState.unlockMinutes,
                onDismiss = { unlockingApp = null },
                onVerifyPin = onVerifyPin,
                onUnlockSuccess = {
                    onUnlockApp(app.packageName, uiState.unlockMinutes)
                    unlockingApp = null
                }
            )
        }

        if (showChangePinDialog) {
            var currentPinInput by remember { mutableStateOf("") }
            var newPinInput by remember { mutableStateOf("") }
            var confirmPinInput by remember { mutableStateOf("") }
            var isCurrentPinVerified by remember { mutableStateOf(uiState.masterPin.isBlank()) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { showChangePinDialog = false },
                containerColor = FrostedBackgroundDarker,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!isCurrentPinVerified) "Verify Current PIN" else "Set New Master PIN",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isCurrentPinVerified) {
                            Text(
                                text = "Enter your current 4-digit Master PIN to authorize. Default - 1234",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = currentPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        currentPinInput = it
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("Current 4-digit PIN", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("current_pin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoLight,
                                    unfocusedBorderColor = GlassBorderMedium,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        } else {
                            Text(
                                text = "Choose a new 4-digit PIN for emergency unlocks.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = newPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        newPinInput = it
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("New 4-digit PIN", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_pin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoLight,
                                    unfocusedBorderColor = GlassBorderMedium,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = confirmPinInput,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                        confirmPinInput = it
                                        errorMessage = null
                                    }
                                },
                                placeholder = { Text("Confirm New PIN", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_pin_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoLight,
                                    unfocusedBorderColor = GlassBorderMedium,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = RoseAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    if (!isCurrentPinVerified) {
                        TextButton(
                            onClick = {
                                if (onVerifyPin(currentPinInput)) {
                                    isCurrentPinVerified = true
                                    errorMessage = null
                                } else {
                                    errorMessage = "Incorrect current PIN. Please try again."
                                }
                            },
                            enabled = currentPinInput.length == 4,
                            modifier = Modifier.testTag("verify_current_pin_btn")
                        ) {
                            Text("Verify PIN", color = IndigoLight, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(
                            onClick = {
                                if (newPinInput.length == 4) {
                                    if (newPinInput != confirmPinInput) {
                                        errorMessage = "PINs do not match."
                                    } else {
                                        onUpdateMasterPin(newPinInput)
                                        showChangePinDialog = false
                                    }
                                }
                            },
                            enabled = newPinInput.length == 4 && confirmPinInput.length == 4,
                            modifier = Modifier.testTag("save_new_pin_btn")
                        ) {
                            Text("Save New PIN", color = IndigoLight, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangePinDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

