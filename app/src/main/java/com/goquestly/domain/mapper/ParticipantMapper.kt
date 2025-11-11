package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.ParticipantDto
import com.goquestly.domain.model.Participant
import com.goquestly.domain.model.ParticipationStatus
import java.util.Locale
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun ParticipantDto.toDomainModel() = Participant(
    id = participantId,
    userId = userId,
    userName = userName,
    joinedAt = joinedAt,
    status = participationStatus.uppercase(Locale.ROOT).let { status ->
        when (status) {
            "PENDING" -> ParticipationStatus.PENDING
            "APPROVED" -> ParticipationStatus.APPROVED
            "REJECTED" -> ParticipationStatus.REJECTED
            else -> ParticipationStatus.PENDING
        }
    },
    rejectionReason = rejectionReason
)
