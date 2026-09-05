package com.rk.detachment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rk.detachment.data.local.entities.AppLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limits ORDER BY usedTodayMinutes DESC")
    fun getAllApps(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE isDistracting = 1")
    fun getDistractingApps(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE isEssential = 1")
    fun getEssentialApps(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE isShieldActive = 1")
    fun getShieldActiveApps(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): AppLimitEntity?

    @Query("SELECT COUNT(*) FROM app_limits WHERE isEssential = 1")
    fun getEssentialCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppLimitEntity>)

    @Query("DELETE FROM app_limits WHERE packageName NOT IN (:packageNames)")
    suspend fun deleteAppsNotIn(packageNames: List<String>)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppLimitEntity)

    @Update
    suspend fun updateApp(app: AppLimitEntity)

    @Query("UPDATE app_limits SET dailyLimitMinutes = :limitMinutes WHERE packageName = :packageName")
    suspend fun updateLimit(packageName: String, limitMinutes: Int)

    @Query("UPDATE app_limits SET usedTodayMinutes = :minutes WHERE packageName = :packageName")
    suspend fun updateUsedMinutes(packageName: String, minutes: Int)

    @Query("UPDATE app_limits SET usedTodayMinutes = usedTodayMinutes + :additionalMinutes WHERE packageName = :packageName")
    suspend fun addUsage(packageName: String, additionalMinutes: Int)

    @Query("UPDATE app_limits SET isDistracting = :isDistracting WHERE packageName = :packageName")
    suspend fun setDistracting(packageName: String, isDistracting: Boolean)

    @Query("UPDATE app_limits SET isEssential = :isEssential WHERE packageName = :packageName")
    suspend fun setEssential(packageName: String, isEssential: Boolean)

    @Query("UPDATE app_limits SET isShieldActive = :isShieldActive WHERE packageName = :packageName")
    suspend fun setShieldActive(packageName: String, isShieldActive: Boolean)

    @Query("UPDATE app_limits SET isLockedManually = :isLocked WHERE packageName = :packageName")
    suspend fun setManualLock(packageName: String, isLocked: Boolean)

    @Query("UPDATE app_limits SET unlockExpiresAtMillis = :expiresAt WHERE packageName = :packageName")
    suspend fun setTemporaryUnlock(packageName: String, expiresAt: Long)

    @Query("UPDATE app_limits SET unlockExpiresAtMillis = 0 WHERE packageName = :packageName")
    suspend fun cancelTemporaryUnlock(packageName: String)

    @Query("UPDATE app_limits SET todayOpens = todayOpens + 1 WHERE packageName = :packageName")
    suspend fun incrementOpens(packageName: String)

    @Query("UPDATE app_limits SET category = :category WHERE packageName = :packageName")
    suspend fun updateCategory(packageName: String, category: String)
}

