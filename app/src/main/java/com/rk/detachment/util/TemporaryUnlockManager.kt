package com.rk.detachment.util

import java.util.concurrent.ConcurrentHashMap

object TemporaryUnlockManager {
    private val unlockMap = ConcurrentHashMap<String, Long>()

    fun setUnlock(packageName: String, expiryMillis: Long) {
        unlockMap[packageName] = expiryMillis
    }

    fun removeUnlock(packageName: String) {
        unlockMap.remove(packageName)
    }

    fun isUnlocked(packageName: String, currentTime: Long = System.currentTimeMillis()): Boolean {
        val expiry = unlockMap[packageName] ?: return false
        if (expiry > currentTime) {
            return true
        }
        unlockMap.remove(packageName)
        return false
    }

    fun getExpiry(packageName: String): Long? {
        val expiry = unlockMap[packageName] ?: return null
        if (expiry > System.currentTimeMillis()) return expiry
        unlockMap.remove(packageName)
        return null
    }

    fun clearAll() {
        unlockMap.clear()
    }
}
