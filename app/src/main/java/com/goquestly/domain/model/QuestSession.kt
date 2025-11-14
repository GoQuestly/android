package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class QuestSession(
    override val id: Int,
    override val questId: Int,
    override val questTitle: String,
    override val startDate: Instant,
    override val endDate: Instant?,
    val endReason: String?,
    val inviteToken: String,
    val participants: List<Participant>,
    override val isActive: Boolean,
    override val participantCount: Int,
    override val questPointCount: Int,
    override val passedQuestPointCount: Int,
    val questPhotoUrl: String?,
    val questDescription: String?,
    val questMaxDurationMinutes: Int,
    val startPointName: String
) : BaseQuestSession()