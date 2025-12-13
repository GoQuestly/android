package com.goquestly.domain.model

data class ParticipantScore(
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val photoUrl: String?,
    val totalScore: Int,
    val completedTasksCount: Int,
    val totalTasksInQuest: Int
)