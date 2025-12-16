package com.goquestly.data.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.goquestly.data.local.DeviceTokenManager
import com.goquestly.domain.repository.NotificationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val notificationRepository: NotificationRepository,
    private val deviceTokenManager: DeviceTokenManager
) {

    suspend fun registerTokenIfNeeded() {
        try {
            val currentStatus = deviceTokenManager.getRegistrationStatus()

            if (currentStatus == DeviceTokenManager.RegistrationStatus.REGISTERED) {
                val localToken = deviceTokenManager.getDeviceToken()
                val freshToken = firebaseMessaging.token.await()

                if (localToken == freshToken) {
                    Log.d(TAG, "FCM token already registered")
                    return
                }
            }

            val token = firebaseMessaging.token.await()
            Log.d(TAG, "FCM token retrieved: $token")

            notificationRepository.registerDeviceToken(token).getOrThrow()
            Log.d(TAG, "FCM token registered successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to register FCM token", e)
        }
    }

    suspend fun unregisterToken() {
        try {
            notificationRepository.deleteDeviceToken().getOrThrow()
            Log.d(TAG, "FCM token unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister FCM token", e)
        }
    }

    companion object {
        private const val TAG = "FcmTokenManager"
    }
}
