package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ParticipantLeftDto(
    val participantId: Int,
    val userId: Int,
    val userName: String,
    val sessionId: Int,
    val leftAt: String
)