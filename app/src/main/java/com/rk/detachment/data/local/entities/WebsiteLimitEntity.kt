package com.rk.detachment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "website_limits")
data class WebsiteLimitEntity(
    @PrimaryKey val domain: String,
    val displayName: String,
    val category: String = "Social",
    val dailyLimitMinutes: Int = 0,
    val usedTodayMinutes: Int = 0,
    val isDistracting: Boolean = true,
    val isLockedManually: Boolean = false,
    val unlockExpiresAtMillis: Long = 0L,
    val isHardMode: Boolean = false,
    val isEnabled: Boolean = true
) {
    val isLimitExceeded: Boolean
        get() = isEnabled && dailyLimitMinutes > 0 && usedTodayMinutes >= dailyLimitMinutes

    fun isTemporaryUnlocked(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (isHardMode) return false
        return unlockExpiresAtMillis > currentTime
    }

    fun isCurrentlyLocked(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (!isEnabled) return false
        if (isTemporaryUnlocked(currentTime)) return false
        return isLockedManually || isLimitExceeded
    }

    fun remainingUnlockSeconds(currentTime: Long = System.currentTimeMillis()): Long {
        if (isHardMode || unlockExpiresAtMillis <= currentTime) return 0L
        return (unlockExpiresAtMillis - currentTime) / 1000L
    }
}
