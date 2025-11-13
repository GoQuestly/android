package com.goquestly.domain.mapper

import android.os.SystemClock
import com.goquestly.data.remote.dto.VerificationStatusResponseDto
import com.goquestly.domain.model.VerificationStatus
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun VerificationStatusResponseDto.toDomainModel(): VerificationStatus {
    return when {
        isVerified -> VerificationStatus.Verified
        canResendCode -> VerificationStatus.Unverified.CanResendCode
        else -> {
            val resendAt = canResendAt?.toEpochMilliseconds() ?: 0L
            val serverAt = serverTime.toEpochMilliseconds()
            val deltaMs = (resendAt - serverAt).coerceAtLeast(0)
            VerificationStatus.Unverified.CannotResendCode(
                canResendAtElapsedRealtime = SystemClock.elapsedRealtime() + deltaMs
            )
        }
    }
}