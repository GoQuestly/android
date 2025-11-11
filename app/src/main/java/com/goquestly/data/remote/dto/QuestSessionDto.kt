package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class QuestSessionDto(
    val questSessionId: Int,
    val questId: Int,
    val questTitle: String,
    @Serializable(with = InstantIsoSerializer::class)
    val startDate: Instant,
    @Serializable(with = InstantIsoSerializer::class)
    val endDate: Instant?,
    val endReason: String?,
    val inviteToken: String,
    val participants: List<ParticipantDto>,
    val isActive: Boolean,
    val participantCount: Int,
    val questPointCount: Int? = null,
    val passedQuestPointCount: Int? = null,
    val questPhotoUrl: String? = null,
    val questDescription: String? = null,
    val questMaxDurationMinutes: Int? = null,
    val startPointName: String? = null
)
