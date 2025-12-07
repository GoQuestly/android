package com.goquestly.domain.useCase

import com.goquestly.domain.exception.UnauthorizedException
import com.goquestly.domain.model.AuthState
import com.goquestly.domain.repository.AuthRepository
import com.goquestly.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<AuthState> {
        val isLoggedIn = authRepository.isLoggedIn()
        if (!isLoggedIn) {
            return Result.success(AuthState.Unauthenticated)
        }

        val profileResult = userRepository.getProfile()
        if (profileResult.isSuccess) {
            val user = profileResult.getOrThrow()
            return Result.success(AuthState.Authenticated(user.isEmailVerified))
        } else {
            val exception = profileResult.exceptionOrNull()
            if (exception is UnauthorizedException) {
                authRepository.logout()
                return Result.success(AuthState.Unauthenticated)
            }
            return Result.failure(exception!!)
        }
    }
}