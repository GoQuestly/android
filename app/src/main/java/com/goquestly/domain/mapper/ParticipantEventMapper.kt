package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.ParticipantJoinedDto
import com.goquestly.data.remote.dto.ParticipantLeftDto
import com.goquestly.domain.model.ParticipantEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun ParticipantJoinedDto.toDomain(): ParticipantEvent.Joined {
    return ParticipantEvent.Joined(
        participantId = participantId,
        userId = userId,
        userName = userName,
        sessionId = sessionId,
        joinedAt = Instant.parse(joinedAt)
    )
}

@OptIn(ExperimentalTime::class)
fun ParticipantLeftDto.toDomain(): ParticipantEvent.Left {
    return ParticipantEvent.Left(
        participantId = participantId,
        userId = userId,
        userName = userName,
        sessionId = sessionId,
        leftAt = Instant.parse(leftAt)
    )
}
