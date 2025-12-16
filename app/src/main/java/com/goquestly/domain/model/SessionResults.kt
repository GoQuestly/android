package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class SessionResults(
    val statistics: SessionResultsStatistics,
    val rankings: List<ParticipantRanking>
)

data class SessionResultsStatistics(
    val sessionDurationSeconds: Int,
    val totalParticipantsCount: Int,
    val finishedParticipantsCount: Int,
    val rejectedParticipantsCount: Int,
    val disqualifiedParticipantsCount: Int
)

@OptIn(ExperimentalTime::class)
data class ParticipantRanking(
    val rank: Int,
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val photoUrl: String?,
    val totalScore: Int,
    val passedCheckpointsCount: Int,
    val finishDate: Instant?,
    val completionTimeSeconds: Int?
)
