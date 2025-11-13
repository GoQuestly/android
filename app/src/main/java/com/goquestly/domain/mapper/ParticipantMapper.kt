package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.ParticipantDto
import com.goquestly.domain.model.Participant
import com.goquestly.domain.model.ParticipationStatus
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun ParticipantDto.toDomainModel() = Participant(
    id = participantId,
    userId = userId,
    userName = userName,
    joinedAt = joinedAt,
    status = ParticipationStatus.entries.find {
        it.name.equals(participationStatus, ignoreCase = true)
    } ?: ParticipationStatus.PENDING,
    rejectionReason = rejectionReason,
    photoUrl = photoUrl
)
