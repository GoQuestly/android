package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class JoinSessionDto(
    val sessionId: Int
)