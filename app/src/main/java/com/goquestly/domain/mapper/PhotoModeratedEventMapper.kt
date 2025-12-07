package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.PhotoModeratedEventDto
import com.goquestly.domain.model.PhotoModeratedEvent
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun PhotoModeratedEventDto.toDomainModel() = PhotoModeratedEvent(
    participantTaskPhotoId = participantTaskPhotoId,
    participantTaskId = participantTaskId,
    taskDescription = taskDescription,
    pointName = pointName,
    photoUrl = photoUrl,
    approved = approved,
    rejectionReason = rejectionReason,
    scoreAdjustment = scoreAdjustment,
    totalScore = totalScore,
    moderatedAt = moderatedAt
)
