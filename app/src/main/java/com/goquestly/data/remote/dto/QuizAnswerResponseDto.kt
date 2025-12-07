package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerResponseDto(
    val success: Boolean,
    val answeredCount: Int? = null,
    val totalQuestions: Int? = null,
    val allAnswered: Boolean? = null,
    val scoreEarned: Int? = null,
    val maxScore: Int? = null,
    val requiredPercentage: Int? = null,
    val passed: Boolean? = null,
    val completedAt: String? = null
)