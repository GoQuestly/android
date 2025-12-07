package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskSubmitResponseDto(
    val success: Boolean,
    val scoreEarned: Int? = null,
    val maxScore: Int? = null,
    val requiredPercentage: Int? = null,
    val passed: Boolean,
    val completedAt: String
)