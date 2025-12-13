package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.ParticipantRankingDto
import com.goquestly.data.remote.dto.SessionResultsDto
import com.goquestly.data.remote.dto.SessionStatisticsDto
import com.goquestly.domain.model.ParticipantRanking
import com.goquestly.domain.model.SessionResults
import com.goquestly.domain.model.SessionStatistics
import kotlin.time.ExperimentalTime

fun SessionResultsDto.toDomain(): SessionResults {
    return SessionResults(
        statistics = statistics.toDomain(),
        rankings = rankings.map { it.toDomain() }
    )
}

fun SessionStatisticsDto.toDomain(): SessionStatistics {
    return SessionStatistics(
        sessionDurationSeconds = sessionDurationSeconds,
        totalParticipantsCount = totalParticipantsCount,
        finishedParticipantsCount = finishedParticipantsCount,
        rejectedParticipantsCount = rejectedParticipantsCount,
        disqualifiedParticipantsCount = disqualifiedParticipantsCount
    )
}

@OptIn(ExperimentalTime::class)
fun ParticipantRankingDto.toDomain(): ParticipantRanking {
    return ParticipantRanking(
        rank = rank,
        participantId = participantId,
        userId = userId,
        userName = userName,
        photoUrl = photoUrl,
        totalScore = totalScore,
        passedCheckpointsCount = passedCheckpointsCount,
        finishDate = finishDate,
        completionTimeSeconds = completionTimeSeconds
    )
}
