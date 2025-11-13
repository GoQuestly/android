package com.goquestly.domain.repository

import com.goquestly.domain.model.User
import com.goquestly.domain.model.VerificationStatus

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

    suspend fun signInWithGoogle(): Result<User>

    suspend fun requestPasswordReset(email: String): Result<Unit>

    suspend fun getVerificationStatus(): Result<VerificationStatus>

    suspend fun sendVerificationCode(): Result<Unit>

    suspend fun verifyEmail(code: String): Result<Unit>

    suspend fun isLoggedIn(): Boolean

    suspend fun logout()
}
