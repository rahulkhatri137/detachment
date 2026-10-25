package com.rk.detachment.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.RoseAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun createLotusPetalPath(size: Size, progress: Float): Path {
    val path = Path()
    if (size.width <= 0f || size.height <= 0f || progress <= 0f) {
        return path
    }
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val maxD = sqrt(cx * cx + cy * cy) * 1.45f
    val currentR = maxD * progress
    val rValley = 0.35f + 0.45f * progress
    val rotation = (1f - progress) * 0.4f

    val steps = 120
    for (step in 0..steps) {
        val theta = (step.toFloat() / steps.toFloat()) * (2f * PI.toFloat())
        val alpha = 8f * (theta - rotation)
        val u = ((cos(alpha) + 1f) * 0.5f).coerceIn(0f, 1f)
        val petalCurve = u.toDouble().pow(0.65).toFloat().coerceIn(0f, 1f)
        val r = currentR * (rValley + (1f - rValley) * petalCurve)
        val x = cx + r * cos(theta)
        val y = cy + r * sin(theta)
        if (x.isNaN() || y.isNaN()) continue
        if (step == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

@Composable
fun LotusBloomContainer(
    modifier: Modifier = Modifier,
    triggerKey: Any? = Unit,
    durationMillis: Int = 1200,
    backgroundColor: Color = FrostedBackgroundDarker,
    content: @Composable BoxScope.() -> Unit
) {
    val bloomAnim = remember { Animatable(0f) }
    LaunchedEffect(triggerKey) {
        bloomAnim.snapTo(0f)
        bloomAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            )
        )
    }

    val bloomProgress = bloomAnim.value
    val contentScale = remember(bloomProgress) {
        0.86f + 0.14f * bloomProgress
    }
    val contentAlpha = remember(bloomProgress) {
        (bloomProgress * 1.5f).coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (bloomProgress >= 1f) {
                        drawContent()
                    } else if (bloomProgress > 0.001f && size.width > 0f && size.height > 0f) {
                        val path = createLotusPetalPath(size, bloomProgress)
                        clipPath(path) {
                            this@drawWithContent.drawContent()
                        }
                        if (bloomProgress in 0.02f..0.98f) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxD = sqrt(cx * cx + cy * cy) * 1.45f
                            drawPath(
                                path = path,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        RoseAccent.copy(alpha = 0.85f * (1f - bloomProgress)),
                                        PurpleLight.copy(alpha = 0.5f * (1f - bloomProgress)),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = (maxD * bloomProgress * 1.05f).coerceAtLeast(1f)
                                ),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                    alpha = contentAlpha
                    transformOrigin = TransformOrigin.Center
                },
            content = content
        )
    }
}
