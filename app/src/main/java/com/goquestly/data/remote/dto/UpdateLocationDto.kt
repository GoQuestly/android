package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateLocationDto(
    val sessionId: Int,
    val latitude: Double,
    val longitude: Double
)
