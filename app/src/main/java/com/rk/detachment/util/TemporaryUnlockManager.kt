package com.rk.detachment.util

import java.util.concurrent.ConcurrentHashMap

object TemporaryUnlockManager {
    private val unlockMap = ConcurrentHashMap<String, Long>()
    private val activeDelaySessions = ConcurrentHashMap.newKeySet<String>()

    fun setUnlock(packageName: String, expiryMillis: Long) {
        unlockMap[packageName] = expiryMillis
        activeDelaySessions.add(packageName)
    }

    fun removeUnlock(packageName: String) {
        unlockMap.remove(packageName)
        activeDelaySessions.remove(packageName)
    }

    fun isUnlocked(packageName: String, currentTime: Long = System.currentTimeMillis()): Boolean {
        val expiry = unlockMap[packageName] ?: return false
        if (expiry > currentTime) {
            return true
        }
        unlockMap.remove(packageName)
        return false
    }

    fun setDelaySessionActive(packageName: String) {
        activeDelaySessions.add(packageName)
    }

    fun isDelaySessionActive(packageName: String): Boolean {
        return activeDelaySessions.contains(packageName)
    }

    fun endDelaySession(packageName: String) {
        activeDelaySessions.remove(packageName)
    }

    fun clearAllDelaySessions() {
        activeDelaySessions.clear()
    }

    fun getExpiry(packageName: String): Long? {
        val expiry = unlockMap[packageName] ?: return null
        if (expiry > System.currentTimeMillis()) return expiry
        unlockMap.remove(packageName)
        return null
    }

    fun clearAll() {
        unlockMap.clear()
        activeDelaySessions.clear()
    }
}
