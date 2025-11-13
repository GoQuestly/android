package com.goquestly.presentation.activeSession

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.service.LocationTrackingService
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val activeSessionManager: ActiveSessionManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(ActiveSessionState())
    val state = _state.asStateFlow()

    val rejectionEvents = activeSessionManager.rejectionEvents

    private var timerJob: Job? = null
    private var sessionMonitorJob: Job? = null

    init {
        loadSession()
        observeRejectionEvents()
        observeLocationUpdates()
    }

    private fun observeRejectionEvents() {
        viewModelScope.launch {
            rejectionEvents.collect {
                handleRejection()
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            activeSessionManager.locationUpdates.collect { update ->
                updateUserLocation(update.location)
                _state.update { it.copy(userBearing = update.bearing) }
            }
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val sessionResult = sessionRepository.getSessionDetails(sessionId)
            val pointsResult = sessionRepository.getQuestPoints(sessionId)

            sessionResult.onSuccess { session ->
                if (!session.isActive) {
                    handleSessionCompletion()
                    return@launch
                }

                startTimer(session)
                startSessionMonitoring(session)

                pointsResult.onSuccess { points ->
                    _state.update {
                        it.copy(
                            session = session,
                            questPoints = points,
                            isLoading = false
                        )
                    }
                }.onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_check_connection_and_retry)
                        )
                    }
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = context.getString(R.string.error_check_connection_and_retry)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startTimer(session: QuestSession) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                elapsedTimeSeconds = Clock.System.now().minus(session.startDate).inWholeSeconds
            )
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1.seconds)
                _state.update {
                    it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1)
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startSessionMonitoring(session: QuestSession) {
        sessionMonitorJob?.cancel()
        sessionMonitorJob = viewModelScope.launch {
            val now = Clock.System.now()
            val sessionEndTime = session.startDate + session.questMaxDurationMinutes.minutes
            val timeUntilCompletion = sessionEndTime - now

            if (timeUntilCompletion.inWholeMilliseconds > 0) {
                delay(timeUntilCompletion.inWholeMilliseconds)
                handleSessionCompletion()
            } else {
                handleSessionCompletion()
            }
        }
    }

    private fun handleSessionCompletion() {
        viewModelScope.launch {
            if (LocationTrackingService.isRunning) {
                LocationTrackingService.stopService(context)
            }
            activeSessionManager.clearActiveSession()
            _state.update { it.copy(isSessionCompleted = true) }
        }
    }

    fun retry() {
        loadSession()
    }

    fun updateUserLocation(location: LatLng) {
        _state.update { it.copy(userLocation = location) }
    }

    fun enableCameraTracking() {
        _state.update { it.copy(isCameraTrackingEnabled = true) }
    }

    fun disableCameraTracking() {
        _state.update { it.copy(isCameraTrackingEnabled = false) }
    }

    fun showLeaveConfirmation() {
        _state.update { it.copy(isLeaveConfirmationSheetOpen = true) }
    }

    fun dismissLeaveConfirmation() {
        _state.update { it.copy(isLeaveConfirmationSheetOpen = false) }
    }

    fun leaveSession(onLeft: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLeaveConfirmationSheetOpen = false) }

            delay(300)

            if (LocationTrackingService.isRunning) {
                LocationTrackingService.stopService(context)
            }

            activeSessionManager.clearActiveSession()

            onLeft()
        }
    }

    fun handleRejection() {
        viewModelScope.launch {
            _state.update { it.copy(isUserRejected = true) }
        }
    }

    fun dismissRejectionDialog() {
        _state.update { it.copy(isUserRejected = false, isSessionCompleted = true) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        sessionMonitorJob?.cancel()
    }

    fun startLocationUpdates() {
        if (LocationTrackingService.isRunning) {
            return
        }
        LocationTrackingService.startService(context, sessionId)
    }
}
