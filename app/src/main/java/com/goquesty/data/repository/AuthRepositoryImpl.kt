package com.goquesty.data.repository

import com.goquesty.data.local.TokenManager
import com.goquesty.data.remote.ApiService
import com.goquesty.data.remote.dto.LoginRequestDto
import com.goquesty.data.remote.dto.RegisterRequestDto
import com.goquesty.data.remote.dto.ResetPasswordRequestDto
import com.goquesty.data.remote.dto.VerifyEmailRequestDto
import com.goquesty.domain.mapper.toDomainModel
import com.goquesty.domain.repository.AuthRepository
import com.goquesty.util.runCatchingAppException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    override suspend fun logout() {
        tokenManager.clearToken()
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ) = runCatchingAppException {
        val request = RegisterRequestDto(email, name, password)
        val response = apiService.register(request)
        tokenManager.saveToken(response.accessToken)
        response.user.toDomainModel()
    }

    override suspend fun login(
        email: String,
        password: String
    ) = runCatchingAppException {
        val request = LoginRequestDto(email, password)
        val response = apiService.login(request)
        tokenManager.saveToken(response.accessToken)
        response.user.toDomainModel()
    }

    override suspend fun requestPasswordReset(email: String) = runCatchingAppException {
        val request = ResetPasswordRequestDto(email)
        apiService.requestPasswordReset(request)
    }

    override suspend fun getVerificationStatus() = runCatchingAppException {
        apiService.getVerificationStatus().toDomainModel()
    }

    override suspend fun sendVerificationCode() = runCatchingAppException {
        apiService.sendVerificationCode()
    }

    override suspend fun verifyEmail(code: String) = runCatchingAppException {
        val request = VerifyEmailRequestDto(code)
        apiService.verifyEmail(request)
    }
}
