package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionScoresDto(
    val participants: List<ParticipantScoreDto>,
    val totalTasksInQuest: Int
)

@Serializable
data class ParticipantScoreDto(
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val photoUrl: String?,
    val totalScore: Int,
    val completedTasksCount: Int
)