package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.QuestSessionDto
import com.goquestly.data.remote.dto.QuestSessionSummaryDto
import com.goquestly.domain.model.QuestSession
import com.goquestly.domain.model.QuestSessionSummary
import com.goquestly.domain.model.SessionEndReason
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun QuestSessionDto.toDomainModel(): QuestSession {
    return QuestSession(
        id = questSessionId,
        questId = questId,
        questTitle = questTitle,
        startDate = startDate,
        endDate = endDate,
        endReason = SessionEndReason.entries.find { it.value == endReason },
        inviteToken = inviteToken,
        participants = participants.map { it.toDomainModel() },
        isActive = isActive,
        participantCount = participantCount,
        questPointCount = questPointCount ?: 0,
        passedQuestPointCount = passedQuestPointCount ?: 0,
        questPhotoUrl = questPhotoUrl,
        questDescription = questDescription,
        questMaxDurationMinutes = questMaxDurationMinutes,
        startPointName = startPointName
    )
}

@OptIn(ExperimentalTime::class)
fun QuestSessionSummaryDto.toDomainModel() = QuestSessionSummary(
    id = questSessionId,
    questId = questId,
    questTitle = questTitle,
    startDate = startDate,
    endDate = endDate,
    endReason = SessionEndReason.entries.find { it.value == endReason },
    isActive = isActive,
    participantCount = participantCount,
    questPointCount = questPointCount,
    passedQuestPointCount = passedQuestPointCount
)
