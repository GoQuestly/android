package com.goquestly.domain.model

sealed class VerificationStatus {
    data object Verified : VerificationStatus()
    sealed class Unverified : VerificationStatus() {
        object CanResendCode : Unverified()
        data class CannotResendCode(
            val canResendAtElapsedRealtime: Long
        ) : Unverified()
    }
}