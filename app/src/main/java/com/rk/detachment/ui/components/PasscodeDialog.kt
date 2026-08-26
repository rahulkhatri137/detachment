package com.rk.detachment.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.IndigoDark
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PasscodeUnlockDialog(
    appName: String,
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onUnlockSuccess: (Int) -> Unit,
    unlockMinutes: Int = 15,
    pinLength: Int = 4
) {
    var selectedMinutes by remember(unlockMinutes) { mutableIntStateOf(unlockMinutes) }
    val expectedPinLength = if (pinLength == 6) 6 else 4
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val durationOptions = listOf(5, 15, 30, 60)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .shadow(32.dp, RoundedCornerShape(32.dp), spotColor = if (isError) RoseAccent else if (isSuccess) EmeraldAccent else IndigoPrimary),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF0F1424),
                border = androidx.compose.foundation.BorderStroke(
                    1.8.dp,
                    Brush.verticalGradient(
                        listOf(
                            if (isError) RoseAccent else if (isSuccess) EmeraldAccent else IndigoLight,
                            IndigoDark.copy(alpha = 0.6f),
                            GlassBorderMedium
                        )
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-60).dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        if (isError) RoseAccent.copy(alpha = 0.25f)
                                        else if (isSuccess) EmeraldAccent.copy(alpha = 0.25f)
                                        else IndigoPrimary.copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.06f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderMedium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    durationOptions.forEach { mins ->
                                        val isSelected = selectedMinutes == mins
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .then(
                                                    if (isSelected) {
                                                        Modifier.background(
                                                            Brush.horizontalGradient(
                                                                listOf(IndigoPrimary, CyanAccent)
                                                            )
                                                        )
                                                    } else {
                                                        Modifier.background(Color.Transparent)
                                                    }
                                                )
                                                .clickable { selectedMinutes = mins }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                                .testTag("pin_duration_${mins}m"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${mins}m",
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                    .testTag("close_passcode_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isSuccess) listOf(EmeraldAccent.copy(alpha = 0.35f), Color(0xFF064E3B))
                                        else if (isError) listOf(RoseAccent.copy(alpha = 0.35f), Color(0xFF881337))
                                        else listOf(IndigoPrimary.copy(alpha = 0.45f), IndigoDark.copy(alpha = 0.3f))
                                    )
                                )
                                .border(
                                    2.dp,
                                    Brush.sweepGradient(
                                        if (isSuccess) listOf(EmeraldAccent, CyanAccent, EmeraldAccent)
                                        else if (isError) listOf(RoseAccent, AmberAccent, RoseAccent)
                                        else listOf(IndigoLight, CyanAccent, IndigoPrimary, IndigoLight)
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isSuccess) EmeraldAccent else if (isError) RoseAccent else CyanAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Unlock $appName",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Enter master PIN for $selectedMinutes-min pass",
                            color = IndigoLight.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(if (expectedPinLength == 6) 12.dp else 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until expectedPinLength) {
                                val isFilled = i < pin.length
                                val dotScale by animateFloatAsState(
                                    targetValue = if (isFilled) 1.25f else 1.0f,
                                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
                                    label = "dot_scale_$i"
                                )
                                val dotBrush = when {
                                    isSuccess -> Brush.radialGradient(listOf(EmeraldAccent, Color(0xFF059669)))
                                    isError -> Brush.radialGradient(listOf(RoseAccent, Color(0xFFE11D48)))
                                    isFilled -> Brush.radialGradient(listOf(CyanAccent, IndigoPrimary))
                                    else -> Brush.radialGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f)))
                                }
                                val dotBorderColor = when {
                                    isSuccess -> EmeraldAccent
                                    isError -> RoseAccent
                                    isFilled -> CyanAccent
                                    else -> Color.White.copy(alpha = 0.25f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(if (expectedPinLength == 6) 16.dp else 20.dp)
                                        .scale(dotScale)
                                        .clip(CircleShape)
                                        .background(dotBrush)
                                        .border(1.5.dp, dotBorderColor, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.height(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimatedVisibility(
                                visible = isError,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = if (expectedPinLength == 6) "Incorrect 6-digit Master PIN" else "Incorrect Passcode. Try default 1234",
                                    color = RoseAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            AnimatedVisibility(
                                visible = isSuccess,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = "PIN verified! Unlocking for $selectedMinutes min...",
                                    color = EmeraldAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val rows = listOf(
                                listOf("1", "2", "3"),
                                listOf("4", "5", "6"),
                                listOf("7", "8", "9"),
                                listOf("C", "0", "DEL")
                            )

                            for (row in rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (item in row) {
                                        KeypadButton(
                                            label = item,
                                            onClick = {
                                                if (isSuccess) return@KeypadButton
                                                isError = false
                                                when (item) {
                                                    "DEL" -> {
                                                        if (pin.isNotEmpty()) {
                                                            pin = pin.dropLast(1)
                                                        }
                                                    }
                                                    "C" -> {
                                                        pin = ""
                                                    }
                                                    else -> {
                                                        if (pin.length < expectedPinLength) {
                                                            val newPin = pin + item
                                                            pin = newPin
                                                            if (newPin.length == expectedPinLength) {
                                                                val success = onVerifyPin(newPin)
                                                                if (success) {
                                                                    isSuccess = true
                                                                    onUnlockSuccess(selectedMinutes)
                                                                } else {
                                                                    isError = true
                                                                    pin = ""
                                                                    coroutineScope.launch {
                                                                        shakeOffset.animateTo(
                                                                            targetValue = 0f,
                                                                            animationSpec = keyframes {
                                                                                durationMillis = 400
                                                                                -20f at 50 using FastOutSlowInEasing
                                                                                20f at 100 using FastOutSlowInEasing
                                                                                -16f at 150 using FastOutSlowInEasing
                                                                                16f at 200 using FastOutSlowInEasing
                                                                                -10f at 250 using FastOutSlowInEasing
                                                                                10f at 300 using FastOutSlowInEasing
                                                                                0f at 400
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "keypad_scale_$label"
    )

    val isDel = label == "DEL"
    val isClear = label == "C"

    val backgroundBrush = when {
        isPressed -> Brush.radialGradient(
            listOf(IndigoPrimary, CyanAccent)
        )
        isDel -> Brush.linearGradient(
            listOf(RoseAccent.copy(alpha = 0.22f), Color(0xFF4C0519).copy(alpha = 0.4f))
        )
        isClear -> Brush.linearGradient(
            listOf(AmberAccent.copy(alpha = 0.22f), Color(0xFF451A03).copy(alpha = 0.4f))
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    }

    val borderColor = when {
        isPressed -> IndigoLight
        isDel -> RoseAccent.copy(alpha = 0.6f)
        isClear -> AmberAccent.copy(alpha = 0.6f)
        else -> GlassBorderMedium.copy(alpha = 0.8f)
    }

    val textColor = when {
        isDel -> RoseAccent
        isClear -> AmberAccent
        else -> TextPrimary
    }

    Surface(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("keypad_btn_$label"),
        shape = CircleShape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.4.dp, borderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            if (isDel) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = RoseAccent,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = if (isClear) 22.sp else 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


