package com.rk.detachment.data.model

import org.json.JSONArray
import org.json.JSONObject

data class AppLimitBackup(
    val packageName: String,
    val appName: String,
    val category: String,
    val dailyLimitMinutes: Int,
    val isDistracting: Boolean,
    val isEssential: Boolean,
    val isShieldActive: Boolean,
    val isLockedManually: Boolean
)

data class ScheduleRuleBackup(
    val title: String,
    val type: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val activeDays: String,
    val isEnabled: Boolean,
    val blockedTarget: String
)

data class ImportSummary(
    val appsUpdated: Int,
    val schedulesRestored: Int,
    val settingsRestored: Int
)

data class DetachmentBackup(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val appLimits: List<AppLimitBackup>,
    val schedules: List<ScheduleRuleBackup>,
    val settings: Map<String, String>
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("version", version)
        root.put("exportDate", exportDate)

        val appArray = JSONArray()
        for (app in appLimits) {
            val appObj = JSONObject()
            appObj.put("packageName", app.packageName)
            appObj.put("appName", app.appName)
            appObj.put("category", app.category)
            appObj.put("dailyLimitMinutes", app.dailyLimitMinutes)
            appObj.put("isDistracting", app.isDistracting)
            appObj.put("isEssential", app.isEssential)
            appObj.put("isShieldActive", app.isShieldActive)
            appObj.put("isLockedManually", app.isLockedManually)
            appArray.put(appObj)
        }
        root.put("appLimits", appArray)

        val schedArray = JSONArray()
        for (rule in schedules) {
            val schedObj = JSONObject()
            schedObj.put("title", rule.title)
            schedObj.put("type", rule.type)
            schedObj.put("startHour", rule.startHour)
            schedObj.put("startMinute", rule.startMinute)
            schedObj.put("endHour", rule.endHour)
            schedObj.put("endMinute", rule.endMinute)
            schedObj.put("activeDays", rule.activeDays)
            schedObj.put("isEnabled", rule.isEnabled)
            schedObj.put("blockedTarget", rule.blockedTarget)
            schedArray.put(schedObj)
        }
        root.put("schedules", schedArray)

        val settingsObj = JSONObject()
        for ((k, v) in settings) {
            settingsObj.put(k, v)
        }
        root.put("settings", settingsObj)

        return root.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): DetachmentBackup {
            val root = JSONObject(jsonStr)
            val version = root.optInt("version", 1)
            val exportDate = root.optLong("exportDate", System.currentTimeMillis())

            val appLimitsList = mutableListOf<AppLimitBackup>()
            val appArray = root.optJSONArray("appLimits")
            if (appArray != null) {
                for (i in 0 until appArray.length()) {
                    val item = appArray.getJSONObject(i)
                    val pkg = item.optString("packageName", "")
                    if (pkg.isNotBlank()) {
                        appLimitsList.add(
                            AppLimitBackup(
                                packageName = pkg,
                                appName = item.optString("appName", ""),
                                category = item.optString("category", "General"),
                                dailyLimitMinutes = item.optInt("dailyLimitMinutes", 0),
                                isDistracting = item.optBoolean("isDistracting", false),
                                isEssential = item.optBoolean("isEssential", false),
                                isShieldActive = item.optBoolean("isShieldActive", false),
                                isLockedManually = item.optBoolean("isLockedManually", false)
                            )
                        )
                    }
                }
            }

            val schedulesList = mutableListOf<ScheduleRuleBackup>()
            val schedArray = root.optJSONArray("schedules")
            if (schedArray != null) {
                for (i in 0 until schedArray.length()) {
                    val item = schedArray.getJSONObject(i)
                    val title = item.optString("title", "")
                    if (title.isNotBlank()) {
                        schedulesList.add(
                            ScheduleRuleBackup(
                                title = title,
                                type = item.optString("type", "CUSTOM"),
                                startHour = item.optInt("startHour", 9),
                                startMinute = item.optInt("startMinute", 0),
                                endHour = item.optInt("endHour", 17),
                                endMinute = item.optInt("endMinute", 0),
                                activeDays = item.optString("activeDays", "MON,TUE,WED,THU,FRI,SAT,SUN"),
                                isEnabled = item.optBoolean("isEnabled", true),
                                blockedTarget = item.optString("blockedTarget", "DISTRACTING")
                            )
                        )
                    }
                }
            }

            val settingsMap = mutableMapOf<String, String>()
            val settingsObj = root.optJSONObject("settings")
            if (settingsObj != null) {
                val keys = settingsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    settingsMap[key] = settingsObj.optString(key, "")
                }
            }

            return DetachmentBackup(
                version = version,
                exportDate = exportDate,
                appLimits = appLimitsList,
                schedules = schedulesList,
                settings = settingsMap
            )
        }
    }
}
