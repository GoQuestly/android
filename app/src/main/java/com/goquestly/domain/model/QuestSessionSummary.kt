package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class QuestSessionSummary(
    override val id: Int,
    override val questId: Int,
    override val questTitle: String,
    override val startDate: Instant,
    override val endDate: Instant?,
    override val endReason: SessionEndReason?,
    override val isActive: Boolean,
    override val participantCount: Int,
    override val questPointCount: Int,
    override val passedQuestPointCount: Int
) : BaseQuestSession()
