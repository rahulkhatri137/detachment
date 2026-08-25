package com.rk.detachment.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FiberSmartRecord
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.model.ConsciousnessMetrics
import com.rk.detachment.data.model.HabitLoopItem
import com.rk.detachment.data.model.YouVsYouComparison
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.AmberLight
import com.rk.detachment.ui.theme.CyanAccent
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.EmeraldLight
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
import com.rk.detachment.ui.theme.RoseLight
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.ui.theme.TextTertiary
import com.rk.detachment.viewmodel.DetachmentUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConsciousnessScoreScreen(
    uiState: DetachmentUiState,
    onNavigateBack: () -> Unit,
    onNavigateToShield: () -> Unit,
    onNavigateToLimits: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comparison = uiState.consciousnessComparison
    val today = comparison.today
    val yesterday = comparison.yesterday

    RadialGlassBackground(modifier = modifier.testTag("consciousness_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "consciousness_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GlassSurfaceHigh)
                                .border(1.dp, GlassBorderHigh, CircleShape)
                                .testTag("btn_back_to_dashboard")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Dashboard",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Consciousness Score",
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "DIGITAL INTENTIONALITY INDEX",
                                color = IndigoLight,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceHigh)
                            .border(1.dp, GlassBorderHigh, CircleShape)
                            .testTag("btn_refresh_consciousness")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Metrics",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item(key = "rhythmic_web_card") {
                RhythmicConsciousnessWebCard(
                    metrics = today,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rhythmic_consciousness_web_card")
                )
            }

            item(key = "core_stats_section_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mindful Metrics Breakdown",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item(key = "unlocks_breakdown_card") {
                UnlocksBreakdownCard(
                    totalUnlocks = today.totalUnlocks,
                    intentionalUnlocks = today.intentionalUnlocks,
                    habitualUnlocks = today.habitualUnlocks
                )
            }

            item(key = "grid_stats_row_1") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        title = "Longest Phone-Free",
                        value = formatMinutesToHoursMinutes(today.longestPhoneFreeMinutes),
                        subtitle = "Continuous peaceful unplug",
                        icon = Icons.Default.Spa,
                        accentColor = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Longest Screen Streak",
                        value = formatMinutesToHoursMinutes(today.longestContinuousUsageMinutes),
                        subtitle = "Peak continuous session",
                        icon = Icons.Default.HourglassTop,
                        accentColor = if (today.longestContinuousUsageMinutes > 60) RoseAccent else AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item(key = "grid_stats_row_2") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        title = "Unnecessary Usage",
                        value = formatMinutesToHoursMinutes(today.unnecessaryUsageMinutes),
                        subtitle = "Impulsive / distracting time",
                        icon = Icons.Default.Timelapse,
                        accentColor = if (today.unnecessaryUsageMinutes > 45) RoseAccent else IndigoLight,
                        modifier = Modifier.weight(1f)
                    )

                    StatTile(
                        title = "Mindless Sessions",
                        value = "${today.mindlessSessionsCount}",
                        subtitle = "Quick check bounces (<1m)",
                        icon = Icons.Default.FiberSmartRecord,
                        accentColor = if (today.mindlessSessionsCount > 8) RoseAccent else AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item(key = "habit_loop_section") {
                HabitLoopSection(
                    habitLoops = today.habitLoops,
                    allApps = uiState.allApps,
                    onNavigateToShield = onNavigateToShield,
                    onNavigateToLimits = onNavigateToLimits
                )
            }

            item(key = "you_vs_you_section") {
                YouVsYouSection(
                    comparison = comparison
                )
            }
        }
    }
}

