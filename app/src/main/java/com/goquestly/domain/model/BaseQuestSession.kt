package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
sealed class BaseQuestSession {
    abstract val id: Int
    abstract val questId: Int
    abstract val questTitle: String
    abstract val startDate: Instant
    abstract val endDate: Instant?
    abstract val isActive: Boolean
    abstract val participantCount: Int
    abstract val questPointCount: Int
    abstract val passedQuestPointCount: Int

    val status: SessionStatus
        get() = when {
            endDate != null -> SessionStatus.COMPLETED
            isActive -> SessionStatus.IN_PROGRESS
            else -> SessionStatus.SCHEDULED
        }
}