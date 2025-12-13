package com.goquestly.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DeviceTokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val deviceTokenKey = stringPreferencesKey("fcm_device_token")
    private val registrationStatusKey = stringPreferencesKey("fcm_registration_status")

    suspend fun saveDeviceToken(token: String) {
        dataStore.edit { preferences ->
            preferences[deviceTokenKey] = token
        }
    }

    suspend fun getDeviceToken(): String? {
        return dataStore.data.first()[deviceTokenKey]
    }

    suspend fun clearDeviceToken() {
        dataStore.edit { preferences ->
            preferences.remove(deviceTokenKey)
            preferences.remove(registrationStatusKey)
        }
    }

    suspend fun setRegistrationStatus(status: RegistrationStatus) {
        dataStore.edit { preferences ->
            preferences[registrationStatusKey] = status.name
        }
    }

    suspend fun getRegistrationStatus(): RegistrationStatus {
        val status = dataStore.data.first()[registrationStatusKey]
        return status?.let { RegistrationStatus.valueOf(it) } ?: RegistrationStatus.NOT_REGISTERED
    }

    enum class RegistrationStatus {
        NOT_REGISTERED,
        PENDING,
        REGISTERED,
        FAILED
    }
}
