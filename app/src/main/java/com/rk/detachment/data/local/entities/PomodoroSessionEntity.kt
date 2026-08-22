package com.rk.detachment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val completedAtMillis: Long = System.currentTimeMillis(),
    val tag: String = "Focus",
    val distractionsBlocked: Int = 0
)
