package com.rk.detachment.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.repository.DetachmentRepository
import com.rk.detachment.data.repository.ParentalControlRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ParentalSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val scope = CoroutineScope(Dispatchers.IO)
            val db = AppDatabase.getDatabase(applicationContext, scope)
            val detachmentRepo = DetachmentRepository(
                db.appLimitDao(),
                db.scheduleRuleDao(),
                db.pomodoroDao(),
                db.appSettingsDao()
            )
            val parentalRepo = ParentalControlRepository(
                db.appSettingsDao(),
                detachmentRepo
            )

            if (!parentalRepo.getIsManaged()) {
                return Result.success()
            }

            val apps = detachmentRepo.allApps.first()
            val screenTime = apps.sumOf { it.usedTodayMinutes }
            parentalRepo.pushDeviceTelemetry(screenTime, apps)
            val syncResult = parentalRepo.pullAndApplyConfig()

            if (syncResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "detachment_parental_sync"

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<ParentalSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }

        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val singleRequest = OneTimeWorkRequestBuilder<ParentalSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(singleRequest)
        }

        fun cancelPeriodicSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
