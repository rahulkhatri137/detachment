package com.rk.detachment.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.rk.detachment.util.UninstallProtectionManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rk.detachment.data.model.ImportSummary
import com.rk.detachment.ui.theme.AmberAccent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ParentalControlDialog(
    onDismiss: () -> Unit,
    isManaged: Boolean,
    pairingCode: String,
    serverUrl: String,
    lastSyncTime: Long,
    syncStatus: String?,
    onRegisterDevice: (String, (Result<String>) -> Unit) -> Unit,
    onPairWithCode: (String, String, (Result<Unit>) -> Unit) -> Unit,
    onSyncNow: ((Result<ImportSummary>) -> Unit) -> Unit,
    onUnpair: ((Result<Unit>) -> Unit) -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf(if (serverUrl.isNotBlank()) serverUrl else "https://detachment-parental.workers.dev") }
    var inputCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }
    var showPinDialogForUnpair by remember { mutableStateOf(false) }

    LiquidGlassAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = null,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("parental_control_dialog"),
        accentColor = if (isManaged) EmeraldAccent else PurpleLight,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isManaged) EmeraldAccent.copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isManaged) Icons.Default.Security else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isManaged) EmeraldAccent else PurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Parental Control",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isManaged) "Managed Remote Guard" else "Cloudflare Worker Sync",
                            color = if (isManaged) EmeraldAccent else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isManaged) {
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = EmeraldAccent.copy(alpha = 0.08f),
                        borderColor = EmeraldAccent.copy(alpha = 0.35f),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Device Paired",
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                FrostedBadge(
                                    text = "ENFORCED",
                                    color = EmeraldAccent,
                                    backgroundColor = EmeraldAccent.copy(alpha = 0.2f),
                                    borderColor = EmeraldAccent.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Pairing Code",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pairingCode,
                                    color = TextPrimary,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 4.sp
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Detachment Code", pairingCode)
                                        clipboard.setPrimaryClip(clip)
                                        isCopied = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = if (isCopied) EmeraldAccent else PurpleLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val dateStr = if (lastSyncTime > 0) {
                                SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(lastSyncTime))
                            } else {
                                "Never"
                            }
                            Text(
                                text = "Last Synced: $dateStr",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            if (!syncStatus.isNullOrBlank()) {
                                Text(
                                    text = "Status: $syncStatus",
                                    color = if (syncStatus.contains("Error", true)) RoseAccent else CyanAccent,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassSurfaceLow,
                        borderColor = GlassBorderLow,
                        cornerRadius = 14.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Child Protection Mode is active. Local limits and focus schedules are locked and enforced remotely from the Cloudflare Worker web dashboard.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    val isAdminActive = remember(context) { UninstallProtectionManager.isDeviceAdminActive(context) }
                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassSurfaceLow,
                        borderColor = if (isAdminActive) EmeraldAccent.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.4f),
                        cornerRadius = 14.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAdminActive) Icons.Default.Shield else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (isAdminActive) EmeraldAccent else AmberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Uninstall Protection",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                FrostedBadge(
                                    text = if (isAdminActive) "ENFORCED" else "NOT ACTIVE",
                                    color = if (isAdminActive) EmeraldAccent else AmberAccent
                                )
                            }
                            Text(
                                text = if (isAdminActive) {
                                    "Device Administrator is active. Android OS disables uninstallation, and settings tampering is guarded with Master PIN."
                                } else {
                                    "Activate Device Administrator to prevent the child from uninstalling Detachment from settings or app launcher."
                                },
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            )
                            if (!isAdminActive) {
                                LiquidGlassDialogButton(
                                    onClick = {
                                        try {
                                            context.startActivity(UninstallProtectionManager.createDeviceAdminIntent(context))
                                        } catch (e: Exception) {
                                        }
                                    },
                                    accentColor = AmberAccent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Activate Device Administrator",
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassDialogButton(
                            onClick = {
                                isLoading = true
                                statusMessage = null
                                onSyncNow { result ->
                                    isLoading = false
                                    result.onSuccess { summary ->
                                        isError = false
                                        statusMessage = "Synced! Applied ${summary.appsUpdated} apps & ${summary.schedulesRestored} schedules."
                                    }.onFailure { err ->
                                        isError = true
                                        statusMessage = "Sync failed: ${err.message}"
                                    }
                                }
                            },
                            enabled = !isLoading,
                            accentColor = EmeraldAccent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isLoading) "Syncing..." else "Sync Now",
                                color = EmeraldAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        LiquidGlassDialogButton(
                            onClick = { showPinDialogForUnpair = true },
                            enabled = !isLoading,
                            accentColor = RoseAccent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Unpair",
                                color = RoseAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Connect this device to your Cloudflare Worker site to remotely enforce app limits, distraction delays, and focus schedules from any web browser.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Cloudflare Worker URL") },
                        placeholder = { Text("https://my-parental-worker.workers.dev") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleLight,
                            unfocusedBorderColor = GlassBorderMedium,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = FrostedBackgroundDarker,
                            unfocusedContainerColor = FrostedBackgroundDarker
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassSurfaceLow,
                        borderColor = GlassBorderMedium,
                        cornerRadius = 14.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Option A: Register Device",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Registers this phone on the worker and generates a new 6-character pairing code.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            LiquidGlassDialogButton(
                                onClick = {
                                    isLoading = true
                                    statusMessage = null
                                    onRegisterDevice(inputUrl) { result ->
                                        isLoading = false
                                        result.onSuccess { code ->
                                            isError = false
                                            statusMessage = "Registered device! Pairing code: $code"
                                        }.onFailure { err ->
                                            isError = true
                                            statusMessage = "Registration failed: ${err.message}"
                                        }
                                    }
                                },
                                enabled = !isLoading && inputUrl.isNotBlank(),
                                accentColor = PurpleLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isLoading) "Registering..." else "Register & Generate Code",
                                    color = PurpleLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    FrostedGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = GlassSurfaceLow,
                        borderColor = GlassBorderMedium,
                        cornerRadius = 14.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Option B: Connect with Existing Code",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.uppercase() },
                                label = { Text("6-Character Device Code") },
                                placeholder = { Text("e.g. DEMO99") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldAccent,
                                    unfocusedBorderColor = GlassBorderMedium,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = FrostedBackgroundDarker,
                                    unfocusedContainerColor = FrostedBackgroundDarker
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            LiquidGlassDialogButton(
                                onClick = {
                                    isLoading = true
                                    statusMessage = null
                                    onPairWithCode(inputUrl, inputCode) { result ->
                                        isLoading = false
                                        result.onSuccess {
                                            isError = false
                                            statusMessage = "Device paired successfully!"
                                        }.onFailure { err ->
                                            isError = true
                                            statusMessage = "Pairing failed: ${err.message}"
                                        }
                                    }
                                },
                                enabled = !isLoading && inputUrl.isNotBlank() && inputCode.isNotBlank(),
                                accentColor = EmeraldAccent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isLoading) "Connecting..." else "Pair with Code",
                                    color = EmeraldAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (!statusMessage.isNullOrBlank()) {
                    Text(
                        text = statusMessage ?: "",
                        color = if (isError) RoseAccent else EmeraldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    )

    if (showPinDialogForUnpair) {
        PasscodeUnlockDialog(
            appName = "Parental Control Unpair",
            onDismiss = { showPinDialogForUnpair = false },
            onVerifyPin = onVerifyPin,
            onUnlockSuccess = {
                showPinDialogForUnpair = false
                isLoading = true
                onUnpair { result ->
                    isLoading = false
                    result.onSuccess {
                        statusMessage = "Device unparented. Local controls restored."
                        isError = false
                    }.onFailure { err ->
                        statusMessage = "Unpair failed: ${err.message}"
                        isError = true
                    }
                }
            }
        )
    }
}
