package com.rk.detachment.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.rk.detachment.R
import com.rk.detachment.ui.theme.FrostedBackground
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.PurpleDark
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.PurpleSoft
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.ui.theme.TextTertiary

@Composable
fun RadialGlassBackground(
    modifier: Modifier = Modifier,
    showWatermark: Boolean = true,
    watermarkAlpha: Float = 0.10f,
    content: @Composable BoxScope.() -> Unit
) {
    val topOrbColors = remember {
        listOf(
            PurplePrimary.copy(alpha = 0.28f),
            PurplePrimary.copy(alpha = 0.12f),
            Color.Transparent
        )
    }
    val bottomOrbColors = remember {
        listOf(
            RoseAccent.copy(alpha = 0.20f),
            RoseAccent.copy(alpha = 0.08f),
            Color.Transparent
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(FrostedBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = topOrbColors,
                    center = Offset(canvasWidth * 0.15f, canvasHeight * 0.08f),
                    radius = canvasWidth * 0.8f
                ),
                center = Offset(canvasWidth * 0.15f, canvasHeight * 0.08f),
                radius = canvasWidth * 0.8f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = bottomOrbColors,
                    center = Offset(canvasWidth * 0.85f, canvasHeight * 0.88f),
                    radius = canvasWidth * 0.75f
                ),
                center = Offset(canvasWidth * 0.85f, canvasHeight * 0.88f),
                radius = canvasWidth * 0.75f
            )
        }

        if (showWatermark) {
            Image(
                painter = painterResource(id = R.drawable.app_icon_asset),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .alpha(watermarkAlpha),
                contentScale = ContentScale.FillWidth
            )
        }

        content()
    }
}

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = GlassBorderHigh,
    backgroundColor: Color = GlassSurfaceHigh,
    backgroundBrush: Brush? = null,
    borderBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val resolvedBorderBrush = remember(borderColor, borderBrush) {
        borderBrush ?: if (borderColor == GlassBorderHigh || borderColor == GlassBorderMedium) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.32f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    borderColor.copy(alpha = 0.70f),
                    borderColor.copy(alpha = 0.30f),
                    borderColor.copy(alpha = 0.15f),
                    borderColor.copy(alpha = 0.40f)
                )
            )
        }
    }

    val liquidBackgroundBrush = remember(backgroundColor, backgroundBrush) {
        backgroundBrush ?: if (backgroundColor == GlassSurfaceHigh || backgroundColor == GlassSurfaceMedium || backgroundColor == GlassSurfaceLow) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.13f),
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.02f),
                    Color.White.copy(alpha = 0.06f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    backgroundColor.copy(alpha = 0.22f),
                    backgroundColor.copy(alpha = 0.10f),
                    backgroundColor.copy(alpha = 0.06f),
                    backgroundColor.copy(alpha = 0.14f)
                )
            )
        }
    }

    val baseModifier = if (onClick != null) {
        modifier
            .shadow(elevation = 10.dp, shape = cardShape, spotColor = Color.Black.copy(alpha = 0.35f), ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(cardShape)
            .clickable(onClick = onClick)
    } else {
        modifier
            .shadow(elevation = 10.dp, shape = cardShape, spotColor = Color.Black.copy(alpha = 0.35f), ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(cardShape)
    }

    Box(
        modifier = baseModifier
            .background(liquidBackgroundBrush)
            .border(BorderStroke(1.dp, resolvedBorderBrush), shape = cardShape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.07f),
                            Color.Transparent
                        ),
                        endY = 120f
                    )
                )
        )
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderColor: Color = GlassBorderMedium,
    backgroundColor: Color = GlassSurfaceMedium,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    FrostedGlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        onClick = onClick,
        content = content
    )
}

@Composable
fun FrostedGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    testTag: String = "glass_button"
) {
    val buttonColor = if (isPrimary) PurplePrimary else GlassSurfaceLow
    val borderBrush = if (isPrimary) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.06f)
            )
        )
    }
    val textColor = if (isPrimary) Color.White else TextPrimary

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(testTag)
            .height(54.dp)
            .shadow(
                elevation = if (isPrimary && enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = PurplePrimary.copy(alpha = 0.45f),
                ambientColor = PurplePrimary.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) buttonColor else Color(0x22334155),
            disabledContainerColor = Color(0x22334155)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) borderBrush else Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF)))
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) textColor else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) textColor else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    testTag: String = "glass_button"
) {
    FrostedGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        isPrimary = isPrimary,
        testTag = testTag
    )
}

