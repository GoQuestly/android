package com.goquesty.data.repository

import com.goquesty.data.local.TokenManager
import com.goquesty.data.remote.ApiService
import com.goquesty.data.remote.dto.LoginRequestDto
import com.goquesty.data.remote.dto.RegisterRequestDto
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

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ) = runCatchingAppException {
        val request = RegisterRequestDto(
            email = email,
            name = name,
            password = password
        )
        val response = apiService.register(request)
        tokenManager.saveToken(response.accessToken)
        response.user.toDomainModel()
    }

    override suspend fun login(
        email: String,
        password: String
    ) = runCatchingAppException {
        val request = LoginRequestDto(
            email = email,
            password = password
        )
        val response = apiService.login(request)
        tokenManager.saveToken(response.accessToken)
        response.user.toDomainModel()
    }
}
