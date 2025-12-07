package com.goquestly.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class PhotoModeratedEvent(
    val participantTaskPhotoId: Int,
    val participantTaskId: Int,
    val taskDescription: String,
    val pointName: String,
    val photoUrl: String,
    val approved: Boolean,
    val rejectionReason: String?,
    val scoreAdjustment: Int,
    val totalScore: Int,
    val moderatedAt: Instant
)