@Composable
fun GlowingProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    primaryColor: Color = PurplePrimary,
    secondaryColor: Color = PurpleLight,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    centerContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val arcSize = size.copy(
                width = diameter - strokeWidth.toPx(),
                height = diameter - strokeWidth.toPx()
            )
            val topLeft = Offset(
                (size.width - arcSize.width) / 2f,
                (size.height - arcSize.height) / 2f
            )

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            val sweep = (progress.coerceIn(0f, 1f)) * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, secondaryColor, primaryColor),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        centerContent()
    }
}

@Composable
fun CategoryBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PurpleLight
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun FrostedBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PurpleSoft,
    backgroundColor: Color = PurplePrimary.copy(alpha = 0.20f),
    borderColor: Color = PurplePrimary.copy(alpha = 0.35f)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
        )
    }
}

@Composable
fun VibrantBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PurpleLight
) {
    FrostedBadge(
        text = text,
        modifier = modifier,
        color = color,
        backgroundColor = color.copy(alpha = 0.18f),
        borderColor = color.copy(alpha = 0.35f)
    )
}

@Composable
fun FrostedHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle.uppercase(),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )
        }

        if (actionIcon != null && onActionClick != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                    .clickable(onClick = onActionClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = PurpleLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = PurplePrimary,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val switchModifier = if (testTag != null) modifier.testTag(testTag) else modifier
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = switchModifier,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = activeColor
                )
            }
        } else null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = activeColor,
            checkedBorderColor = Color.Transparent,
            checkedIconColor = activeColor,
            uncheckedThumbColor = TextTertiary,
            uncheckedTrackColor = Color(0x28FFFFFF),
            uncheckedBorderColor = Color(0x33FFFFFF),
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
            disabledCheckedTrackColor = activeColor.copy(alpha = 0.4f),
            disabledUncheckedThumbColor = TextMuted,
            disabledUncheckedTrackColor = Color(0x15FFFFFF)
        )
    )
}

