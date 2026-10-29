package com.rk.detachment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rk.detachment.data.local.entities.WebsiteLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteLimitDao {
    @Query("SELECT * FROM website_limits ORDER BY displayName ASC")
    fun getAllWebsitesFlow(): Flow<List<WebsiteLimitEntity>>

    @Query("SELECT * FROM website_limits WHERE isEnabled = 1")
    fun getEnabledWebsitesFlow(): Flow<List<WebsiteLimitEntity>>

    @Query("SELECT * FROM website_limits WHERE domain = :domain LIMIT 1")
    suspend fun getWebsiteByDomain(domain: String): WebsiteLimitEntity?

    @Query("SELECT COUNT(*) FROM website_limits")
    suspend fun getWebsitesCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultWebsites(websites: List<WebsiteLimitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsite(website: WebsiteLimitEntity)

    @Update
    suspend fun updateWebsite(website: WebsiteLimitEntity)

    @Query("UPDATE website_limits SET dailyLimitMinutes = :limitMinutes, isHardMode = :isHardMode WHERE domain = :domain")
    suspend fun updateLimit(domain: String, limitMinutes: Int, isHardMode: Boolean)

    @Query("UPDATE website_limits SET isLockedManually = :isLocked WHERE domain = :domain")
    suspend fun setManualLock(domain: String, isLocked: Boolean)

    @Query("UPDATE website_limits SET isEnabled = :isEnabled WHERE domain = :domain")
    suspend fun setEnabled(domain: String, isEnabled: Boolean)

    @Query("UPDATE website_limits SET isHardMode = :isHardMode WHERE domain = :domain")
    suspend fun setHardMode(domain: String, isHardMode: Boolean)

    @Query("UPDATE website_limits SET unlockExpiresAtMillis = :expiresAt WHERE domain = :domain")
    suspend fun setTemporaryUnlock(domain: String, expiresAt: Long)

    @Query("UPDATE website_limits SET unlockExpiresAtMillis = 0 WHERE domain = :domain")
    suspend fun cancelTemporaryUnlock(domain: String)

    @Query("UPDATE website_limits SET usedTodayMinutes = usedTodayMinutes + :minutes WHERE domain = :domain")
    suspend fun addUsage(domain: String, minutes: Int)

    @Query("UPDATE website_limits SET usedTodayMinutes = 0")
    suspend fun resetDailyUsage()

    @Query("UPDATE website_limits SET dailyLimitMinutes = 0")
    suspend fun resetAllLimitsToZero()

    @Query("DELETE FROM website_limits WHERE domain = :domain")
    suspend fun deleteWebsite(domain: String)
}
