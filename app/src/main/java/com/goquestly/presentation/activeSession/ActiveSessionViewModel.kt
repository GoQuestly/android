package com.goquestly.presentation.activeSession

import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.local.ServerTimeManager
import com.goquestly.data.service.LocationTrackingService
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.domain.model.PointPassedEvent
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.repository.SessionRepository
import com.goquestly.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@HiltViewModel
class ActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val activeSessionManager: ActiveSessionManager,
    private val userRepository: UserRepository,
    private val serverTimeManager: ServerTimeManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(ActiveSessionState())
    val state = _state.asStateFlow()

    val participationBlockEvents = activeSessionManager.participationBlockEvents

    private var timerJob: Job? = null
    private var sessionMonitorJob: Job? = null

    private var leaderboardJob: Job? = null

    init {
        loadSession()
        observeParticipationBlockEvents()
        observeLocationUpdates()
        observePointPassedEvents()
        observeSessionCancelled()
        observeSessionEnded()
        observePhotoModerated()
    }

    private fun observeParticipationBlockEvents() {
        viewModelScope.launch {
            participationBlockEvents.collect { event ->
                handleParticipationBlock(event.reason)
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

    private fun observePointPassedEvents() {
        viewModelScope.launch {
            activeSessionManager.pointPassedEvents.collect { event ->
                handlePointPassed(event)
            }
        }
    }

    private fun observeSessionCancelled() {
        viewModelScope.launch {
            activeSessionManager.sessionCancelledEvents.collect {
                handleSessionCompletion()
            }
        }
    }

    private fun observeSessionEnded() {
        viewModelScope.launch {
            activeSessionManager.sessionEndedEvents.collect {
                handleSessionCompletion()
            }
        }
    }

    private fun observePhotoModerated() {
        viewModelScope.launch {
            activeSessionManager.photoModeratedEvents.collect { event ->
                _state.update { it.copy(photoModeratedEvent = event) }
                refreshQuestPoints()

                refreshLeaderboardOnce()
            }
        }
    }

    private fun refreshQuestPoints() {
        viewModelScope.launch {
            sessionRepository.getQuestPoints(sessionId).onSuccess { points ->
                _state.update { it.copy(questPoints = points) }
            }
        }
    }

    private fun handlePointPassed(event: PointPassedEvent) {
        val point = _state.value.questPoints.find { it.pointId == event.questPointId }
        val hasTask = point?.hasTask ?: false
        val taskStatus = point?.taskStatus

        _state.update {
            it.copy(
                pointPassedEvent = PointPassedEvent(
                    questPointId = event.questPointId,
                    pointName = event.pointName,
                    orderNumber = event.orderNumber,
                    hasTask = hasTask,
                    taskStatus = taskStatus
                ),
                questPoints = it.questPoints.map { p ->
                    if (p.pointId == event.questPointId) p.copy(isPassed = true) else p
                }
            )
        }

        if (!hasTask) {
            viewModelScope.launch {
                sessionRepository.getQuestPoints(sessionId).onSuccess { updatedPoints ->
                    _state.update { it.copy(questPoints = updatedPoints) }
                }
            }
        }

        refreshLeaderboardOnce()
    }

    fun dismissPointPassedDialog() {
        _state.update { it.copy(pointPassedEvent = null) }

        val notificationIds = activeSessionManager.getAndClearPointPassedNotifications()
        if (notificationIds.isNotEmpty()) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationIds.forEach { notificationId ->
                notificationManager.cancel(notificationId)
            }
        }
    }

    fun dismissPhotoModeratedDialog() {
        _state.update { it.copy(photoModeratedEvent = null) }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val sessionDeferred = async { sessionRepository.getSessionDetails(sessionId) }
            val pointsDeferred = async { sessionRepository.getQuestPoints(sessionId) }
            val activeTaskDeferred = async { sessionRepository.getActiveTask(sessionId) }
            val profileDeferred = async { userRepository.getProfile() }

            val sessionResult = sessionDeferred.await()
            val pointsResult = pointsDeferred.await()
            val activeTaskResult = activeTaskDeferred.await()
            val profileResult = profileDeferred.await()

            val currentUserId = profileResult.getOrNull()?.id
            _state.update { it.copy(currentUserId = currentUserId) }

            sessionResult.onSuccess { session ->
                if (!session.isActive) {
                    handleSessionCompletion()
                    return@launch
                }

                currentUserId?.let { uid ->
                    session.participants
                        .find { it.userId == uid }
                        ?.rejectionReason
                        ?.let { reasonString ->
                            ParticipationBlockReason.entries
                                .find { it.value == reasonString }
                                ?.let { reason ->
                                    handleParticipationBlock(reason)
                                    return@launch
                                }
                        }
                }

                startTimer(session)
                startSessionMonitoring(session)

                pointsResult.onSuccess { points ->
                    val activeTask = activeTaskResult.getOrNull()

                    _state.update {
                        it.copy(
                            session = session,
                            questPoints = points,
                            isLoading = false,
                            activeTask = activeTask
                        )
                    }

                    startLeaderboardPolling()
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

    private fun startLeaderboardPolling() {
        leaderboardJob?.cancel()
        leaderboardJob = viewModelScope.launch {
            fetchLeaderboard()

            while (isActive) {
                delay(2_000)
                fetchLeaderboard()
            }
        }
    }

    private suspend fun fetchLeaderboard() {
        _state.update { it.copy(isLeaderboardLoading = true) }

        sessionRepository.getSessionScores(sessionId)
            .onSuccess { list ->
                val sorted = list.sortedByDescending { it.totalScore }

                val totalTasks = sorted.firstOrNull()?.totalTasksInQuest ?: 0

                _state.update {
                    it.copy(
                        leaderboard = sorted,
                        totalTasksInQuest = totalTasks,
                        isLeaderboardLoading = false
                    )
                }
            }
            .onFailure {
                _state.update { it.copy(isLeaderboardLoading = false) }
            }
    }

    private fun refreshLeaderboardOnce() {
        viewModelScope.launch {
            fetchLeaderboard()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startTimer(session: QuestSession) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val now = serverTimeManager.getCurrentServerTime()
            _state.update {
                it.copy(
                    elapsedTimeSeconds = (now - session.startDate).inWholeSeconds
                )
            }

            while (isActive) {
                delay(1.seconds)
                _state.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun startSessionMonitoring(session: QuestSession) {
        sessionMonitorJob?.cancel()
        sessionMonitorJob = viewModelScope.launch {
            val now = serverTimeManager.getCurrentServerTime()
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
            leaderboardJob?.cancel()

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

            leaderboardJob?.cancel()

            if (LocationTrackingService.isRunning) {
                LocationTrackingService.stopService(context)
            }

            activeSessionManager.clearActiveSession()
            onLeft()
        }
    }

    fun handleParticipationBlock(reason: ParticipationBlockReason) {
        viewModelScope.launch {
            leaderboardJob?.cancel()
            activeSessionManager.clearActiveSession()
            _state.update { it.copy(isParticipationBlocked = true, blockReason = reason) }
        }
    }

    fun dismissBlockDialog() {
        _state.update { it.copy(isParticipationBlocked = false, isSessionCompleted = true) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        sessionMonitorJob?.cancel()
        leaderboardJob?.cancel()
    }

    fun startLocationUpdates() {
        if (LocationTrackingService.isRunning) return
        LocationTrackingService.startService(context, sessionId)
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}