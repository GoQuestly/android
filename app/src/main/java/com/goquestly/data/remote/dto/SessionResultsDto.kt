package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class SessionResultsDto(
    val statistics: SessionStatisticsDto,
    val rankings: List<ParticipantRankingDto>
)

@Serializable
data class SessionStatisticsDto(
    val sessionDurationSeconds: Int,
    val totalParticipantsCount: Int,
    val finishedParticipantsCount: Int,
    val rejectedParticipantsCount: Int,
    val disqualifiedParticipantsCount: Int
)

@OptIn(ExperimentalTime::class)
@Serializable
data class ParticipantRankingDto(
    val rank: Int,
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val photoUrl: String?,
    val totalScore: Int,
    val passedCheckpointsCount: Int,
    @Serializable(with = InstantIsoSerializer::class)
    val finishDate: Instant?,
    val completionTimeSeconds: Int?
)
