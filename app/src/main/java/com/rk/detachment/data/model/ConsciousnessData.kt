package com.rk.detachment.data.model

data class HabitLoopItem(
    val packageName: String,
    val appName: String,
    val openCount: Int,
    val timeSpanMinutes: Int,
    val avgSessionDurationSeconds: Int,
    val severity: String = "HIGH"
)

data class ConsciousnessMetrics(
    val score: Int = 78,
    val tierTitle: String = "Intentionally Present",
    val tierSubtitle: String = "High digital awareness with balanced screen discipline",
    val resistanceScore: Float = 0.85f,
    val intentionalityScore: Float = 0.78f,
    val unpluggedScore: Float = 0.72f,
    val disciplineScore: Float = 0.80f,
    val focusScore: Float = 0.70f,
    val unlockMindfulnessScore: Float = 0.75f,
    val totalUnlocks: Int = 28,
    val intentionalUnlocks: Int = 19,
    val habitualUnlocks: Int = 9,
    val mindlessSessionsCount: Int = 4,
    val intentionalSessionsCount: Int = 14,
    val unnecessaryUsageMinutes: Int = 22,
    val longestPhoneFreeMinutes: Int = 165,
    val totalPhoneFreeMinutes: Int = 640,
    val longestContinuousUsageMinutes: Int = 38,
    val overLimitMinutes: Int = 0,
    val distractionsResistedCount: Int = 6,
    val totalScreenTimeMinutes: Int = 115,
    val habitLoops: List<HabitLoopItem> = emptyList()
)

data class YouVsYouComparison(
    val today: ConsciousnessMetrics = ConsciousnessMetrics(),
    val yesterday: ConsciousnessMetrics = ConsciousnessMetrics(
        score = 64,
        tierTitle = "Scattered Attention",
        tierSubtitle = "Frequent quick checks and impulsive unlocks",
        resistanceScore = 0.40f,
        intentionalityScore = 0.50f,
        unpluggedScore = 0.45f,
        disciplineScore = 0.55f,
        focusScore = 0.35f,
        unlockMindfulnessScore = 0.48f,
        totalUnlocks = 49,
        intentionalUnlocks = 21,
        habitualUnlocks = 28,
        mindlessSessionsCount = 16,
        intentionalSessionsCount = 8,
        unnecessaryUsageMinutes = 74,
        longestPhoneFreeMinutes = 85,
        totalPhoneFreeMinutes = 410,
        longestContinuousUsageMinutes = 82,
        overLimitMinutes = 35,
        distractionsResistedCount = 2,
        totalScreenTimeMinutes = 225,
        habitLoops = listOf(
            HabitLoopItem(
                packageName = "com.instagram.android",
                appName = "Instagram",
                openCount = 8,
                timeSpanMinutes = 20,
                avgSessionDurationSeconds = 42,
                severity = "SEVERE"
            )
        )
    ),
    val scoreDelta: Int = +14,
    val screenTimeDeltaPercent: Int = -49,
    val unlocksDeltaPercent: Int = -43,
    val habitualUnlocksDeltaPercent: Int = -68,
    val phoneFreeDeltaPercent: Int = +94,
    val mindlessSessionsDeltaPercent: Int = -75
)
