package com.goquesty.domain.repository

import com.goquesty.domain.model.User

interface AuthRepository {
    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<User>

    suspend fun login(
        email: String,
        password: String
    ): Result<User>

    suspend fun requestPasswordReset(email: String): Result<Unit>
}
