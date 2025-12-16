package com.goquestly.domain.repository

interface NotificationRepository {
    suspend fun registerDeviceToken(token: String): Result<Unit>
    suspend fun deleteDeviceToken(): Result<Unit>
    suspend fun getLocalDeviceToken(): String?
}
