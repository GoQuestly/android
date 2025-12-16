package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionStatisticsDto(
    val totalSessions: Int,
    val finishedSessions: Int,
    val finishRate: Double,
    val averageRank: Int,
    val bestRank: Int,
    val totalScore: Int,
    val totalCheckpointsPassed: Int,
    val totalTasksCompleted: Int,
    val rejectedSessions: Int,
    val disqualifiedSessions: Int
)