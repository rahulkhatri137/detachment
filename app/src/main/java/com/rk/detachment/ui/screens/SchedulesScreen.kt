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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.IndigoSoft
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.viewmodel.DetachmentUiState

@Composable
fun SchedulesScreen(
    uiState: DetachmentUiState,
    onNavigateToBlackout: () -> Unit,
    onToggleRule: (Int, Boolean) -> Unit,
    onSaveRule: (ScheduleRuleEntity) -> Unit,
    onDeleteRule: (ScheduleRuleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<ScheduleRuleEntity?>(null) }

    RadialGlassBackground(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Focus Schedules",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automatic app locking during study, work, or sleep hours",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                val activeCount = uiState.activeSchedules.size
                item {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (activeCount > 0) IndigoPrimary.copy(alpha = 0.2f) else GlassSurfaceMedium,
                        borderColor = if (activeCount > 0) IndigoLight else GlassBorderMedium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (activeCount > 0) IndigoPrimary else Color(0x331E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (activeCount > 0) Icons.Default.Lock else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (activeCount > 0) Color.White else TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = if (activeCount > 0) "$activeCount Schedule Rule Currently Active" else "No Schedule Active Right Now",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (activeCount > 0) "Automatically enforcing app block rules" else "Schedules will trigger automatically based on time",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = IndigoLight.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = IndigoLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Note: Essential apps (Whitelisted) & Distracting apps (Blocked) can be customized anytime in the Blackout screen.",
                                        color = TextPrimary,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.clickable(onClick = onNavigateToBlackout)
                                    )
                                }
                            }
                        }
                    }
                }

                items(uiState.scheduleRules) { rule ->
                    val isRuleActiveNow = rule.isCurrentlyActive()

                    FrostedGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_card_${rule.id}"),
                        borderColor = if (isRuleActiveNow) IndigoLight else GlassBorderMedium,
                        backgroundColor = if (isRuleActiveNow) IndigoPrimary.copy(alpha = 0.25f) else GlassSurfaceHigh
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                when (rule.type) {
                                                    "STUDY" -> IndigoPrimary.copy(alpha = 0.2f)
                                                    "SLEEP" -> IndigoSoft.copy(alpha = 0.2f)
                                                    else -> AmberAccent.copy(alpha = 0.2f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (rule.type) {
                                                "STUDY" -> Icons.Default.School
                                                "SLEEP" -> Icons.Default.Brightness2
                                                else -> Icons.Default.Work
                                            },
                                            contentDescription = null,
                                            tint = when (rule.type) {
                                                "STUDY" -> IndigoLight
                                                "SLEEP" -> IndigoSoft
                                                else -> AmberAccent
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = rule.title,
                                                color = TextPrimary,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isRuleActiveNow) {
                                                FrostedBadge(text = "ACTIVE", color = IndigoLight)
                                            }
                                        }
                                        Text(
                                            text = rule.formattedTimeRange(),
                                            color = IndigoLight,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Switch(
                                    checked = rule.isEnabled,
                                    onCheckedChange = { checked ->
                                        onToggleRule(rule.id, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = IndigoLight,
                                        checkedTrackColor = IndigoPrimary.copy(alpha = 0.5f),
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = Color(0x331E293B)
                                    ),
                                    modifier = Modifier.testTag("toggle_rule_${rule.id}")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                                    days.forEach { d ->
                                        val isDayActive = rule.activeDays.contains("ALL") || rule.activeDays.contains(d)
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isDayActive) IndigoPrimary.copy(alpha = 0.3f)
                                                    else Color(0x22FFFFFF)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isDayActive) IndigoLight else Color.Transparent,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = d.take(1),
                                                color = if (isDayActive) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x22FFFFFF)
                                ) {
                                    Text(
                                        text = if (rule.blockedTarget == "DISTRACTING") "Blocks Distracting" else "Blocks Non-Essential",
                                        color = TextSecondary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { editingRule = rule },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteRule(rule) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseAccent.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_schedule_fab"),
                containerColor = IndigoPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }

        if (showAddDialog || editingRule != null) {
            val isEdit = editingRule != null
            var title by remember { mutableStateOf(editingRule?.title ?: "New Focus Block") }
            var type by remember { mutableStateOf(editingRule?.type ?: "STUDY") }
            var startH by remember { mutableIntStateOf(editingRule?.startHour ?: 9) }
            var startM by remember { mutableIntStateOf(editingRule?.startMinute ?: 0) }
            var endH by remember { mutableIntStateOf(editingRule?.endHour ?: 17) }
            var endM by remember { mutableIntStateOf(editingRule?.endMinute ?: 0) }
            var blockedTarget by remember { mutableStateOf(editingRule?.blockedTarget ?: "DISTRACTING") }

            var showStartTimePicker by remember { mutableStateOf(false) }
            var showEndTimePicker by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    editingRule = null
                },
                containerColor = FrostedBackgroundDarker,
                title = {
                    Text(
                        text = if (isEdit) "Edit Focus Schedule" else "Create Focus Schedule",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Schedule Name", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoLight,
                                unfocusedBorderColor = GlassBorderMedium,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("STUDY", "SLEEP", "WORK").forEach { t ->
                                val isSelected = type == t
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) IndigoPrimary else Color(0x22FFFFFF),
                                    modifier = Modifier.clickable { type = t }
                                ) {
                                    Text(
                                        text = t,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Time Range (Tap to select with clock):",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = IndigoPrimary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoLight.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showStartTimePicker = true }
                                        .testTag("schedule_start_time_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "START",
                                                color = IndigoLight,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Change Start Time",
                                                tint = IndigoLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatScheduleTime(startH, startM),
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format("%02d:%02d 24h", startH, startM),
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                Text("→", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = IndigoSoft.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSoft.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showEndTimePicker = true }
                                        .testTag("schedule_end_time_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "END",
                                                color = IndigoSoft,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = "Change End Time",
                                                tint = IndigoSoft,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatScheduleTime(endH, endM),
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = String.format("%02d:%02d 24h", endH, endM),
                                            color = TextSecondary,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Apps to Lock:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (blockedTarget == "DISTRACTING") IndigoPrimary.copy(alpha = 0.15f) else Color(0x0DFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (blockedTarget == "DISTRACTING") IndigoLight.copy(alpha = 0.4f) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { blockedTarget = "DISTRACTING" }
                                        .testTag("target_distracting_row")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = blockedTarget == "DISTRACTING",
                                            onClick = { blockedTarget = "DISTRACTING" },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = IndigoLight,
                                                unselectedColor = TextSecondary
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "Distracting apps only",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (blockedTarget == "DISTRACTING") FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "Locks apps marked as Distracting",
                                                color = TextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (blockedTarget == "ALL_NON_ESSENTIAL") IndigoPrimary.copy(alpha = 0.15f) else Color(0x0DFFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (blockedTarget == "ALL_NON_ESSENTIAL") IndigoLight.copy(alpha = 0.4f) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { blockedTarget = "ALL_NON_ESSENTIAL" }
                                        .testTag("target_non_essential_row")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        RadioButton(
                                            selected = blockedTarget == "ALL_NON_ESSENTIAL",
                                            onClick = { blockedTarget = "ALL_NON_ESSENTIAL" },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = IndigoPrimary,
                                                unselectedColor = TextSecondary
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "All non-essential apps",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (blockedTarget == "ALL_NON_ESSENTIAL") FontWeight.SemiBold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "Locks all apps except Essential whitelist",
                                                color = TextSecondary,
                                                fontSize = 10.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newRule = ScheduleRuleEntity(
                                id = editingRule?.id ?: 0,
                                title = title,
                                type = type,
                                startHour = startH,
                                startMinute = startM,
                                endHour = endH,
                                endMinute = endM,
                                activeDays = "MON,TUE,WED,THU,FRI,SAT,SUN",
                                isEnabled = true,
                                blockedTarget = blockedTarget
                            )
                            onSaveRule(newRule)
                            showAddDialog = false
                            editingRule = null
                        }
                    ) {
                        Text("Save Rule", color = IndigoLight, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            editingRule = null
                        }
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )

            if (showStartTimePicker) {
                RoundClockTimePickerDialog(
                    title = "Select Start Time",
                    initialHour = startH,
                    initialMinute = startM,
                    onConfirm = { h, m ->
                        startH = h
                        startM = m
                        showStartTimePicker = false
                    },
                    onDismiss = { showStartTimePicker = false }
                )
            }

            if (showEndTimePicker) {
                RoundClockTimePickerDialog(
                    title = "Select End Time",
                    initialHour = endH,
                    initialMinute = endM,
                    onConfirm = { h, m ->
                        endH = h
                        endM = m
                        showEndTimePicker = false
                    },
                    onDismiss = { showEndTimePicker = false }
                )
            }
        }
    }
}

private fun formatScheduleTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when (hour % 12) {
        0 -> 12
        else -> hour % 12
    }
    return String.format("%d:%02d %s", displayHour, minute, period)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundClockTimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FrostedBackgroundDarker,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = IndigoLight,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = GlassSurfaceHigh,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = TextPrimary,
                        selectorColor = IndigoPrimary,
                        containerColor = Color.Transparent,
                        periodSelectorBorderColor = IndigoLight.copy(alpha = 0.5f),
                        periodSelectorSelectedContainerColor = IndigoPrimary,
                        periodSelectorUnselectedContainerColor = GlassSurfaceMedium,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = TextSecondary,
                        timeSelectorSelectedContainerColor = IndigoPrimary.copy(alpha = 0.35f),
                        timeSelectorUnselectedContainerColor = GlassSurfaceMedium,
                        timeSelectorSelectedContentColor = IndigoLight,
                        timeSelectorUnselectedContentColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                },
                modifier = Modifier.testTag("confirm_time_picker_btn")
            ) {
                Text("Set Time", color = IndigoLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_time_picker_btn")
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}