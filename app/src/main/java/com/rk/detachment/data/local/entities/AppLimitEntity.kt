package com.rk.detachment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val iconName: String,
    val category: String,
    val dailyLimitMinutes: Int = 30,
    val usedTodayMinutes: Int = 0,
    val isDistracting: Boolean = false,
    val isEssential: Boolean = false,
    val isLockedManually: Boolean = false,
    val unlockExpiresAtMillis: Long = 0L,
    val todayOpens: Int = 0
) {
    val isLimitExceeded: Boolean
        get() = dailyLimitMinutes > 0 && usedTodayMinutes >= dailyLimitMinutes

    fun isTemporaryUnlocked(currentTime: Long = System.currentTimeMillis()): Boolean {
        return unlockExpiresAtMillis > currentTime
    }

    fun isCurrentlyLocked(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (isTemporaryUnlocked(currentTime)) return false
        return isLockedManually || isLimitExceeded
    }

    fun remainingUnlockSeconds(currentTime: Long = System.currentTimeMillis()): Long {
        if (unlockExpiresAtMillis <= currentTime) return 0L
        return (unlockExpiresAtMillis - currentTime) / 1000L
    }
}
