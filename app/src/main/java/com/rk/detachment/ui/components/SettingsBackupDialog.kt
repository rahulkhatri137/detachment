package com.rk.detachment.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rk.detachment.data.model.ImportSummary
import com.rk.detachment.ui.components.LiquidGlassAlertDialog
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary

@Composable
fun SettingsBackupDialog(
    onDismiss: () -> Unit,
    onExport: ((String) -> Unit, (String) -> Unit) -> Unit,
    onImport: (String, (Result<ImportSummary>) -> Unit) -> Unit,
    appLimitsCount: Int,
    schedulesCount: Int
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var exportJson by remember { mutableStateOf("") }
    var isExportLoading by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }
    var isCopied by remember { mutableStateOf(false) }

    var importJsonText by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importResultText by remember { mutableStateOf<String?>(null) }
    var isImportSuccess by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (!content.isNullOrBlank()) {
                    importJsonText = content
                    importResultText = null
                    isImporting = true
                    onImport(content) { result ->
                        isImporting = false
                        result.onSuccess { summary ->
                            isImportSuccess = true
                            importResultText = "Restored ${summary.appsUpdated} apps, ${summary.schedulesRestored} schedules, and settings!"
                        }.onFailure { err ->
                            isImportSuccess = false
                            importResultText = "Import error: ${err.message ?: "Invalid JSON"}"
                        }
                    }
                }
            } catch (e: Exception) {
                isImportSuccess = false
                importResultText = "Error reading file: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        isExportLoading = true
        onExport(
            { json ->
                exportJson = json
                isExportLoading = false
            },
            { err ->
                exportError = err
                isExportLoading = false
            }
        )
    }

    LiquidGlassAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = null,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("settings_backup_dialog"),
        accentColor = PurpleLight,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "App Backup & Restore",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Export and import your Detachment settings",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurfaceLow)
                        .border(1.dp, GlassBorderLow, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) PurplePrimary.copy(alpha = 0.35f) else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = if (selectedTab == 0) PurpleLight else TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export",
                                color = if (selectedTab == 0) TextPrimary else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) PurplePrimary.copy(alpha = 0.35f) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = if (selectedTab == 1) PurpleLight else TextMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Import",
                                color = if (selectedTab == 1) TextPrimary else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = GlassSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderLow)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "$appLimitsCount", color = PurpleLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "App Limits", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = GlassSurfaceHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderLow)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "$schedulesCount", color = EmeraldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Schedules", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (exportJson.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x22000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderLow)
                        ) {
                            Text(
                                text = exportJson,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LiquidGlassDialogButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Detachment Settings Backup", exportJson)
                                    clipboard.setPrimaryClip(clip)
                                    isCopied = true
                                },
                                modifier = Modifier.weight(1f),
                                accentColor = if (isCopied) EmeraldAccent else PurpleLight
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = if (isCopied) "Copied!" else "Copy JSON", fontSize = 14.sp)
                            }

                            LiquidGlassDialogButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Detachment Settings Backup")
                                        putExtra(Intent.EXTRA_TEXT, exportJson)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Detachment Backup"))
                                },
                                modifier = Modifier.weight(1f),
                                accentColor = PurpleLight
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Share Backup", fontSize = 14.sp)
                            }
                        }
                    } else if (exportError != null) {
                        Text(
                            text = "Error: $exportError",
                            color = RoseAccent,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Generating backup...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importResultText = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 180.dp),
                        placeholder = {
                            Text(
                                text = "Paste backup JSON here...",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x22000000),
                            unfocusedContainerColor = Color(0x18000000),
                            focusedBorderColor = PurpleLight,
                            unfocusedBorderColor = GlassBorderLow,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LiquidGlassDialogButton(
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        },
                        enabled = !isImporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_file_picker_btn"),
                        accentColor = CyanAccent
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isImporting) "Importing File..." else "Import Backup File (.json)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiquidGlassDialogButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val item = clipboard.primaryClip?.getItemAt(0)
                                val text = item?.text?.toString()
                                if (!text.isNullOrBlank()) {
                                    importJsonText = text
                                    importResultText = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            accentColor = PurpleLight
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Paste", fontSize = 14.sp)
                        }

                        LiquidGlassDialogButton(
                            onClick = {
                                if (importJsonText.isNotBlank()) {
                                    isImporting = true
                                    onImport(importJsonText) { result ->
                                        isImporting = false
                                        result.onSuccess { summary ->
                                            isImportSuccess = true
                                            importResultText = "Restored ${summary.appsUpdated} apps, ${summary.schedulesRestored} schedules, and settings!"
                                        }.onFailure { err ->
                                            isImportSuccess = false
                                            importResultText = "Import error: ${err.message ?: "Invalid JSON"}"
                                        }
                                    }
                                }
                            },
                            enabled = importJsonText.isNotBlank() && !isImporting,
                            modifier = Modifier.weight(1.2f),
                            accentColor = EmeraldAccent
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isImporting) "Restoring..." else "Restore", fontSize = 14.sp)
                        }
                    }

                    if (importResultText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isImportSuccess) EmeraldAccent.copy(alpha = 0.15f) else RoseAccent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isImportSuccess) EmeraldAccent.copy(alpha = 0.4f) else RoseAccent.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = importResultText!!,
                                color = if (isImportSuccess) EmeraldAccent else RoseAccent,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
