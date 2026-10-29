package com.rk.detachment.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.rk.detachment.data.local.entities.WebsiteLimitEntity
import com.rk.detachment.ui.components.CategoryBadge
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.LiquidGlassAlertDialog
import com.rk.detachment.ui.components.LiquidGlassDialogButton
import com.rk.detachment.ui.components.LiquidGlassSwitch
import com.rk.detachment.ui.components.PasscodeUnlockDialog
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
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
fun WebsiteLimitsScreen(
    uiState: DetachmentUiState,
    onNavigateBack: () -> Unit,
    onUpdateLimit: (String, Int, Boolean) -> Unit,
    onToggleLock: (String, Boolean) -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onUnlockWebsite: (String, Int) -> Unit,
    onRelockWebsite: (String) -> Unit,
    onAddWebsite: (String, String, String, Int) -> Unit,
    onDeleteWebsite: (String) -> Unit,
    onVerifyPin: (String) -> Boolean,
    onUpdateMasterPin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var editingWebsite by remember { mutableStateOf<WebsiteLimitEntity?>(null) }
    var unlockingWebsite by remember { mutableStateOf<WebsiteLimitEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("All", "Social", "Video", "Entertainment", "News", "Other")
    }

    val filteredWebsites = remember(uiState.allWebsites, searchQuery, selectedCategory) {
        uiState.allWebsites.filter { site ->
            val matchesCategory = selectedCategory == "All" || site.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    site.displayName.contains(searchQuery, ignoreCase = true) ||
                    site.domain.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    RadialGlassBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "website_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GlassSurfaceLow)
                                .testTag("website_limits_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Website Limits",
                                color = TextPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.allWebsites.size} Monitored Websites",
                                color = PurpleLight,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PurplePrimary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { showAddDialog = true }
                            .testTag("add_custom_website_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Website",
                                tint = PurpleLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Add",
                                color = PurpleLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item(key = "website_search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("website_search_field"),
                    placeholder = {
                        Text(
                            "Search ${uiState.allWebsites.size} websites...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PurpleLight
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
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = GlassBorderMedium,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }

            item(key = "website_category_chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories, key = { it }) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PurplePrimary else GlassSurfaceLow,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) PurplePrimary else GlassBorderLow
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

            items(filteredWebsites, key = { it.domain }) { website ->
                val isLocked = website.isCurrentlyLocked()
                val isTempUnlocked = website.isTemporaryUnlocked()
                val isExceeded = website.isLimitExceeded

                val targetBorderColor = when {
                    isLocked -> RoseAccent.copy(alpha = 0.50f)
                    isTempUnlocked -> EmeraldAccent.copy(alpha = 0.50f)
                    else -> GlassBorderLow
                }
                val animatedCardBorderColor by animateColorAsState(
                    targetValue = targetBorderColor,
                    animationSpec = tween(durationMillis = 350),
                    label = "website_card_border"
                )

                FrostedGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("website_card_${website.domain.replace(".", "_")}"),
                    backgroundColor = GlassSurfaceHigh,
                    borderColor = animatedCardBorderColor
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PurplePrimary.copy(alpha = 0.22f))
                                    .border(1.dp, PurpleLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = website.displayName.take(1).uppercase(),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = website.displayName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    CategoryBadge(
                                        text = website.category,
                                        color = PurpleLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Limit: ${if (website.dailyLimitMinutes > 0) "${website.dailyLimitMinutes}m" else "None"}${if (website.isHardMode && website.dailyLimitMinutes > 0) " (H)" else ""}",
                                        color = if (website.isHardMode && website.dailyLimitMinutes > 0) RoseAccent else TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${website.domain}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            LiquidGlassSwitch(
                                checked = isLocked,
                                onCheckedChange = { checked ->
                                    onToggleLock(website.domain, checked)
                                },
                                activeColor = RoseAccent
                            )
                        }

                        if (website.dailyLimitMinutes > 0) {
                            val prog = (website.usedTodayMinutes.toFloat() / website.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { prog },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isExceeded) RoseAccent else PurplePrimary,
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
                                modifier = Modifier.clickable { editingWebsite = website }
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
                                    border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { onRelockWebsite(website.domain) }
                                        .testTag("relock_btn_${website.domain.replace(".", "_")}")
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
                            } else if (isLocked && !website.isHardMode) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AmberAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { unlockingWebsite = website }
                                        .testTag("unlock_btn_${website.domain.replace(".", "_")}")
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
                            } else if (isLocked && website.isHardMode) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RoseAccent.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = RoseAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Hard Mode",
                                            color = RoseAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x18FFFFFF),
                                modifier = Modifier
                                    .clickable { onDeleteWebsite(website.domain) }
                                    .testTag("delete_btn_${website.domain.replace(".", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Remove",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingWebsite?.let { website ->
        var currentLimit by remember { mutableIntStateOf(website.dailyLimitMinutes) }
        var isHardMode by remember { mutableStateOf(website.isHardMode) }
        val quickPresets = remember { listOf(15, 30, 45, 60, 90, 120, 0) }

        LiquidGlassAlertDialog(
            onDismissRequest = { editingWebsite = null },
            accentColor = PurpleLight,
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
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PurplePrimary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = website.displayName.take(1).uppercase(),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = website.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Text(
                        text = if (currentLimit == 0) "No Limit" else "$currentLimit minutes per day",
                        color = PurpleLight,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Slider(
                        value = currentLimit.toFloat(),
                        onValueChange = { currentLimit = it.toInt() },
                        valueRange = 0f..180f,
                        steps = 35,
                        colors = SliderDefaults.colors(
                            thumbColor = PurplePrimary,
                            activeTrackColor = PurplePrimary,
                            inactiveTrackColor = GlassSurfaceMedium
                        )
                    )

                    Text("Quick Presets:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPresets.take(6).forEach { min ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentLimit == min) PurplePrimary else Color(0x22FFFFFF),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { currentLimit = min }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (min == 0) "Off" else "${min}m",
                                        color = if (currentLimit == min) Color.White else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isHardMode) RoseAccent.copy(alpha = 0.12f) else GlassSurfaceMedium)
                            .border(
                                1.dp,
                                if (isHardMode) RoseAccent.copy(alpha = 0.45f) else GlassBorderLow,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Hard mode",
                                color = if (isHardMode) RoseAccent else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "No unlocks allowed",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        LiquidGlassSwitch(
                            checked = isHardMode,
                            onCheckedChange = { isHardMode = it },
                            activeColor = RoseAccent,
                            testTag = "hard_mode_switch"
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LiquidGlassDialogButton(
                        onClick = {
                            onUpdateLimit(website.domain, 0, false)
                            editingWebsite = null
                        },
                        accentColor = RoseAccent,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("Clear", color = RoseAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    LiquidGlassDialogButton(
                        onClick = {
                            onUpdateLimit(website.domain, currentLimit, isHardMode)
                            editingWebsite = null
                        },
                        accentColor = PurplePrimary,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text("Save", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    if (showAddDialog) {
        var inputDomain by remember { mutableStateOf("") }
        var inputName by remember { mutableStateOf("") }
        var inputCategory by remember { mutableStateOf("Social") }

        LiquidGlassAlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Website Domain",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = inputDomain,
                        onValueChange = { inputDomain = it },
                        label = { Text("Domain (e.g., pinterest.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleLight,
                            unfocusedBorderColor = GlassBorderMedium,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Display Name (e.g., Pinterest)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleLight,
                            unfocusedBorderColor = GlassBorderMedium,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text(text = "Category", color = TextSecondary, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Social", "Video", "Entertainment", "News", "Other")) { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (inputCategory == cat) PurplePrimary else GlassSurfaceMedium,
                                border = BorderStroke(1.dp, if (inputCategory == cat) PurpleLight else GlassBorderMedium),
                                modifier = Modifier.clickable { inputCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (inputCategory == cat) Color.White else TextPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiquidGlassDialogButton(
                        onClick = { showAddDialog = false },
                        accentColor = GlassBorderMedium,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    LiquidGlassDialogButton(
                        onClick = {
                            if (inputDomain.isNotBlank()) {
                                onAddWebsite(inputDomain, inputName, inputCategory, 0)
                                showAddDialog = false
                            }
                        },
                        accentColor = PurplePrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    unlockingWebsite?.let { website ->
        PasscodeUnlockDialog(
            appName = website.displayName,
            unlockMinutes = uiState.unlockMinutes,
            pinLength = if (uiState.masterPin.length == 6) 6 else 4,
            onVerifyPin = onVerifyPin,
            onUnlockSuccess = { chosenMinutes ->
                onUnlockWebsite(website.domain, chosenMinutes)
                unlockingWebsite = null
            },
            onDismiss = { unlockingWebsite = null }
        )
    }
}
