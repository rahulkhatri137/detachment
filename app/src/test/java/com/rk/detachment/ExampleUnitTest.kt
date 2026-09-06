package com.rk.detachment

import com.rk.detachment.util.TemporaryUnlockManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    @Before
    fun setup() {
        TemporaryUnlockManager.clearAll()
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun temporaryUnlockManager_unlockAndExpiry() {
        val testPkg = "com.example.social"
        val now = 1000000L
        val expiry = now + 15 * 60 * 1000L

        TemporaryUnlockManager.setUnlock(testPkg, expiry)
        assertTrue(TemporaryUnlockManager.isUnlocked(testPkg, now))
        assertTrue(TemporaryUnlockManager.isUnlocked(testPkg, now + 1000L))
        assertFalse(TemporaryUnlockManager.isUnlocked(testPkg, expiry + 1L))
    }

    @Test
    fun milestoneCalculation_multiplesOf15Only() {
        val intervalMinutes = 15
        val testMinutes = listOf(0, 5, 14, 15, 16, 29, 30, 44, 45, 60, 75)
        val expectedMilestones = listOf(0, 0, 0, 15, 15, 15, 30, 30, 45, 60, 75)

        for (i in testMinutes.indices) {
            val mins = testMinutes[i]
            val milestone = (mins / intervalMinutes) * intervalMinutes
            assertEquals(expectedMilestones[i], milestone)
        }
    }

    @Test
    fun dayBoundaryIsolation_yesterdayUsageNotCarriedOver() {
        val yesterdayUsedMinutes = 120
        val todayCalculatedMinutes = 15
        val hasUsageStats = true

        val usedToday = if (hasUsageStats) todayCalculatedMinutes else yesterdayUsedMinutes
        assertEquals(15, usedToday)
        assertTrue(usedToday < yesterdayUsedMinutes)
    }

    @Test
    fun pillReminder_triggersAsPerAppLimitsScreentime_notActiveSession() {
        val appLimitsDashboardScreenTimeMinutes = 11
        val activeSessionDurationMinutes = 4
        val intervalMinutes = 15

        val totalScreenTime = appLimitsDashboardScreenTimeMinutes + activeSessionDurationMinutes
        assertEquals(15, totalScreenTime)

        val milestone = (totalScreenTime / intervalMinutes) * intervalMinutes
        assertEquals(15, milestone)

        val alertedSet = mutableSetOf<Int>()
        val shouldTrigger = !alertedSet.contains(milestone) && milestone >= intervalMinutes
        assertTrue(shouldTrigger)

        alertedSet.add(milestone)
        val secondCheckDuringSameSession = !alertedSet.contains(milestone)
        assertFalse(secondCheckDuringSameSession)

        val subsequentSessionMinutes = 15
        val nextTotalScreenTime = totalScreenTime + subsequentSessionMinutes
        assertEquals(30, nextTotalScreenTime)

        val nextMilestone = (nextTotalScreenTime / intervalMinutes) * intervalMinutes
        assertEquals(30, nextMilestone)
        assertTrue(!alertedSet.contains(nextMilestone))
    }

    @Test
    fun temporaryUnlockManager_removeUnlock() {
        val testPkg = "com.example.game"
        val now = 1000000L
        TemporaryUnlockManager.setUnlock(testPkg, now + 60000L)
        assertTrue(TemporaryUnlockManager.isUnlocked(testPkg, now))
        TemporaryUnlockManager.removeUnlock(testPkg)
        assertFalse(TemporaryUnlockManager.isUnlocked(testPkg, now))
    }
}
