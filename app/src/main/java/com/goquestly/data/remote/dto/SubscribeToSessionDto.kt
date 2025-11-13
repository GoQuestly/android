package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubscribeToSessionDto(
    val sessionId: Int
)