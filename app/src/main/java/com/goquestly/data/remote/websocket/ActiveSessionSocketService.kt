package com.goquestly.data.remote.websocket

import android.util.Log
import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.dto.JoinSessionDto
import com.goquestly.data.remote.dto.LeaveSessionDto
import com.goquestly.data.remote.dto.PointPassedEventDto
import com.goquestly.data.remote.dto.UpdateLocationDto
import com.goquestly.util.API_BASE_URL
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

@Singleton
class ActiveSessionSocketService @Inject constructor(
    tokenManager: TokenManager,
    private val jsonSerializer: Json
) : BaseSocketService(
    tokenManager = tokenManager,
    json = jsonSerializer,
    endpoint = "active-session",
    logTag = TAG
) {
    companion object {
        private const val TAG = "ActiveSessionSocket"
        private const val ERROR_JOIN_SESSION = "join-session-error"
        private const val ERROR_LEAVE_SESSION = "leave-session-error"
        private const val ERROR_UPDATE_LOCATION = "update-location-error"
        private const val PARTICIPANT_REJECTED = "participant-rejected"
        private const val PARTICIPANT_DISQUALIFIED = "participant-disqualified"
        private const val POINT_PASSED = "point-passed"
        private const val SESSION_CANCELLED = "session-cancelled"
    }

    private val pointPassedCallbacks = mutableListOf<(PointPassedEventDto) -> Unit>()
    private val sessionCancelledCallbacks = mutableListOf<() -> Unit>()

    suspend fun connect() {
        connect(API_BASE_URL)
    }

    fun joinSession(sessionId: Int) {
        emitDto("join-session", JoinSessionDto(sessionId))
    }

    fun leaveSession(sessionId: Int) {
        emitDto("leave-session", LeaveSessionDto(sessionId))
    }

    fun updateLocation(sessionId: Int, latitude: Double, longitude: Double) {
        emitDto("update-location", UpdateLocationDto(sessionId, latitude, longitude))
    }

    fun observeParticipantRejected(): Flow<String> = observeCustomError(PARTICIPANT_REJECTED)
    fun observeParticipantDisqualified(): Flow<String> =
        observeCustomError(PARTICIPANT_DISQUALIFIED)

    fun observePointPassed(): Flow<PointPassedEventDto> = callbackFlow {
        val callback: (PointPassedEventDto) -> Unit = { event ->
            trySend(event)
        }
        pointPassedCallbacks.add(callback)
        awaitClose {
            pointPassedCallbacks.remove(callback)
        }
    }

    fun observeSessionCancelled(): Flow<Unit> = callbackFlow {
        val callback: () -> Unit = {
            trySend(Unit)
        }
        sessionCancelledCallbacks.add(callback)
        awaitClose {
            sessionCancelledCallbacks.remove(callback)
        }
    }

    override fun getErrorEventNames(): List<String> = listOf(
        ERROR_JOIN_SESSION,
        ERROR_LEAVE_SESSION,
        ERROR_UPDATE_LOCATION,
        PARTICIPANT_REJECTED,
        PARTICIPANT_DISQUALIFIED
    )

    override fun setupCustomEventListeners() {
        socket?.on(POINT_PASSED) { args ->
            args.firstOrNull()?.let { data ->
                try {
                    val event =
                        jsonSerializer.decodeFromString<PointPassedEventDto>(data.toString())
                    pointPassedCallbacks.forEach { it(event) }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse point-passed event", e)
                }
            }
        }

        socket?.on(SESSION_CANCELLED) {
            sessionCancelledCallbacks.forEach { it() }
        }
    }

    override fun clearCallbacks() {
        pointPassedCallbacks.clear()
        sessionCancelledCallbacks.clear()
    }
}
