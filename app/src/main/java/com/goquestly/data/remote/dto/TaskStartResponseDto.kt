package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskStartResponseDto(
    val participantTaskId: Int,
    val questPointId: Int,
    val startDate: String,
    val expiresAt: String
)