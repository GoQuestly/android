package com.goquestly.presentation.sessionDetails

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.remote.websocket.ParticipantSocketService
import com.goquestly.domain.model.ParticipantEvent
import com.goquestly.domain.model.ParticipationStatus
import com.goquestly.domain.model.SessionStatus
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@HiltViewModel
class SessionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val participantSocketService: ParticipantSocketService,
    private val activeSessionManager: ActiveSessionManager,
    private val userRepository: com.goquestly.domain.repository.UserRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])
    private var sessionMonitoringJob: Job? = null
    private var currentUserId: Int? = null

    private val _state = MutableStateFlow(SessionDetailsState())
    val state = _state.asStateFlow()


    companion object {
        private const val TAG = "SessionDetailsVM"
    }

    init {
        loadCurrentUser()
        connectToWebSocket()
        startSessionTimeMonitoring()
        startSessionCompletionMonitoring()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getProfile().onSuccess { user ->
                currentUserId = user.id
            }
        }
    }

    fun loadSessionDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sessionRepository.getSessionDetails(sessionId)
                .onSuccess { session ->
                    val currentUserParticipant = currentUserId?.let { userId ->
                        session.participants.find { it.userId == userId }
                    }

                    val isRejected =
                        currentUserParticipant?.status == ParticipationStatus.REJECTED &&
                                session.endDate == null

                    _state.update {
                        it.copy(
                            session = session,
                            participants = session.participants,
                            isLoading = false,
                            isCurrentUserRejected = isRejected,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_check_connection_and_retry)
                        )
                    }
                }
        }
    }

    private fun connectToWebSocket() {
        viewModelScope.launch {
            try {
                participantSocketService.connect()
                participantSocketService.subscribeToSession(sessionId)
                Log.d(TAG, "Subscribed to session $sessionId")

                launch {
                    participantSocketService.observeParticipantJoined().collect { event ->
                        handleParticipantJoined(event)
                    }
                }

                launch {
                    participantSocketService.observeParticipantLeft().collect { event ->
                        handleParticipantLeft(event)
                    }
                }

                launch {
                    participantSocketService.observeSubscribeError().collect { error ->
                        Log.e(TAG, "Subscribe error: $error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connection failed", e)
            }
        }
    }

    private fun handleParticipantJoined(event: ParticipantEvent.Joined) {
        if (event.sessionId != sessionId) return

        Log.d(TAG, "Participant joined: ${event.userName}")

        _state.update { currentState ->
            val existingParticipant = currentState.participants.find { it.userId == event.userId }
            if (existingParticipant != null) {
                currentState
            } else {
                val newParticipant = com.goquestly.domain.model.Participant(
                    id = event.participantId,
                    userId = event.userId,
                    userName = event.userName,
                    joinedAt = event.joinedAt,
                    status = ParticipationStatus.APPROVED,
                    rejectionReason = null,
                    photoUrl = event.photoUrl
                )
                currentState.copy(
                    participants = currentState.participants + newParticipant,
                    session = currentState.session?.copy(
                        participantCount = currentState.participants.size + 1
                    )
                )
            }
        }
    }

    private fun handleParticipantLeft(event: ParticipantEvent.Left) {
        if (event.sessionId != sessionId) return

        Log.d(TAG, "Participant left: ${event.userName}")

        _state.update { currentState ->
            currentState.copy(
                participants = currentState.participants.filterNot { it.userId == event.userId },
                session = currentState.session?.copy(
                    participantCount = maxOf(0, currentState.participants.size - 1)
                )
            )
        }
    }

    fun toggleParticipantsSheet() {
        _state.update { it.copy(isParticipantsSheetOpen = !it.isParticipantsSheetOpen) }
    }

    fun showLeaveConfirmation() {
        _state.update { it.copy(isLeaveConfirmationSheetOpen = true) }
    }

    fun dismissLeaveConfirmation() {
        _state.update { it.copy(isLeaveConfirmationSheetOpen = false) }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startSessionTimeMonitoring() {
        sessionMonitoringJob?.cancel()

        @Suppress("UnusedFlow")
        sessionMonitoringJob = viewModelScope.launch {
            _state.map { it.session }
                .distinctUntilChanged()
                .flatMapLatest { session ->
                    if (session == null || session.status != SessionStatus.SCHEDULED) {
                        return@flatMapLatest emptyFlow()
                    }

                    val now = Clock.System.now()
                    val timeUntilStart = session.startDate - now

                    if (timeUntilStart.inWholeMilliseconds > 0) {
                        flow {
                            delay(timeUntilStart.inWholeMilliseconds)
                            emit(session)
                        }
                    } else {
                        flowOf(session)
                    }
                }
                .collect { session ->
                    loadSessionDetails()
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startSessionCompletionMonitoring() {
        viewModelScope.launch {
            @Suppress("UnusedFlow")
            _state.map { it.session }
                .distinctUntilChanged()
                .flatMapLatest { session ->
                    if (session == null || session.status != SessionStatus.IN_PROGRESS) {
                        return@flatMapLatest emptyFlow()
                    }

                    val now = Clock.System.now()
                    val sessionEndTime = session.startDate + session.questMaxDurationMinutes.minutes
                    val timeUntilCompletion = sessionEndTime - now

                    if (timeUntilCompletion.inWholeMilliseconds > 0) {
                        flow {
                            delay(timeUntilCompletion.inWholeMilliseconds)
                            emit(session)
                        }
                    } else {
                        flowOf(session)
                    }
                }
                .collect { session ->
                    loadSessionDetails()
                }
        }
    }

    fun leaveSession(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isLeaveConfirmationSheetOpen = false
                )
            }

            sessionRepository.leaveSession(sessionId)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_check_connection_and_retry)
                        )
                    }
                }
        }
    }

    fun joinSession(onNavigateToActiveSession: (Int) -> Unit) {
        viewModelScope.launch {
            activeSessionManager.setActiveSession(sessionId)
            onNavigateToActiveSession(sessionId)
        }
    }

    override fun onCleared() {
        participantSocketService.unsubscribeFromSession(sessionId)
        super.onCleared()
    }
}
