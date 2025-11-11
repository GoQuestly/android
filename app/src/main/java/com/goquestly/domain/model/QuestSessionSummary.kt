package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class QuestSessionSummary(
    val id: Int,
    val questId: Int,
    val questTitle: String,
    val startDate: Instant,
    val endDate: Instant?,
    val isActive: Boolean,
    val participantCount: Int,
    val questPointCount: Int,
    val passedQuestPointCount: Int
)
