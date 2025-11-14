package com.goquestly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.google.android.gms.maps.model.LatLng
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

    private val _rejectionEvents = MutableSharedFlow<Unit>(replay = 0)
    val rejectionEvents = _rejectionEvents.asSharedFlow()

    data class LocationUpdate(val location: LatLng, val bearing: Float)

    private val _locationUpdates = MutableSharedFlow<LocationUpdate>(replay = 1)
    val locationUpdates = _locationUpdates.asSharedFlow()

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

    suspend fun emitRejection() {
        _rejectionEvents.emit(Unit)
    }

    suspend fun updateLocation(location: LatLng, bearing: Float) {
        _locationUpdates.emit(LocationUpdate(location, bearing))
    }
}
