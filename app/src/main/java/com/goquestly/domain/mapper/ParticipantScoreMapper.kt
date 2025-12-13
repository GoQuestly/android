package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.SessionScoresDto
import com.goquestly.domain.model.ParticipantScore

fun SessionScoresDto.toDomain(): List<ParticipantScore> {
    return participants.map {
        ParticipantScore(
            participantId = it.participantId,
            userId = it.userId,
            userName = it.userName,
            photoUrl = it.photoUrl,
            totalScore = it.totalScore,
            completedTasksCount = it.completedTasksCount,
            totalTasksInQuest = totalTasksInQuest
        )
    }
}