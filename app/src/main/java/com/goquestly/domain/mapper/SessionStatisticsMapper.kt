package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.SessionStatisticsDto
import com.goquestly.domain.model.SessionStatistics

fun SessionStatisticsDto.toDomain(): SessionStatistics =
    SessionStatistics(
        totalSessions = totalSessions,
        finishedSessions = finishedSessions,
        finishRate = finishRate,
        averageRank = averageRank,
        bestRank = bestRank,
        totalScore = totalScore,
        totalCheckpointsPassed = totalCheckpointsPassed,
        totalTasksCompleted = totalTasksCompleted,
        rejectedSessions = rejectedSessions,
        disqualifiedSessions = disqualifiedSessions
    )