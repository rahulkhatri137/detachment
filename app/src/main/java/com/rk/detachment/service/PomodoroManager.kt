package com.rk.detachment.service

import android.content.Context
import com.rk.detachment.data.local.AppDatabase
import com.rk.detachment.data.local.entities.AppSettingsEntity
import com.rk.detachment.data.repository.DetachmentRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PomodoroState(
    val isBlackoutActive: Boolean = false,
    val isPomodoroRunning: Boolean = false,
    val blackoutTotalSeconds: Int = 25 * 60,
    val blackoutSecondsRemaining: Int = 25 * 60,
    val pomodoroSessionTag: String = "Deep Work",
    val isOverlayHidden: Boolean = false
)

object PomodoroManager {
    private val _state = MutableStateFlow(PomodoroState())
    val state = _state.asStateFlow()

    private var pomodoroJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var repository: DetachmentRepository? = null
    var database: AppDatabase? = null
    var onShowMessage: ((String) -> Unit)? = null
    var applicationContext: Context? = null

    fun initialize(context: Context, repo: DetachmentRepository, db: AppDatabase) {
        applicationContext = context.applicationContext
        repository = repo
        database = db
    }

    fun startBlackout(durationMinutes: Int = 25, tag: String = "Deep Work") {
        pomodoroJob?.cancel()
        val totalSecs = durationMinutes * 60
        _state.value = _state.value.copy(
            isBlackoutActive = true,
            isPomodoroRunning = true,
            blackoutTotalSeconds = totalSecs,
            blackoutSecondsRemaining = totalSecs,
            pomodoroSessionTag = tag,
            isOverlayHidden = false
        )

        scope.launch {
            database?.appSettingsDao()?.setSetting(AppSettingsEntity("is_blackout_active", "true"))
        }
        
        applicationContext?.let { PomodoroOverlayService.start(it) }

        pomodoroJob = scope.launch {
            while (isActive && _state.value.blackoutSecondsRemaining > 0 && _state.value.isPomodoroRunning) {
                delay(1000L)
                if (!isActive || !_state.value.isPomodoroRunning) break
                val remaining = (_state.value.blackoutSecondsRemaining - 1).coerceAtLeast(0)
                _state.value = _state.value.copy(blackoutSecondsRemaining = remaining)
            }

            if (isActive && _state.value.blackoutSecondsRemaining <= 0 && _state.value.isBlackoutActive) {
                repository?.savePomodoroSession(durationMinutes, tag, 0)
                database?.appSettingsDao()?.setSetting(AppSettingsEntity("is_blackout_active", "false"))
                onShowMessage?.invoke("Detachment Blackout completed! +$durationMinutes min focus logged.")
                _state.value = _state.value.copy(
                    isPomodoroRunning = false,
                    isBlackoutActive = false
                )
                applicationContext?.let { PomodoroOverlayService.stop(it) }
            }
        }
    }

    fun pause() {
        _state.value = _state.value.copy(isPomodoroRunning = false)
        pomodoroJob?.cancel()
    }

    fun resume() {
        if (_state.value.blackoutSecondsRemaining > 0) {
            _state.value = _state.value.copy(isPomodoroRunning = true)
            scope.launch {
                database?.appSettingsDao()?.setSetting(AppSettingsEntity("is_blackout_active", "true"))
            }
            val durationMinutes = _state.value.blackoutTotalSeconds / 60
            val tag = _state.value.pomodoroSessionTag

            pomodoroJob = scope.launch {
                while (isActive && _state.value.blackoutSecondsRemaining > 0 && _state.value.isPomodoroRunning) {
                    delay(1000L)
                    if (!isActive || !_state.value.isPomodoroRunning) break
                    val remaining = (_state.value.blackoutSecondsRemaining - 1).coerceAtLeast(0)
                    _state.value = _state.value.copy(blackoutSecondsRemaining = remaining)
                }

                if (isActive && _state.value.blackoutSecondsRemaining <= 0 && _state.value.isBlackoutActive) {
                    repository?.savePomodoroSession(durationMinutes, tag, 0)
                    database?.appSettingsDao()?.setSetting(AppSettingsEntity("is_blackout_active", "false"))
                    onShowMessage?.invoke("Detachment Blackout completed! +$durationMinutes min focus logged.")
                    _state.value = _state.value.copy(
                        isPomodoroRunning = false,
                        isBlackoutActive = false
                    )
                    applicationContext?.let { PomodoroOverlayService.stop(it) }
                }
            }
        }
    }

    fun stop() {
        pomodoroJob?.cancel()
        scope.launch {
            database?.appSettingsDao()?.setSetting(AppSettingsEntity("is_blackout_active", "false"))
        }
        _state.value = _state.value.copy(
            isBlackoutActive = false,
            isPomodoroRunning = false,
            isOverlayHidden = false
        )
        onShowMessage?.invoke("Detachment Blackout ended.")
        applicationContext?.let { PomodoroOverlayService.stop(it) }
    }

    fun setOverlayHidden(hidden: Boolean) {
        if (_state.value.isOverlayHidden != hidden) {
            _state.value = _state.value.copy(isOverlayHidden = hidden)
        }
    }
}
