package com.rk.detachment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rk.detachment.data.local.entities.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY completedAtMillis DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("SELECT COUNT(*) FROM pomodoro_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions")
    fun getTotalFocusMinutes(): Flow<Int?>
}
