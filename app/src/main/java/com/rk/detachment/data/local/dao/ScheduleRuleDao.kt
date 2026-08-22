package com.rk.detachment.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rk.detachment.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleRuleDao {
    @Query("SELECT * FROM schedule_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<ScheduleRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ScheduleRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ScheduleRuleEntity>)

    @Update
    suspend fun updateRule(rule: ScheduleRuleEntity)

    @Delete
    suspend fun deleteRule(rule: ScheduleRuleEntity)

    @Query("UPDATE schedule_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleRule(id: Int, isEnabled: Boolean)
}
