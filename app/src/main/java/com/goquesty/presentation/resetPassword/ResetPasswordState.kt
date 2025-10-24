package com.goquesty.presentation.resetPassword

data class ResetPasswordState(
    val email: String = "",
    val emailError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isLinkSent: Boolean = false
)