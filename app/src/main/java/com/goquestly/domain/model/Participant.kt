package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Participant(
    val id: Int,
    val userId: Int,
    val userName: String,
    val joinedAt: Instant,
    val status: ParticipationStatus,
    val rejectionReason: String?,
    val photoUrl: String?
)
