package com.goquesty.presentation.main

import com.goquesty.domain.model.AuthState

data class MainState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val authState: AuthState? = null
)
