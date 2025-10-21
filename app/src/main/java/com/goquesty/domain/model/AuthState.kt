package com.goquesty.domain.model

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val isEmailVerified: Boolean) : AuthState
}