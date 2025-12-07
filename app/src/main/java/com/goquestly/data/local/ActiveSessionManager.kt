package com.goquestly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.android.gms.maps.model.LatLng
import com.goquestly.domain.model.ParticipationBlockReason
import com.goquestly.domain.model.PhotoModeratedEvent
import com.goquestly.domain.model.PointPassedEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

@Singleton
class ActiveSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val activeSessionIdKey = intPreferencesKey("active_session_id")
    private val activeTaskIdKey = intPreferencesKey("active_task_id")
    private val currentQuestionIndexKey = intPreferencesKey("current_question_index")
    private val taskExpirationTimeKey = longPreferencesKey("task_expiration_time_millis")

    private val pointPassedNotificationIds = mutableSetOf<Int>()

    data class ParticipationBlockEvent(val reason: ParticipationBlockReason)

    private val _participationBlockEvents = MutableSharedFlow<ParticipationBlockEvent>(replay = 0)
    val participationBlockEvents = _participationBlockEvents.asSharedFlow()

    data class LocationUpdate(val location: LatLng, val bearing: Float)

    private val _locationUpdates = MutableSharedFlow<LocationUpdate>(replay = 1)
    val locationUpdates = _locationUpdates.asSharedFlow()

    private val _pointPassedEvents = MutableSharedFlow<PointPassedEvent>(replay = 0)
    val pointPassedEvents = _pointPassedEvents.asSharedFlow()

    private val _sessionCancelledEvents = MutableSharedFlow<Unit>(replay = 0)
    val sessionCancelledEvents = _sessionCancelledEvents.asSharedFlow()

    private val _photoModeratedEvents = MutableSharedFlow<PhotoModeratedEvent>(replay = 0)
    val photoModeratedEvents = _photoModeratedEvents.asSharedFlow()

    suspend fun setActiveSession(sessionId: Int) {
        dataStore.edit { preferences ->
            preferences[activeSessionIdKey] = sessionId
        }
    }

    suspend fun getActiveSessionId(): Int? {
        return dataStore.data.first()[activeSessionIdKey]
    }

    suspend fun clearActiveSession() {
        dataStore.edit { preferences ->
            preferences.remove(activeSessionIdKey)
        }
    }

    suspend fun emitParticipationBlock(reason: ParticipationBlockReason) {
        _participationBlockEvents.emit(ParticipationBlockEvent(reason))
    }

    suspend fun updateLocation(location: LatLng, bearing: Float) {
        _locationUpdates.emit(LocationUpdate(location, bearing))
    }

    suspend fun emitPointPassed(questPointId: Int, pointName: String, orderNumber: Int) {
        _pointPassedEvents.emit(PointPassedEvent(questPointId, pointName, orderNumber))
    }

    fun addPointPassedNotification(notificationId: Int) {
        pointPassedNotificationIds.add(notificationId)
    }

    fun getAndClearPointPassedNotifications(): Set<Int> {
        val ids = pointPassedNotificationIds.toSet()
        pointPassedNotificationIds.clear()
        return ids
    }

    suspend fun emitSessionCancelled() {
        _sessionCancelledEvents.emit(Unit)
    }

    suspend fun emitPhotoModerated(event: PhotoModeratedEvent) {
        _photoModeratedEvents.emit(event)
    }

    suspend fun saveQuizProgress(taskId: Int, currentQuestionIndex: Int) {
        dataStore.edit { preferences ->
            preferences[activeTaskIdKey] = taskId
            preferences[currentQuestionIndexKey] = currentQuestionIndex
        }
    }

    suspend fun getQuizProgress(taskId: Int): Int? {
        val preferences = dataStore.data.first()
        val savedTaskId = preferences[activeTaskIdKey]
        return if (savedTaskId == taskId) {
            preferences[currentQuestionIndexKey] ?: 0
        } else {
            null
        }
    }

    suspend fun clearQuizProgress() {
        dataStore.edit { preferences ->
            preferences.remove(activeTaskIdKey)
            preferences.remove(currentQuestionIndexKey)
        }
    }

    suspend fun saveTaskExpiration(expirationTimeMillis: Long) {
        dataStore.edit { preferences ->
            preferences[taskExpirationTimeKey] = expirationTimeMillis
        }
    }

    suspend fun isTaskExpired(): Boolean {
        val preferences = dataStore.data.first()
        val expirationTime = preferences[taskExpirationTimeKey] ?: return false
        return System.currentTimeMillis() >= expirationTime
    }

    suspend fun clearTaskExpiration() {
        dataStore.edit { preferences ->
            preferences.remove(taskExpirationTimeKey)
        }
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(activeSessionIdKey)
            preferences.remove(activeTaskIdKey)
            preferences.remove(currentQuestionIndexKey)
            preferences.remove(taskExpirationTimeKey)
        }
    }
}

