package com.rk.detachment.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.rk.detachment.util.HeadsUpPillData

@Composable
fun HeadsUpNotchPillOverlay(
    pillData: HeadsUpPillData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val topPadding = if (isLandscape) 8.dp else 24.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = topPadding)
            .zIndex(9999f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = pillData != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            ) + fadeIn() + scaleIn(initialScale = 0.85f),
            exit = slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 240, easing = FastOutLinearInEasing)
            ) + fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.85f, animationSpec = tween(240))
        ) {
            if (pillData != null) {
                HeadsUpNotchPillContent(
                    data = pillData,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun HeadsUpNotchPillContent(
    data: HeadsUpPillData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val formattedTime = if (data.minutesUsed >= 60) {
        val hrs = data.minutesUsed / 60
        val mins = data.minutesUsed % 60
        if (mins == 0) "${hrs}h" else "${hrs}h ${mins}m"
    } else {
        "${data.minutesUsed} min"
    }

    val pillBorderBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.75f),
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.40f)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.5f),
                ambientColor = Color.Black.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .background(com.rk.detachment.ui.theme.GlassPillBackground)
            .border(BorderStroke(1.2.dp, pillBorderBrush), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDismiss
            )
            .testTag("heads_up_notch_pill")
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        endY = 40f
                    )
                )
        )

        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
                    .border(BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppIconView(
                    packageName = data.packageName,
                    appName = data.appName,
                    size = 22.dp,
                    cornerRadius = 11.dp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.12f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.30f))
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
