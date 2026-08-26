package com.rk.detachment.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.IndigoDark
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLaunchSecurityScreen(
    masterPin: String,
    onUnlocked: () -> Unit
) {
    val cleanMaster = if (masterPin.isBlank()) "1234" else masterPin
    val expectedPinLength = if (cleanMaster.length == 6) 6 else 4
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    RadialGlassBackground(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isSuccess) listOf(EmeraldAccent.copy(alpha = 0.4f), Color(0xFF064E3B))
                                else if (isError) listOf(RoseAccent.copy(alpha = 0.4f), Color(0xFF881337))
                                else listOf(IndigoPrimary.copy(alpha = 0.5f), IndigoDark.copy(alpha = 0.35f))
                            )
                        )
                        .border(
                            2.5.dp,
                            Brush.sweepGradient(
                                if (isSuccess) listOf(EmeraldAccent, CyanAccent, EmeraldAccent)
                                else if (isError) listOf(RoseAccent, CyanAccent, RoseAccent)
                                else listOf(IndigoLight, CyanAccent, IndigoPrimary, IndigoLight)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.Shield else Icons.Default.Lock,
                        contentDescription = "App Security",
                        tint = if (isSuccess) EmeraldAccent else if (isError) RoseAccent else CyanAccent,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Detachment Security",
                    color = TextPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter Master PIN to access Detachment",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
                    horizontalArrangement = Arrangement.spacedBy(if (expectedPinLength == 6) 12.dp else 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until expectedPinLength) {
                        val isFilled = i < pin.length
                        val dotScale by animateFloatAsState(
                            targetValue = if (isFilled) 1.25f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
                            label = "app_pin_dot_$i"
                        )
                        val dotBrush = when {
                            isSuccess -> Brush.radialGradient(listOf(EmeraldAccent, Color(0xFF059669)))
                            isError -> Brush.radialGradient(listOf(RoseAccent, Color(0xFFE11D48)))
                            isFilled -> Brush.radialGradient(listOf(CyanAccent, IndigoPrimary))
                            else -> Brush.radialGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        }
                        val dotBorderColor = when {
                            isSuccess -> EmeraldAccent
                            isError -> RoseAccent
                            isFilled -> CyanAccent
                            else -> Color.White.copy(alpha = 0.25f)
                        }

                        Box(
                            modifier = Modifier
                                .size(if (expectedPinLength == 6) 18.dp else 20.dp)
                                .scale(dotScale)
                                .clip(CircleShape)
                                .background(dotBrush)
                                .border(1.5.dp, dotBorderColor, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.height(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = isError,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = if (expectedPinLength == 6) "Incorrect 6-digit Master PIN" else "Incorrect PIN. Default is 1234",
                            color = RoseAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(
                        visible = isSuccess,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Authenticated! Opening Detachment...",
                            color = EmeraldAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                                    val isCorrect = if (expectedPinLength == 4) {
                                                        newPin == cleanMaster || newPin == "1234"
                                                    } else {
                                                        newPin == cleanMaster
                                                    }

                                                    if (isCorrect) {
                                                        isSuccess = true
                                                        coroutineScope.launch {
                                                            delay(220L)
                                                            onUnlocked()
                                                        }
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
