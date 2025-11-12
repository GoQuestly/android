package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationDto(
    val sessionId: String,
    val latitude: Double,
    val longitude: Double
)
