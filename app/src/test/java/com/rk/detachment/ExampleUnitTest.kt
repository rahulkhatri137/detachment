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
        val expiry = now + 15 * 60 * 1000L // 15 mins

        TemporaryUnlockManager.setUnlock(testPkg, expiry)
        assertTrue(TemporaryUnlockManager.isUnlocked(testPkg, now))
        assertTrue(TemporaryUnlockManager.isUnlocked(testPkg, now + 1000L))
        assertFalse(TemporaryUnlockManager.isUnlocked(testPkg, expiry + 1L))
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
