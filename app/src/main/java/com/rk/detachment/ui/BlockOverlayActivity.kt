package com.rk.detachment.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.ui.components.AppIconView
import com.rk.detachment.ui.components.FrostedBadge
import com.rk.detachment.ui.components.FrostedGlassCard
import com.rk.detachment.ui.components.GlowingProgressRing
import com.rk.detachment.ui.components.PasscodeUnlockDialog
import com.rk.detachment.ui.components.RadialGlassBackground
import com.rk.detachment.ui.theme.AmberAccent
import com.rk.detachment.ui.theme.DetachmentTheme
import com.rk.detachment.ui.theme.EmeraldAccent
import com.rk.detachment.ui.theme.FrostedBackgroundDarker
import com.rk.detachment.ui.theme.GlassBorderLow
import com.rk.detachment.ui.theme.GlassBorderMedium
import com.rk.detachment.ui.theme.GlassSurfaceHigh
import com.rk.detachment.ui.theme.GlassSurfaceMedium
import com.rk.detachment.ui.theme.IndigoLight
import com.rk.detachment.ui.theme.IndigoPrimary
import com.rk.detachment.ui.theme.RoseAccent
import com.rk.detachment.ui.theme.TextPrimary
import com.rk.detachment.ui.theme.TextSecondary
import com.rk.detachment.util.AppManagerHelper
import com.rk.detachment.util.TemporaryUnlockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import kotlin.math.ceil

data class OverlayScreenData(
    val packageName: String,
    val appName: String,
    val category: String,
    val reason: String,
    val isFrictionDelay: Boolean,
    val delaySeconds: Int,
    val usedMinutes: Int,
    val limitMinutes: Int
)

class BlockOverlayActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_IS_FRICTION_DELAY = "extra_is_friction_delay"
        const val EXTRA_DELAY_SECONDS = "extra_delay_seconds"
        const val EXTRA_USED_MINUTES = "extra_used_minutes"
        const val EXTRA_LIMIT_MINUTES = "extra_limit_minutes"

        @Volatile
        var activeInstance: BlockOverlayActivity? = null
            private set

        @Volatile
        var currentActivePackage: String? = null
            private set

        fun dismissIfActive() {
            try {
                activeInstance?.let { activity ->
                    activeInstance = null
                    currentActivePackage = null
                    activity.finishAndRemoveTask()
                }
            } catch (e: Exception) {
            }
        }
    }

    private val overlayDataState = mutableStateOf<OverlayScreenData?>(null)

    private fun parseOverlayData(srcIntent: Intent): OverlayScreenData {
        return OverlayScreenData(
            packageName = srcIntent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "",
            appName = srcIntent.getStringExtra(EXTRA_APP_NAME) ?: "Application",
            category = srcIntent.getStringExtra(EXTRA_CATEGORY) ?: "Apps",
            reason = srcIntent.getStringExtra(EXTRA_REASON) ?: "App blocked by Detachment",
            isFrictionDelay = srcIntent.getBooleanExtra(EXTRA_IS_FRICTION_DELAY, false),
            delaySeconds = srcIntent.getIntExtra(EXTRA_DELAY_SECONDS, 15),
            usedMinutes = srcIntent.getIntExtra(EXTRA_USED_MINUTES, 0),
            limitMinutes = srcIntent.getIntExtra(EXTRA_LIMIT_MINUTES, 0)
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newData = parseOverlayData(intent)
        if (newData.packageName.isNotBlank() && 
            newData.packageName != packageName && 
            newData.packageName != applicationContext.packageName &&
            !newData.packageName.startsWith("com.rk.detachment")) {
            currentActivePackage = newData.packageName
            overlayDataState.value = newData
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val initialData = parseOverlayData(intent)
        if (initialData.packageName.isBlank() || 
            initialData.packageName == packageName || 
            initialData.packageName == applicationContext.packageName ||
            initialData.packageName.startsWith("com.rk.detachment")) {
            activeInstance = null
            currentActivePackage = null
            finishAndRemoveTask()
            return
        }

        activeInstance = this
        currentActivePackage = initialData.packageName
        overlayDataState.value = initialData

        setContent {
            DetachmentTheme {
                BackHandler {
                    recordDistractionResisted()
                    returnToHome()
                }

                var defaultUnlockMinutes by remember { mutableIntStateOf(15) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(applicationContext, this)
                        defaultUnlockMinutes = db.appSettingsDao().getValue("key_unlock_minutes")?.toIntOrNull() ?: 15
                    }
                }

                val currentData = overlayDataState.value ?: parseOverlayData(intent)
                if (currentData.packageName.isBlank()) {
                    LaunchedEffect(Unit) {
                        finishAndRemoveTask()
                    }
                } else {
                    RadialGlassBackground(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        AnimatedContent(
                            targetState = currentData,
                            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                            label = "overlay_content_switch"
                        ) { data ->
                            if (data.isFrictionDelay) {
                                RealFrictionDelayView(
                                    packageName = data.packageName,
                                    appName = data.appName,
                                    category = data.category,
                                    delaySeconds = data.delaySeconds,
                                    onProceed = {
                                        grantDelayProceedAndLaunch(data.packageName)
                                    },
                                    onClose = {
                                        recordDistractionResisted()
                                        returnToHome()
                                    }
                                )
                            } else {
                                RealLockScreenView(
                                    packageName = data.packageName,
                                    appName = data.appName,
                                    category = data.category,
                                    reason = data.reason,
                                    usedMinutes = data.usedMinutes,
                                    limitMinutes = data.limitMinutes,
                                    unlockMinutes = defaultUnlockMinutes,
                                    onUnlock = { chosenMinutes ->
                                        grantTemporaryUnlockAndLaunch(data.packageName, chosenMinutes)
                                    },
                                    onClose = {
                                        recordDistractionResisted()
                                        returnToHome()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activeInstance = this
        val currentPkg = overlayDataState.value?.packageName
        if (!currentPkg.isNullOrBlank()) {
            currentActivePackage = currentPkg
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activeInstance == this) {
            activeInstance = null
            currentActivePackage = null
        }
    }

    private fun returnToHome() {
        activeInstance = null
        currentActivePackage = null
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        } catch (e: Exception) {
        }
        finishAndRemoveTask()
    }

    private fun grantDelayProceedAndLaunch(packageName: String) {
        TemporaryUnlockManager.setDelaySessionActive(packageName)
        activeInstance = null
        currentActivePackage = null
        AppManagerHelper.launchRealApp(this@BlockOverlayActivity, packageName)
        finishAndRemoveTask()
    }

    private fun grantTemporaryUnlockAndLaunch(packageName: String, minutes: Int) {
        val expiry = System.currentTimeMillis() + (minutes * 60 * 1000L)
        TemporaryUnlockManager.setUnlock(packageName, expiry)
        activeInstance = null
        currentActivePackage = null

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)
            db.appLimitDao().setTemporaryUnlock(packageName, expiry)
            withContext(Dispatchers.Main) {
                AppManagerHelper.launchRealApp(this@BlockOverlayActivity, packageName)
                finishAndRemoveTask()
            }
        }
    }

    private fun recordDistractionResisted() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext, this)
            val current = db.appSettingsDao().getValue("distractions_resisted")?.toIntOrNull() ?: 0
            db.appSettingsDao().setSetting(AppSettingsEntity("distractions_resisted", (current + 1).toString()))
        }
    }
}

@Composable
fun RealLockScreenView(
    packageName: String,
    appName: String,
    category: String,
    reason: String,
    usedMinutes: Int,
    limitMinutes: Int,
    unlockMinutes: Int = 15,
    onUnlock: (Int) -> Unit,
    onClose: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var masterPin by remember { mutableStateOf("1234") }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context, scope)
            masterPin = db.appSettingsDao().getValue("master_pin") ?: "1234"
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_lock")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RoseAccent.copy(alpha = 0.35f),
                            RoseAccent.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, RoseAccent.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = RoseAccent,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = GlassSurfaceHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconView(
                    packageName = packageName,
                    appName = appName,
                    size = 56.dp,
                    isLocked = true,
                    cornerRadius = 16.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = appName,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                FrostedBadge(
                    text = category,
                    color = IndigoLight
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseAccent.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoseAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reason,
                        color = RoseAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (limitMinutes > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Today's Usage: ${usedMinutes}m",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Daily Limit: ${limitMinutes}m",
                            color = IndigoLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (usedMinutes.toFloat() / limitMinutes.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RoseAccent,
                        trackColor = Color(0x22FFFFFF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("block_return_home_btn")
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Close & Stay Focused",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showPinDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
            border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("block_emergency_unlock_btn")
        ) {
            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emergency PIN Unlock",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = AmberAccent
            )
        }
    }

    if (showPinDialog) {
        PasscodeUnlockDialog(
            appName = appName,
            unlockMinutes = unlockMinutes,
            pinLength = if (masterPin.length == 6) 6 else 4,
            onVerifyPin = { pin -> pin == masterPin },
            onUnlockSuccess = { chosenMinutes ->
                showPinDialog = false
                onUnlock(chosenMinutes)
            },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
fun RealFrictionDelayView(
    packageName: String,
    appName: String,
    category: String,
    delaySeconds: Int = 15,
    onProceed: () -> Unit,
    onClose: () -> Unit
) {
    val safeDelaySeconds = delaySeconds.coerceAtLeast(1)
    val progressAnim = remember(safeDelaySeconds) { Animatable(0f) }
    var isDelayFinished by remember(safeDelaySeconds) { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing_cycle")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_ring_scale"
    )
    val breathingGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_glow_alpha"
    )

    LaunchedEffect(safeDelaySeconds) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = safeDelaySeconds * 1000,
                easing = LinearEasing
            )
        )
        isDelayFinished = true
    }

    val currentProgress = progressAnim.value
    val secondsRemaining = if (isDelayFinished || currentProgress >= 1f) {
        0
    } else {
        ceil((1f - currentProgress) * safeDelaySeconds).toInt().coerceIn(1, safeDelaySeconds)
    }

    val elapsedSeconds = (currentProgress * safeDelaySeconds).toInt()
    val breathPhase = when (elapsedSeconds % 8) {
        in 0..3 -> "Inhale calm & clarity..."
        in 4..7 -> "Exhale the impulse to scroll..."
        else -> "Pause mindfully..."
    }

    val quotes = remember {
        listOf(
            "Do you truly need this right now, or is it just a habitual dopamine loop?",
            "Almost everything will work again if you unplug it for a few minutes, including you.",
            "Detachment is the key to deep focus and masterwork.",
            "Give yourself the space to choose with clarity.",
            "Before you unlock your phone, know what you came to do.",
            "A notification is an invitation, not an obligation.",
            "Don’t let a device designed to serve you decide where your attention goes.",
            "If you don’t have a reason to unlock it, don’t.",
            "Your attention is yours. Spend it deliberately.",
            "Not every moment of boredom needs to be filled.",
            "The phone can wait. Your life is happening now.",
            "Reach for your phone with intention, not impulse.",
            "Every unnecessary scroll is a small decision to give your time away.",
            "Use your phone as a tool, not as a place to escape.",
            "Ask yourself: am I using my phone, or is my phone using me?",
            "You don’t need to check what hasn’t asked for your attention.",
            "Silence the screen before it silences your thoughts.",
            "A quiet mind begins with fewer things demanding its attention.",
            "Don’t confuse stimulation with fulfillment.",
            "You opened your phone for a reason. Remember what it was.",
            "If five minutes of scrolling became an hour, it wasn’t a break.",
            "Boredom is not a problem that requires a screen.",
            "Protect the moments in which nothing is happening. They are where thought begins.",
            "The ability to put your phone down is a form of freedom.",
            "You can be reachable without being constantly available.",
            "Your phone should fit into your life, not become the center of it.",
            "Before consuming something, ask whether it deserves a piece of your attention.",
            "Don’t trade a meaningful moment for a meaningless refresh.",
            "When you feel the urge to check, pause. An urge is not an instruction.",
            "Leave the phone behind sometimes. Your mind needs places where nothing is asking for it.",
            "The goal isn’t to use your phone less. It’s to use it only when it adds something.",
            "Convenience becomes a trap when you stop choosing.",
            "Put the screen down long enough to notice what you were avoiding.",
            "Your time disappears quietly when your attention is constantly interrupted.",
            "A phone can connect you to the world while disconnecting you from the moment.",
            "Don’t fill every empty second. Some empty seconds are valuable.",
            "Choose your next action before the algorithm chooses it for you.",
            "Every unlock is a question: is this where I want my attention right now?",
            "Your life deserves more attention than your screen.",
            "The strongest form of digital discipline is simply being comfortable without checking.",
            "You don't need to respond to every impulse your phone creates.",
            "Don’t let a few seconds of impulse steal an hour of presence.",
            "Your attention is a finite resource. Spend it like it matters.",
            "A phone in your hand should be a choice, not a reflex.",
            "Disconnecting from the screen is reconnecting with yourself.",
            "Master your attention before you try to master your time."
        )
    }
    val quoteIndex = remember { (0 until quotes.size).random() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIconView(
            packageName = packageName,
            appName = appName,
            size = 56.dp,
            isLocked = false,
            cornerRadius = 16.dp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${safeDelaySeconds}-Second Mindful Delay",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Detachment Distraction Shield for $appName",
            color = IndigoLight,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(if (!isDelayFinished) breathingScale else 1f),
            contentAlignment = Alignment.Center
        ) {
            if (!isDelayFinished) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AmberAccent.copy(alpha = breathingGlowAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            GlowingProgressRing(
                progress = currentProgress,
                modifier = Modifier.size(180.dp),
                strokeWidth = 10.dp,
                primaryColor = if (isDelayFinished) EmeraldAccent else AmberAccent,
                secondaryColor = if (isDelayFinished) EmeraldAccent else AmberAccent
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isDelayFinished) "0" else "$secondsRemaining",
                        color = if (isDelayFinished) EmeraldAccent else TextPrimary,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isDelayFinished) "Completed" else "seconds left",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FrostedBackgroundDarker,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderMedium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = breathPhase,
                    color = IndigoLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${quotes[quoteIndex]}\"",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("friction_resist_btn")
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Resist & Close App",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onProceed,
            enabled = isDelayFinished,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldAccent,
                disabledContainerColor = Color(0x22FFFFFF)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("friction_proceed_btn")
        ) {
            Text(
                text = if (isDelayFinished) "Proceed Mindfully" else "Think before Launch",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDelayFinished) Color.Black else TextSecondary
            )
            if (isDelayFinished) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
    }
}
