package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SocketErrorDto(
    val success: Boolean,
    val error: String
)
