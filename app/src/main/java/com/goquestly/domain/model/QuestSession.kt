package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class QuestSession(
    val id: Int,
    val questId: Int,
    val questTitle: String,
    val startDate: Instant,
    val endDate: Instant?,
    val endReason: String?,
    val inviteToken: String,
    val participants: List<Participant>,
    val isActive: Boolean,
    val participantCount: Int,
    val questPointCount: Int,
    val passedQuestPointCount: Int,
    val questPhotoUrl: String?,
    val questDescription: String?,
    val questMaxDurationMinutes: Int?,
    val startPointName: String?
)
