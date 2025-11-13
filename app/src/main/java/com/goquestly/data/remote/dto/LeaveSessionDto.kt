package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeaveSessionDto(
    val sessionId: Int
)