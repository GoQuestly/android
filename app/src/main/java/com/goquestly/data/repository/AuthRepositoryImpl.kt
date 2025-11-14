package com.goquestly.data.repository

import com.goquestly.data.auth.GoogleAuthManager
import com.goquestly.data.local.TokenManager
import com.goquestly.data.remote.ApiService
import com.goquestly.data.remote.dto.GoogleSignInRequestDto
import com.goquestly.data.remote.dto.LoginRequestDto
import com.goquestly.data.remote.dto.RegisterRequestDto
import com.goquestly.data.remote.dto.ResetPasswordRequestDto
import com.goquestly.data.remote.dto.VerifyEmailRequestDto
import com.goquestly.domain.mapper.toDomainModel
import com.goquestly.domain.repository.AuthRepository
import com.goquestly.util.GOOGLE_WEB_CLIENT_ID
import com.goquestly.util.runCatchingAppException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val googleAuthManager: GoogleAuthManager,
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

    override suspend fun signInWithGoogle() = runCatchingAppException {
        val googleResult = googleAuthManager.signIn(GOOGLE_WEB_CLIENT_ID).getOrThrow()
        val request = GoogleSignInRequestDto(googleResult.idToken)
        val response = apiService.signInWithGoogle(request)
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
