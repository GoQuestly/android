package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantJoinedDto(
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val sessionId: Int,
    val joinedAt: String,
    val photoUrl: String? = null
)