package com.goquestly.data.remote.websocket

import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.dto.JoinSessionDto
import com.goquestly.data.remote.dto.LeaveSessionDto
import com.goquestly.data.remote.dto.UpdateLocationDto
import com.goquestly.util.API_BASE_URL
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Singleton
class LocationSocketService @Inject constructor(
    tokenManager: TokenManager,
    json: Json
) : BaseSocketService(
    tokenManager = tokenManager,
    json = json,
    endpoint = "location",
    logTag = TAG
) {
    companion object {
        private const val TAG = "LocationSocket"
        private const val ERROR_JOIN_SESSION = "join-session-error"
        private const val ERROR_LEAVE_SESSION = "leave-session-error"
        private const val ERROR_UPDATE_LOCATION = "update-location-error"
        private const val PARTICIPANT_REJECTED = "participant-rejected"
    }

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

    fun observeJoinSessionError(): Flow<String> = observeCustomError(ERROR_JOIN_SESSION)
    fun observeLeaveSessionError(): Flow<String> = observeCustomError(ERROR_LEAVE_SESSION)
    fun observeUpdateLocationError(): Flow<String> = observeCustomError(ERROR_UPDATE_LOCATION)
    fun observeParticipantRejected(): Flow<String> = observeCustomError(PARTICIPANT_REJECTED)


    override fun getErrorEventNames(): List<String> = listOf(
        ERROR_JOIN_SESSION,
        ERROR_LEAVE_SESSION,
        ERROR_UPDATE_LOCATION,
        PARTICIPANT_REJECTED
    )

    override fun setupCustomEventListeners() {
    }

    override fun clearCallbacks() {
    }
}