@Composable
fun RhythmicConsciousnessWebCard(
    metrics: ConsciousnessMetrics,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rosette_transition")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val tierColor = when {
        metrics.score >= 85 -> EmeraldAccent
        metrics.score >= 70 -> CyanAccent
        metrics.score >= 50 -> AmberAccent
        else -> RoseAccent
    }

    val blueColor = Color(0xFF0284C7)
    val cyanColor = Color(0xFF06B6D4)
    val emeraldColor = Color(0xFF10B981)
    val mintColor = Color(0xFF34D399)

    FrostedGlassCard(
        modifier = modifier,
        cornerRadius = 32.dp,
        backgroundColor = GlassSurfaceHigh,
        borderColor = GlassBorderHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .testTag("rhythmic_consciousness_canvas_box"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = (size.minDimension / 2f) * 0.94f
                    val innerRadius = maxRadius * 0.38f
                    val outerRadius = maxRadius * 0.98f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                cyanColor.copy(alpha = 0.06f),
                                emeraldColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = outerRadius * 1.05f
                        ),
                        radius = outerRadius * 1.05f,
                        center = centerOffset
                    )

                    val goldenAngle = 2.399963229728653
                    val dotCount = 260
                    
                    data class RosetteDot(
                        val offset: Offset,
                        val radiusPx: Float,
                        val color: Color,
                        val alpha: Float,
                        val index: Int
                    )

                    val dots = ArrayList<RosetteDot>(dotCount)

                    for (i in 0 until dotCount) {
                        val fraction = i.toFloat() / dotCount.toFloat()
                        val baseR = innerRadius + (outerRadius - innerRadius) * Math.sqrt(fraction.toDouble()).toFloat()
                        val r = baseR * breathingScale
                        val angle = (i * goldenAngle) + rotationAngle
                        
                        val x = centerOffset.x + (r * cos(angle)).toFloat()
                        val y = centerOffset.y + (r * sin(angle)).toFloat()

                        val nx = (x - centerOffset.x) / outerRadius
                        val ny = (y - centerOffset.y) / outerRadius
                        val gradientFactor = ((nx + ny + 1.4f) / 2.8f).coerceIn(0f, 1f)

                        val dotColor = if (gradientFactor < 0.5f) {
                            val subFrac = gradientFactor / 0.5f
                            androidx.compose.ui.graphics.lerp(blueColor, cyanColor, subFrac)
                        } else {
                            val subFrac = (gradientFactor - 0.5f) / 0.5f
                            androidx.compose.ui.graphics.lerp(cyanColor, emeraldColor, subFrac)
                        }

                        val baseDotSize = 2.2.dp.toPx() + (4.0.dp.toPx() * Math.pow(fraction.toDouble(), 0.7).toFloat())
                        val waveDist = (fraction - wavePhase + 1f) % 1f
                        val waveGlow = if (waveDist < 0.25f) (1f - (waveDist / 0.25f)) * 0.35f else 0f
                        
                        val finalAlpha = (0.75f + waveGlow).coerceIn(0.6f, 1.0f)
                        val finalRadius = baseDotSize * (1f + waveGlow * 0.35f)

                        dots.add(RosetteDot(Offset(x, y), finalRadius, dotColor, finalAlpha, i))
                    }

                    for (i in 0 until dots.size) {
                        val neighbors = listOf(i + 13, i + 21, i + 34)
                        val p1 = dots[i]
                        
                        for (ni in neighbors) {
                            if (ni < dots.size) {
                                val p2 = dots[ni]
                                val distSq = (p1.offset.x - p2.offset.x) * (p1.offset.x - p2.offset.x) +
                                             (p1.offset.y - p2.offset.y) * (p1.offset.y - p2.offset.y)
                                val maxDist = 24.dp.toPx()
                                
                                if (distSq < maxDist * maxDist) {
                                    val dist = Math.sqrt(distSq.toDouble()).toFloat()
                                    val lineAlpha = ((1f - (dist / maxDist)) * 0.16f).coerceIn(0f, 0.22f)
                                    val lineColor = androidx.compose.ui.graphics.lerp(p1.color, p2.color, 0.5f).copy(alpha = lineAlpha)
                                    
                                    drawLine(
                                        color = lineColor,
                                        start = p1.offset,
                                        end = p2.offset,
                                        strokeWidth = 0.8.dp.toPx()
                                    )
                                }
                            }
                        }
                    }

                    dots.forEach { dot ->
                        drawCircle(
                            color = dot.color.copy(alpha = dot.alpha * 0.25f),
                            radius = dot.radiusPx * 1.5f,
                            center = dot.offset
                        )
                        drawCircle(
                            color = dot.color.copy(alpha = dot.alpha),
                            radius = dot.radiusPx,
                            center = dot.offset
                        )
                    }

                    val centerHoleRadius = innerRadius * 0.92f
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF080D1A)
                            ),
                            center = centerOffset,
                            radius = centerHoleRadius
                        ),
                        radius = centerHoleRadius,
                        center = centerOffset
                    )

                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(cyanColor.copy(alpha = 0.5f), emeraldColor.copy(alpha = 0.5f)),
                            start = Offset(centerOffset.x - centerHoleRadius, centerOffset.y - centerHoleRadius),
                            end = Offset(centerOffset.x + centerHoleRadius, centerOffset.y + centerHoleRadius)
                        ),
                        radius = centerHoleRadius,
                        center = centerOffset,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${metrics.score}",
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "CONSCIOUS",
                        color = tierColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FrostedBadge(
                text = metrics.tierTitle.uppercase(),
                color = tierColor,
                backgroundColor = tierColor.copy(alpha = 0.16f),
                borderColor = tierColor.copy(alpha = 0.40f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = metrics.tierSubtitle,
                color = TextSecondary,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadarPill(
                        label = "Resistance",
                        value = "${(metrics.resistanceScore * 100).toInt()}%",
                        color = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    RadarPill(
                        label = "Intentional",
                        value = "${(metrics.intentionalityScore * 100).toInt()}%",
                        color = IndigoLight,
                        modifier = Modifier.weight(1f)
                    )
                    RadarPill(
                        label = "Unplugged",
                        value = "${(metrics.unpluggedScore * 100).toInt()}%",
                        color = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadarPill(
                        label = "Discipline",
                        value = "${(metrics.disciplineScore * 100).toInt()}%",
                        color = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                    RadarPill(
                        label = "Deep Focus",
                        value = "${(metrics.focusScore * 100).toInt()}%",
                        color = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                    RadarPill(
                        label = "Mindful Lock",
                        value = "${(metrics.unlockMindfulnessScore * 100).toInt()}%",
                        color = EmeraldLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RadarPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GlassSurfaceLow)
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                color = TextMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UnlocksBreakdownCard(
    totalUnlocks: Int,
    intentionalUnlocks: Int,
    habitualUnlocks: Int,
    modifier: Modifier = Modifier
) {
    val intentionalRatio = if (totalUnlocks > 0) (intentionalUnlocks.toFloat() / totalUnlocks.toFloat()) else 0.7f
    val habitualRatio = if (totalUnlocks > 0) (habitualUnlocks.toFloat() / totalUnlocks.toFloat()) else 0.3f

    FrostedGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        backgroundColor = GlassSurfaceHigh,
        borderColor = GlassBorderMedium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
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
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Device Unlocks",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalUnlocks total pickups today",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Text(
                    text = "$totalUnlocks",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(intentionalRatio)
                        .fillMaxSize()
                        .background(EmeraldAccent)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .background(RoseAccent)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EmeraldAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Intentional: $intentionalUnlocks (${(intentionalRatio * 100).toInt()}%)",
                        color = EmeraldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(RoseAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Habitual: $habitualUnlocks (${(habitualRatio * 100).toInt()}%)",
                        color = RoseLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun StatTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        backgroundColor = GlassSurfaceMedium,
        borderColor = GlassBorderMedium
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.5.sp
            )
        }
    }
}

@Composable
fun HabitLoopSection(
    habitLoops: List<HabitLoopItem>,
    allApps: List<AppLimitEntity>,
    onNavigateToShield: () -> Unit,
    onNavigateToLimits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shieldedPackageSet = remember(allApps) {
        allApps.filter { it.isShieldActive }.map { it.packageName }.toSet()
    }
    val hasLoops = habitLoops.isNotEmpty()

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_loop_detection_card"),
        cornerRadius = 24.dp,
        backgroundColor = GlassSurfaceHigh,
        borderColor = if (hasLoops) AmberAccent.copy(alpha = 0.4f) else GlassBorderMedium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (hasLoops) AmberAccent.copy(alpha = 0.20f) else EmeraldAccent.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = null,
                        tint = if (hasLoops) AmberAccent else EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Habit Loop Detector",
                        color = TextPrimary,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Repetitive impulsive open & close cycles",
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val bannerColor = if (hasLoops) AmberAccent else EmeraldAccent
            val bannerBg = if (hasLoops) AmberAccent.copy(alpha = 0.12f) else EmeraldAccent.copy(alpha = 0.12f)
            val bannerBorder = if (hasLoops) AmberAccent.copy(alpha = 0.35f) else EmeraldAccent.copy(alpha = 0.35f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bannerBg)
                    .border(1.dp, bannerBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (hasLoops) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasLoops) {
                            "${habitLoops.size} Compulsive Loop${if (habitLoops.size > 1) "s" else ""} Detected"
                        } else {
                            "Zero Compulsive Loops Detected"
                        },
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (hasLoops) "ATTENTION NEEDED" else "ALL CLEAN",
                    color = bannerColor,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!hasLoops) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GlassSurfaceLow)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "✨ You are opening apps with mindful intention rather than muscle-memory reflex.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            } else {
                habitLoops.forEachIndexed { index, loop ->
                    val isShielded = shieldedPackageSet.contains(loop.packageName)
                    HabitLoopItemRow(
                        item = loop,
                        isShielded = isShielded,
                        onAddShield = onNavigateToShield
                    )
                    if (index < habitLoops.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HabitLoopItemRow(
    item: HabitLoopItem,
    isShielded: Boolean,
    onAddShield: () -> Unit
) {
    val severityColor = when (item.severity) {
        "SEVERE" -> RoseAccent
        "MODERATE" -> AmberAccent
        else -> IndigoLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(GlassSurfaceLow)
            .border(1.dp, severityColor.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(severityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.appName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${item.openCount} opens in ${item.timeSpanMinutes}m)",
                        color = severityColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (isShielded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldAccent.copy(alpha = 0.15f))
                            .border(0.5.dp, EmeraldAccent.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = EmeraldAccent,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Delay Active",
                            color = EmeraldAccent,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onAddShield,
                        colors = ButtonDefaults.buttonColors(containerColor = severityColor.copy(alpha = 0.22f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 7.dp, vertical = 0.dp),
                        modifier = Modifier.height(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Add Delay",
                            color = severityColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Avg session: ${item.avgSessionDurationSeconds}s • ${item.severity.lowercase().replaceFirstChar { it.uppercase() }} reflexive loop",
                color = TextSecondary,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun YouVsYouSection(
    comparison: YouVsYouComparison,
    modifier: Modifier = Modifier
) {
    val today = comparison.today
    val yesterday = comparison.yesterday

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("you_vs_you_card"),
        cornerRadius = 28.dp,
        backgroundColor = GlassSurfaceHigh,
        borderColor = IndigoPrimary.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "You vs You",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Yesterday vs Today Progress",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                val isImproved = comparison.scoreDelta >= 0
                val deltaColor = if (isImproved) EmeraldAccent else RoseAccent

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(deltaColor.copy(alpha = 0.15f))
                        .border(1.dp, deltaColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isImproved) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = deltaColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isImproved) "+${comparison.scoreDelta} PTS" else "${comparison.scoreDelta} PTS",
                        color = deltaColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ComparisonMetricRow(
                title = "Consciousness Score",
                todayVal = "${today.score}/100",
                yesterdayVal = "${yesterday.score}/100",
                deltaPercent = comparison.scoreDelta,
                isPositiveImprovement = comparison.scoreDelta >= 0,
                unit = "pts"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricRow(
                title = "Total Screen Time",
                todayVal = formatMinutesToHoursMinutes(today.totalScreenTimeMinutes),
                yesterdayVal = formatMinutesToHoursMinutes(yesterday.totalScreenTimeMinutes),
                deltaPercent = comparison.screenTimeDeltaPercent,
                isPositiveImprovement = comparison.screenTimeDeltaPercent <= 0,
                unit = "%"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricRow(
                title = "Total Pickups",
                todayVal = "${today.totalUnlocks}",
                yesterdayVal = "${yesterday.totalUnlocks}",
                deltaPercent = comparison.unlocksDeltaPercent,
                isPositiveImprovement = comparison.unlocksDeltaPercent <= 0,
                unit = "%"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricRow(
                title = "Habitual Unlocks",
                todayVal = "${today.habitualUnlocks}",
                yesterdayVal = "${yesterday.habitualUnlocks}",
                deltaPercent = comparison.habitualUnlocksDeltaPercent,
                isPositiveImprovement = comparison.habitualUnlocksDeltaPercent <= 0,
                unit = "%"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricRow(
                title = "Longest Phone-Free Block",
                todayVal = formatMinutesToHoursMinutes(today.longestPhoneFreeMinutes),
                yesterdayVal = formatMinutesToHoursMinutes(yesterday.longestPhoneFreeMinutes),
                deltaPercent = comparison.phoneFreeDeltaPercent,
                isPositiveImprovement = comparison.phoneFreeDeltaPercent >= 0,
                unit = "%"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonMetricRow(
                title = "Mindless Bounce Sessions",
                todayVal = "${today.mindlessSessionsCount}",
                yesterdayVal = "${yesterday.mindlessSessionsCount}",
                deltaPercent = comparison.mindlessSessionsDeltaPercent,
                isPositiveImprovement = comparison.mindlessSessionsDeltaPercent <= 0,
                unit = "%"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassSurfaceLow)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (comparison.scoreDelta >= 0) {
                        "✨ You are more intentional today! Your continuous phone-free time increased, and habitual quick checks dropped significantly."
                    } else {
                        "💡 Screen usage is slightly higher today. Set mindful delays on distracting apps to reclaim focus."
                    },
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ComparisonMetricRow(
    title: String,
    todayVal: String,
    yesterdayVal: String,
    deltaPercent: Int,
    isPositiveImprovement: Boolean,
    unit: String
) {
    val deltaColor = if (isPositiveImprovement) EmeraldAccent else RoseAccent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassSurfaceLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Yesterday: $yesterdayVal",
                color = TextMuted,
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = todayVal,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = deltaColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, deltaColor.copy(alpha = 0.35f))
            ) {
                Text(
                    text = if (deltaPercent > 0) "+$deltaPercent$unit" else "$deltaPercent$unit",
                    color = deltaColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun formatMinutesToHoursMinutes(mins: Int): String {
    val h = mins / 60
    val m = mins % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