@Composable
fun LiquidGlassDialogButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = PurpleLight,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "liquid_glass_btn_scale"
    )

    val borderBrush = if (enabled) {
        Brush.verticalGradient(
            listOf(
                accentColor.copy(alpha = if (isPressed) 0.85f else 0.65f),
                accentColor.copy(alpha = if (isPressed) 0.50f else 0.25f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    val backgroundColor = if (enabled) {
        if (isPressed) accentColor.copy(alpha = 0.24f) else accentColor.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.04f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .heightIn(min = 38.dp),
        enabled = enabled,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderBrush),
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) accentColor else TextMuted
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
fun LiquidGlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = FrostedBackgroundDarker,
    accentColor: Color = PurpleLight,
    borderStrokeWidth: Dp = 2.dp,
    properties: DialogProperties = DialogProperties()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier.border(
            borderStrokeWidth,
            Brush.verticalGradient(
                listOf(
                    accentColor.copy(alpha = 0.85f),
                    accentColor.copy(alpha = 0.45f),
                    PurplePrimary.copy(alpha = 0.30f)
                )
            ),
            shape
        ),
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        properties = properties
    )
}

data class HoldParticle(
    val angle: Float,
    val speed: Float,
    val radius: Float
)

@Composable
fun LiquidGlassHoldButton(
    text: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    holdingText: String = "Hold to commit",
    icon: ImageVector? = null,
    accentColor: Color = PurpleLight,
    holdDurationMillis: Long = 3000L,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    testTag: String? = null
) {
    var isHolding by remember { mutableStateOf(false) }
    var isExploding by remember { mutableStateOf(false) }
    val progressAnimatable = remember { Animatable(0f) }
    val explodeAnimatable = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val isWaveActive = isHolding || progressAnimatable.value > 0f
    val wavePhase = if (isWaveActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "fluid_wave")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * Math.PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave_phase"
        ).value
    } else 0f

    val particles = remember {
        List(40) { index ->
            val angle = (index * (2.0 * Math.PI / 40.0) + (index % 5) * 0.12).toFloat()
            val speed = 120f + (index % 7) * 45f
            val radius = 5f + (index % 4) * 2.5f
            HoldParticle(angle, speed, radius)
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding && enabled && !isExploding) {
            val remaining = (1f - progressAnimatable.value) * holdDurationMillis
            val result = progressAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = remaining.toInt().coerceAtLeast(10),
                    easing = LinearEasing
                )
            )
            if (progressAnimatable.value >= 0.999f && isHolding) {
                isExploding = true
                coroutineScope.launch {
                    explodeAnimatable.snapTo(0f)
                    explodeAnimatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(450, easing = FastOutSlowInEasing)
                    )
                    onAction()
                    isExploding = false
                    progressAnimatable.snapTo(0f)
                    explodeAnimatable.snapTo(0f)
                }
            }
        } else if (!isHolding && !isExploding) {
            if (progressAnimatable.value > 0f) {
                progressAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val progress = progressAnimatable.value
    val explodeVal = explodeAnimatable.value

    val buttonScale by animateFloatAsState(
        targetValue = when {
            isExploding -> 1.04f - explodeVal * 0.04f
            isHolding -> 0.97f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn_scale"
    )

    val currentModifier = modifier
        .scale(buttonScale)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .pointerInput(enabled, isExploding) {
            if (!enabled || isExploding) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isHolding = true
                waitForUpOrCancellation()
                isHolding = false
            }
        }

    Box(
        modifier = currentModifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = if (isHolding) accentColor.copy(alpha = 0.16f) else accentColor.copy(alpha = 0.08f),
            border = BorderStroke(
                width = if (isHolding) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = if (isHolding) 0.85f else 0.45f),
                        accentColor.copy(alpha = if (isHolding) 0.50f else 0.20f)
                    )
                )
            )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (progress > 0f) {
                    val fillWidth = size.width * progress
                    val waveAmplitude = 5.dp.toPx()
                    val wavePath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo((fillWidth - waveAmplitude).coerceAtLeast(0f), 0f)
                        var y = 0f
                        val step = 4.dp.toPx()
                        while (y <= size.height) {
                            val waveOffset = sin((y / size.height) * 4f * Math.PI.toFloat() + wavePhase) * waveAmplitude
                            lineTo(fillWidth + waveOffset, y)
                            y += step
                        }
                        lineTo(0f, size.height)
                        close()
                    }

                    drawPath(
                        path = wavePath,
                        brush = Brush.horizontalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.22f),
                                accentColor.copy(alpha = 0.45f + progress * 0.25f)
                            ),
                            startX = 0f,
                            endX = fillWidth.coerceAtLeast(1f)
                        )
                    )

                    drawPath(
                        path = wavePath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.65f),
                                accentColor.copy(alpha = 0.85f)
                            )
                        ),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    val flecks = 6
                    for (i in 0 until flecks) {
                        val fx = (fillWidth * (0.2f + (i * 0.13f))).coerceIn(0f, fillWidth)
                        val fy = (size.height * ((i * 0.28f + wavePhase * 0.1f) % 1f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.4f),
                            radius = 1.5.dp.toPx(),
                            center = Offset(fx, fy)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isHolding) Color.White else accentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isHolding || progress > 0.05f) holdingText else text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isHolding) Color.White else accentColor
            )
        }

        if (isExploding && explodeVal > 0f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val alpha = (1f - explodeVal).coerceIn(0f, 1f)
                val densityVal = density

                particles.forEach { p ->
                    val dist = p.speed * explodeVal * 2.5f * densityVal
                    val px = cx + cos(p.angle) * dist
                    val py = cy + sin(p.angle) * dist
                    val r = p.radius * densityVal * (1f - explodeVal * 0.25f)

                    drawCircle(
                        color = accentColor.copy(alpha = alpha * 0.40f),
                        radius = r * 1.6f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = alpha * 0.90f),
                        radius = r,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.95f),
                        radius = r * 0.45f,
                        center = Offset(px - r * 0.25f, py - r * 0.25f)
                    )
                }
            }
        }
    }
}
