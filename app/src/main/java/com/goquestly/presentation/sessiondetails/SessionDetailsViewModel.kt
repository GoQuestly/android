package com.goquestly.presentation.sessiondetails

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.data.remote.websocket.ParticipantSocketService
import com.goquestly.domain.model.ParticipantEvent
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@HiltViewModel
class SessionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val participantSocketService: ParticipantSocketService
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(SessionDetailsState())
    val state = _state.asStateFlow()

    companion object {
        private const val TAG = "SessionDetailsVM"
    }

    init {
        loadSessionDetails()
        connectToWebSocket()
    }

    private fun loadSessionDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            sessionRepository.getSessionDetails(sessionId)
                .onSuccess { session ->
                    _state.update {
                        it.copy(
                            session = session,
                            participants = session.participants,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                    Log.e(TAG, "Failed to load session details", error)
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
                    status = com.goquestly.domain.model.ParticipationStatus.APPROVED,
                    rejectionReason = null
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

    override fun onCleared() {
        participantSocketService.unsubscribeFromSession(sessionId)
        super.onCleared()
    }
}
