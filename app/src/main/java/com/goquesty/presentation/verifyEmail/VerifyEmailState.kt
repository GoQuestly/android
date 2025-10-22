package com.goquesty.presentation.verifyEmail

data class VerifyEmailState(
    val secondsBeforeResend: Int? = null,
    val verificationCode: String = "",
    val codeError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val isVerified: Boolean = false,
    val isCodeResent: Boolean = false
)