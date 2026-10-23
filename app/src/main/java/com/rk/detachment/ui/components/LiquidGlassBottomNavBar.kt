package com.rk.detachment.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.NavigationTab
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.PurpleLight
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.PurpleSoft
import com.rk.detachment.ui.theme.TextMuted
import com.rk.detachment.ui.theme.TextSecondary

@Composable
fun LiquidGlassBottomNavBar(
    currentTab: NavigationTab,
    showConsciousnessScreen: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = PurplePrimary.copy(alpha = 0.45f),
                ambientColor = Color.Black.copy(alpha = 0.85f)
            ),
        shape = RoundedCornerShape(32.dp),
        color = FrostedBackgroundDarker.copy(alpha = 0.88f),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.65f),
                    Color.White.copy(alpha = 0.20f),
                    PurplePrimary.copy(alpha = 0.40f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.03f),
                            FrostedBackgroundDarker.copy(alpha = 0.50f)
                        )
                    )
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab && !showConsciousnessScreen
                    LiquidGlassNavTabItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag(tab.tag)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassNavTabItem(
    tab: NavigationTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_tab_scale"
    )

    val animatedIconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.65f,
        animationSpec = tween(durationMillis = 200),
        label = "nav_tab_icon_alpha"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "nav_tab_icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) PurpleSoft else TextMuted,
        animationSpec = tween(durationMillis = 200),
        label = "nav_tab_text_color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    val itemShape = RoundedCornerShape(22.dp)
    val itemBackground = if (isSelected) {
        Modifier.background(
            brush = Brush.verticalGradient(
                listOf(
                    PurplePrimary.copy(alpha = 0.40f),
                    PurplePrimary.copy(alpha = 0.18f)
                )
            ),
            shape = itemShape
        ).border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.55f),
                    PurpleLight.copy(alpha = 0.25f)
                )
            ),
            shape = itemShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .clip(itemShape)
            .then(itemBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(animatedIconAlpha)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = tab.title,
                color = textColor,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = if (isSelected) 0.2.sp else 0.sp
            )
        }
    }
}
