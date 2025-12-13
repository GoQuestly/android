package com.goquestly.data.repository

import com.goquestly.data.local.DeviceTokenManager
import com.goquestly.data.remote.ApiService
import com.goquestly.data.remote.dto.DeviceTokenRequestDto
import com.goquestly.domain.repository.NotificationRepository
import com.goquestly.util.runCatchingAppException
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val deviceTokenManager: DeviceTokenManager
) : NotificationRepository {

    override suspend fun registerDeviceToken(token: String) = runCatchingAppException {
        deviceTokenManager.setRegistrationStatus(DeviceTokenManager.RegistrationStatus.PENDING)
        try {
            apiService.registerDeviceToken(DeviceTokenRequestDto(token))
            deviceTokenManager.saveDeviceToken(token)
            deviceTokenManager.setRegistrationStatus(DeviceTokenManager.RegistrationStatus.REGISTERED)
        } catch (e: Exception) {
            deviceTokenManager.setRegistrationStatus(DeviceTokenManager.RegistrationStatus.FAILED)
            throw e
        }
    }

    override suspend fun deleteDeviceToken() = runCatchingAppException {
        apiService.deleteDeviceToken()
        deviceTokenManager.clearDeviceToken()
    }

    override suspend fun getLocalDeviceToken(): String? {
        return deviceTokenManager.getDeviceToken()
    }
}
