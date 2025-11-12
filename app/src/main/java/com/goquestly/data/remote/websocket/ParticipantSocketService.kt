package com.goquestly.data.remote.websocket

import android.util.Log
import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.dto.ParticipantJoinedDto
import com.goquestly.data.remote.dto.ParticipantLeftDto
import com.goquestly.domain.mapper.toDomain
import com.goquestly.domain.model.ParticipantEvent
import com.goquestly.util.API_BASE_URL
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import org.json.JSONObject

@Singleton
class ParticipantSocketService @Inject constructor(
    tokenManager: TokenManager,
    json: Json
) : BaseSocketService(
    tokenManager = tokenManager,
    json = json,
    endpoint = "participants",
    logTag = TAG
) {
    private val participantJoinedCallbacks = mutableListOf<(ParticipantEvent.Joined) -> Unit>()
    private val participantLeftCallbacks = mutableListOf<(ParticipantEvent.Left) -> Unit>()

    companion object {
        private const val TAG = "ParticipantSocket"
        private const val ERROR_SUBSCRIBE = "subscribe-error"
        private const val ERROR_UNSUBSCRIBE = "unsubscribe-error"
    }

    suspend fun connect() {
        connect(API_BASE_URL)
    }

    fun subscribeToSession(sessionId: Int) {
        val payload = JSONObject().apply {
            put("sessionId", sessionId)
        }
        emit("subscribe-to-session", payload)
    }

    fun unsubscribeFromSession(sessionId: Int) {
        val payload = JSONObject().apply {
            put("sessionId", sessionId)
        }
        emit("unsubscribe-from-session", payload)
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
        }
    }

    override fun clearCallbacks() {
        participantJoinedCallbacks.clear()
        participantLeftCallbacks.clear()
    }
}
