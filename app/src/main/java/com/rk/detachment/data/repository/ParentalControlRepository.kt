package com.rk.detachment.data.repository

import android.os.Build
import com.rk.detachment.data.local.dao.AppSettingsDao
import com.rk.detachment.data.local.entities.AppLimitEntity
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.model.ImportSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ParentalControlRepository(
    private val appSettingsDao: AppSettingsDao,
    private val detachmentRepository: DetachmentRepository
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val isManaged: Flow<Boolean> = appSettingsDao.observeValue(KEY_PARENTAL_IS_MANAGED).map { it == "true" }
    val pairingCode: Flow<String?> = appSettingsDao.observeValue(KEY_PARENTAL_CODE)
    val serverUrl: Flow<String?> = appSettingsDao.observeValue(KEY_PARENTAL_SERVER_URL)
    val lastSyncTime: Flow<Long> = appSettingsDao.observeValue(KEY_PARENTAL_LAST_SYNC).map { it?.toLongOrNull() ?: 0L }
    val lastSyncStatus: Flow<String?> = appSettingsDao.observeValue(KEY_PARENTAL_SYNC_STATUS)

    suspend fun getIsManaged(): Boolean = appSettingsDao.getValue(KEY_PARENTAL_IS_MANAGED) == "true"
    suspend fun getPairingCode(): String? = appSettingsDao.getValue(KEY_PARENTAL_CODE)
    suspend fun getServerUrl(): String = appSettingsDao.getValue(KEY_PARENTAL_SERVER_URL) ?: DEFAULT_SERVER_URL

    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    suspend fun registerDevice(
        rawUrl: String,
        desiredCode: String? = null,
        currentApps: List<AppLimitEntity> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = normalizeUrl(rawUrl)
            val endpoint = "$baseUrl/api/device/register"

            val payload = JSONObject()
            if (!desiredCode.isNullOrBlank()) {
              payload.put("code", desiredCode.trim().uppercase())
            }
            payload.put("deviceName", "${Build.MANUFACTURER} ${Build.MODEL}".trim())
            payload.put("battery", 100)

            val appsArray = JSONArray()
            for (app in currentApps) {
                val appObj = JSONObject()
                appObj.put("packageName", app.packageName)
                appObj.put("appName", app.appName)
                appObj.put("category", app.category)
                appObj.put("dailyLimitMinutes", app.dailyLimitMinutes)
                appObj.put("isDistracting", app.isDistracting)
                appObj.put("isEssential", app.isEssential)
                appObj.put("isShieldActive", app.isShieldActive)
                appObj.put("isLockedManually", app.isLockedManually)
                appsArray.put(appObj)
            }
            payload.put("apps", appsArray)

            val request = Request.Builder()
                .url(endpoint)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
                }
                val json = JSONObject(body)
                val code = json.optString("code")
                if (code.isBlank()) {
                    return@withContext Result.failure(Exception("Failed to obtain device pairing code"))
                }

                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SERVER_URL, baseUrl))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_CODE, code))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_IS_MANAGED, "true"))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_LAST_SYNC, System.currentTimeMillis().toString()))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Paired"))

                pullAndApplyConfig()

                Result.success(code)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pairExistingCode(
        rawUrl: String,
        code: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val baseUrl = normalizeUrl(rawUrl)
            val cleanCode = code.trim().uppercase()
            val endpoint = "$baseUrl/api/device/$cleanCode/config"

            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Device code not found on server ($response.code)"))
                }
                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) {
                    return@withContext Result.failure(Exception(json.optString("error", "Pairing failed")))
                }

                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SERVER_URL, baseUrl))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_CODE, cleanCode))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_IS_MANAGED, "true"))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_LAST_SYNC, System.currentTimeMillis().toString()))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Connected"))

                val configObj = json.optJSONObject("config")
                if (configObj != null) {
                    detachmentRepository.importBackupData(configObj.toString())
                }

                val currentApps = detachmentRepository.allApps.first()
                pushDeviceTelemetry(0, currentApps)

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullAndApplyConfig(): Result<ImportSummary> = withContext(Dispatchers.IO) {
        try {
            val isManaged = getIsManaged()
            val code = getPairingCode()
            val baseUrl = getServerUrl()

            if (!isManaged || code.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Parental Control is not paired"))
            }

            val endpoint = "$baseUrl/api/device/$code/config"
            val request = Request.Builder().url(endpoint).get().build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Sync failed (${response.code})"))
                    return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
                }

                val json = JSONObject(body)
                val configObj = json.optJSONObject("config")
                if (configObj == null) {
                    return@withContext Result.failure(Exception("Missing config in response"))
                }

                val summary = detachmentRepository.importBackupData(configObj.toString())
                val now = System.currentTimeMillis()
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_LAST_SYNC, now.toString()))
                appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Success"))

                Result.success(summary)
            }
        } catch (e: Exception) {
            appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Error: ${e.message}"))
            Result.failure(e)
        }
    }

    suspend fun pushDeviceTelemetry(
        screenTimeMinutes: Int,
        apps: List<AppLimitEntity>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isManaged = getIsManaged()
            val code = getPairingCode()
            val baseUrl = getServerUrl()

            if (!isManaged || code.isNullOrBlank()) {
                return@withContext Result.success(Unit)
            }

            val endpoint = "$baseUrl/api/device/$code/sync"
            val payload = JSONObject()
            payload.put("totalScreenTimeMinutes", screenTimeMinutes)

            val appsArray = JSONArray()
            for (app in apps) {
                val appObj = JSONObject()
                appObj.put("packageName", app.packageName)
                appObj.put("appName", app.appName)
                appObj.put("category", app.category)
                appObj.put("dailyLimitMinutes", app.dailyLimitMinutes)
                appObj.put("isDistracting", app.isDistracting)
                appObj.put("isEssential", app.isEssential)
                appObj.put("isShieldActive", app.isShieldActive)
                appObj.put("isLockedManually", app.isLockedManually)
                appsArray.put(appObj)
            }
            payload.put("apps", appsArray)

            val request = Request.Builder()
                .url(endpoint)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unpair(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val code = getPairingCode()
            val baseUrl = getServerUrl()
            if (!code.isNullOrBlank()) {
                try {
                    val endpoint = "$baseUrl/api/device/$code/unpair"
                    val request = Request.Builder()
                        .url(endpoint)
                        .post("{}".toRequestBody(jsonMediaType))
                        .build()
                    client.newCall(request).execute().close()
                } catch (_: Exception) {
                }
            }

            appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_IS_MANAGED, "false"))
            appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_CODE, ""))
            appSettingsDao.setSetting(AppSettingsEntity(KEY_PARENTAL_SYNC_STATUS, "Unpaired"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val DEFAULT_SERVER_URL = "https://detachment-parental.workers.dev"
        const val KEY_PARENTAL_IS_MANAGED = "parental_is_managed"
        const val KEY_PARENTAL_CODE = "parental_paired_code"
        const val KEY_PARENTAL_SERVER_URL = "parental_server_url"
        const val KEY_PARENTAL_LAST_SYNC = "parental_last_sync_time"
        const val KEY_PARENTAL_SYNC_STATUS = "parental_last_sync_status"
    }
}
