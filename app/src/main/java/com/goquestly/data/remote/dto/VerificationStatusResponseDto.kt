package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@OptIn(ExperimentalTime::class)
data class VerificationStatusResponseDto(
    @SerialName("is_verified")
    val isVerified: Boolean,
    @SerialName("can_resend_code")
    val canResendCode: Boolean,
    @Serializable(with = InstantIsoSerializer::class)
    @SerialName("can_resend_at")
    val canResendAt: Instant?,
    @Serializable(with = InstantIsoSerializer::class)
    @SerialName("server_time")
    val serverTime: Instant,
    @Serializable(with = InstantIsoSerializer::class)
    @SerialName("code_expires_at")
    val codeExpiresAt: Instant?,
)
