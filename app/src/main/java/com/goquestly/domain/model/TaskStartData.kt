package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class TaskStartData(
    val participantTaskId: Int,
    val questPointId: Int,
    val startDate: Instant,
    val expiresAt: Instant
)