package com.goquestly.data.remote.websocket

import android.util.Log
import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.dto.ParticipantJoinedDto
import com.goquestly.data.remote.dto.ParticipantLeftDto
import com.goquestly.data.remote.dto.SubscribeToSessionDto
import com.goquestly.data.remote.dto.UnsubscribeFromSessionDto
import com.goquestly.domain.mapper.toDomain
import com.goquestly.domain.model.ParticipantEvent
import com.goquestly.util.API_BASE_URL
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

@Singleton
class SessionEventsSocketService @Inject constructor(
    tokenManager: TokenManager,
    json: Json
) : BaseSocketService(
    tokenManager = tokenManager,
    json = json,
    endpoint = "session-events",
    logTag = TAG
) {
    private val participantJoinedCallbacks = mutableListOf<(ParticipantEvent.Joined) -> Unit>()
    private val participantLeftCallbacks = mutableListOf<(ParticipantEvent.Left) -> Unit>()
    private val sessionCancelledCallbacks = mutableListOf<() -> Unit>()
    private val sessionEndedCallbacks = mutableListOf<() -> Unit>()

    companion object {
        private const val TAG = "SessionEventsSocket"
        private const val ERROR_SUBSCRIBE = "subscribe-error"
        private const val ERROR_UNSUBSCRIBE = "unsubscribe-error"
        private const val SESSION_CANCELLED = "session-cancelled"
        private const val SESSION_ENDED = "session-ended"
    }

    suspend fun connect() {
        connect(API_BASE_URL)
    }

    fun subscribeToSession(sessionId: Int) {
        emitDto("subscribe-to-session", SubscribeToSessionDto(sessionId))
    }

    fun unsubscribeFromSession(sessionId: Int) {
        emitDto("unsubscribe-from-session", UnsubscribeFromSessionDto(sessionId))
    }

    fun observeParticipantJoined(): Flow<ParticipantEvent.Joined> = callbackFlow {
        val callback: (ParticipantEvent.Joined) -> Unit = { event ->
            trySend(event)
        }
        participantJoinedCallbacks.add(callback)
        awaitClose { participantJoinedCallbacks.remove(callback) }
    }

    fun observeParticipantLeft(): Flow<ParticipantEvent.Left> = callbackFlow {
        val callback: (ParticipantEvent.Left) -> Unit = { event ->
            trySend(event)
        }
        participantLeftCallbacks.add(callback)
        awaitClose { participantLeftCallbacks.remove(callback) }
    }

    fun observeSessionCancelled(): Flow<Unit> = callbackFlow {
        val callback: () -> Unit = {
            trySend(Unit)
        }
        sessionCancelledCallbacks.add(callback)
        awaitClose { sessionCancelledCallbacks.remove(callback) }
    }

    fun observeSessionEnded(): Flow<Unit> = callbackFlow {
        val callback: () -> Unit = {
            trySend(Unit)
        }
        sessionEndedCallbacks.add(callback)
        awaitClose { sessionEndedCallbacks.remove(callback) }
    }

    fun observeSubscribeError(): Flow<String> = observeCustomError(ERROR_SUBSCRIBE)
    fun observeUnsubscribeError(): Flow<String> = observeCustomError(ERROR_UNSUBSCRIBE)

    override fun getErrorEventNames(): List<String> = listOf(
        ERROR_SUBSCRIBE,
        ERROR_UNSUBSCRIBE
    )

    override fun setupCustomEventListeners() {
        socket?.apply {
            on("participant-joined") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val dto = json.decodeFromString<ParticipantJoinedDto>(data.toString())
                        val event = dto.toDomain()
                        participantJoinedCallbacks.forEach { it(event) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse participant-joined", e)
                    }
                }
            }

            on("participant-left") { args ->
                args.firstOrNull()?.let { data ->
                    try {
                        val dto = json.decodeFromString<ParticipantLeftDto>(data.toString())
                        val event = dto.toDomain()
                        participantLeftCallbacks.forEach { it(event) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse participant-left", e)
                    }
                }
            }

            on(SESSION_CANCELLED) {
                sessionCancelledCallbacks.forEach { it() }
            }

            on(SESSION_ENDED) {
                sessionEndedCallbacks.forEach { it() }
            }
        }
    }

    override fun clearCallbacks() {
        participantJoinedCallbacks.clear()
        participantLeftCallbacks.clear()
        sessionCancelledCallbacks.clear()
        sessionEndedCallbacks.clear()
    }
}
