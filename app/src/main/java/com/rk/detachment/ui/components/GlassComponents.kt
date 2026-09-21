package com.rk.detachment.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val borderBrush = remember(borderColor) {
        if (borderColor == GlassBorderHigh || borderColor == GlassBorderMedium) {
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

    val liquidBackgroundBrush = remember(backgroundColor) {
        if (backgroundColor == GlassSurfaceHigh || backgroundColor == GlassSurfaceMedium || backgroundColor == GlassSurfaceLow) {
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
            .border(BorderStroke(1.dp, borderBrush), shape = cardShape)
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
