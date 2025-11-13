package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class ParticipantDto(
    val participantId: Int,
    val userId: Int,
    val userName: String,
    @Serializable(with = InstantIsoSerializer::class)
    val joinedAt: Instant,
    val participationStatus: String,
    val rejectionReason: String?,
    val photoUrl: String?
)
