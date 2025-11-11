package com.goquestly.presentation.main

import com.goquestly.domain.model.AuthState

data class MainState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val authState: AuthState? = null
)
