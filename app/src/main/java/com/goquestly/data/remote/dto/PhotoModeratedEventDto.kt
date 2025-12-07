package com.goquestly.data.remote.dto

import com.goquestly.data.remote.serializer.InstantIsoSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class PhotoModeratedEventDto(
    val participantTaskPhotoId: Int,
    val participantTaskId: Int,
    val userId: Int,
    val userName: String,
    val questTaskId: Int,
    val taskDescription: String,
    val pointName: String,
    val photoUrl: String,
    val approved: Boolean,
    val rejectionReason: String? = null,
    val scoreAdjustment: Int,
    val totalScore: Int,
    val sessionId: Int,
    @Serializable(with = InstantIsoSerializer::class)
    val moderatedAt: Instant
)
