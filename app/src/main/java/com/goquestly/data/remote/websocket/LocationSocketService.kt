package com.goquestly.data.remote.websocket

import com.goquestly.data.local.TokenManager
import com.goquestly.util.API_BASE_URL
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.json.JSONObject

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
    }

    suspend fun connect() {
        connect(API_BASE_URL)
    }

    fun joinSession(sessionId: String) {
        val payload = JSONObject().apply {
            put("sessionId", sessionId)
        }
        emit("join-session", payload)
    }

    fun leaveSession(sessionId: String) {
        val payload = JSONObject().apply {
            put("sessionId", sessionId)
        }
        emit("leave-session", payload)
    }

    fun updateLocation(sessionId: String, latitude: Double, longitude: Double) {
        val payload = JSONObject().apply {
            put("sessionId", sessionId)
            put("latitude", latitude)
            put("longitude", longitude)
        }
        emit("update-location", payload)
    }

    fun observeJoinSessionError(): Flow<String> = observeCustomError(ERROR_JOIN_SESSION)
    fun observeLeaveSessionError(): Flow<String> = observeCustomError(ERROR_LEAVE_SESSION)
    fun observeUpdateLocationError(): Flow<String> = observeCustomError(ERROR_UPDATE_LOCATION)

    override fun getErrorEventNames(): List<String> = listOf(
        ERROR_JOIN_SESSION,
        ERROR_LEAVE_SESSION,
        ERROR_UPDATE_LOCATION
    )

    override fun setupCustomEventListeners() {
    }

    override fun clearCallbacks() {
    }
}
