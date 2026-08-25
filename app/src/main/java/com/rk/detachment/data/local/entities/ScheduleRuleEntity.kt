package com.rk.detachment.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "schedule_rules")
data class ScheduleRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val activeDays: String = "MON,TUE,WED,THU,FRI,SAT,SUN",
    val isEnabled: Boolean = true,
    val blockedTarget: String = "DISTRACTING"
) {
    fun isCurrentlyActive(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (!isEnabled) return false

        val currentDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }

        if (!activeDays.contains("ALL") && !activeDays.contains(currentDay)) {
            return false
        }

        val currentMinuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMinuteOfDay = startHour * 60 + startMinute
        val endMinuteOfDay = endHour * 60 + endMinute

        return if (startMinuteOfDay <= endMinuteOfDay) {
            currentMinuteOfDay in startMinuteOfDay..endMinuteOfDay
        } else {
            currentMinuteOfDay >= startMinuteOfDay || currentMinuteOfDay <= endMinuteOfDay
        }
    }

    fun formattedTimeRange(): String {
        val startStr = String.format("%02d:%02d", startHour, startMinute)
        val endStr = String.format("%02d:%02d", endHour, endMinute)
        return "$startStr - $endStr"
    }
}
