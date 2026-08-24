package com.rk.detachment.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.ui.theme.FrostedBackground
import com.rk.detachment.ui.theme.GlassBorderHigh
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceLow
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.IndigoDark
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.IndigoSoft
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.ui.theme.TextTertiary

@Composable
fun RadialGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val topOrbColors = remember {
        listOf(
            IndigoPrimary.copy(alpha = 0.28f),
            IndigoPrimary.copy(alpha = 0.12f),
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
    val borderStroke = remember(borderColor) {
        BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    }

    val cardModifier = if (onClick != null) {
        modifier
            .clip(cardShape)
            .clickable(onClick = onClick)
    } else {
        modifier.clip(cardShape)
    }

    Card(
        modifier = cardModifier.border(borderStroke, shape = cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
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
    val buttonColor = if (isPrimary) IndigoPrimary else GlassSurfaceLow
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
                spotColor = IndigoPrimary.copy(alpha = 0.45f),
                ambientColor = IndigoPrimary.copy(alpha = 0.35f)
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
    primaryColor: Color = IndigoPrimary,
    secondaryColor: Color = IndigoLight,
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
    color: Color = IndigoLight
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
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
        )
    }
}

@Composable
fun FrostedBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = IndigoSoft,
    backgroundColor: Color = IndigoPrimary.copy(alpha = 0.20f),
    borderColor: Color = IndigoPrimary.copy(alpha = 0.35f)
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
    color: Color = IndigoLight
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
                    tint = IndigoLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
