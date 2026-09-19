package com.rk.detachment.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.PurplePrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.util.AppManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

class CircularPetalShape(
    private val petalCount: Int = 6,
    private val petalDepth: Float = 0.08f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = minOf(centerX, centerY)
        val numPoints = 120

        for (i in 0 until numPoints) {
            val theta = (i.toFloat() / numPoints.toFloat()) * (2f * Math.PI.toFloat())
            val radius = baseRadius * (1f - petalDepth * (0.5f - 0.5f * cos(petalCount * theta)))
            val x = centerX + radius * cos(theta)
            val y = centerY + radius * sin(theta)
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun AppIconView(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    isLocked: Boolean = false,
    cornerRadius: Dp = 14.dp,
    shape: Shape = remember { CircularPetalShape(petalCount = 6, petalDepth = 0.08f) }
) {
    val context = LocalContext.current
    var bitmap by remember(packageName) {
        mutableStateOf<Bitmap?>(AppManagerHelper.getAppIconBitmapFromMemory(packageName))
    }

    LaunchedEffect(packageName) {
        if (bitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                AppManagerHelper.getAppIconBitmap(context, packageName)
            }
            bitmap = loaded
        }
    }

    val borderColor = remember(isLocked) {
        if (isLocked) RoseAccent.copy(alpha = 0.6f) else GlassBorderMedium
    }
    val fallbackBgColor = remember(isLocked) {
        if (isLocked) RoseAccent.copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(fallbackBgColor)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = appName,
                modifier = Modifier
                    .size(size * 0.88f)
                    .clip(shape)
            )
        } else {
            Text(
                text = appName.take(1).uppercase(),
                color = TextPrimary,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
