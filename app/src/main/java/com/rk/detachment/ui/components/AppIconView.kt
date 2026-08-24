package com.rk.detachment.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.util.AppManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppIconView(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    isLocked: Boolean = false,
    cornerRadius: Dp = 14.dp
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

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val borderColor = remember(isLocked) {
        if (isLocked) RoseAccent.copy(alpha = 0.6f) else GlassBorderMedium
    }
    val fallbackBgColor = remember(isLocked) {
        if (isLocked) RoseAccent.copy(alpha = 0.2f) else IndigoPrimary.copy(alpha = 0.2f)
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
                    .size(size * 0.85f)
                    .clip(RoundedCornerShape(cornerRadius * 0.7f))
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
