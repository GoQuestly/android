package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class TaskSubmitData(
    val success: Boolean,
    val scoreEarned: Int?,
    val maxScore: Int?,
    val requiredPercentage: Int?,
    val passed: Boolean,
    val completedAt: Instant
)