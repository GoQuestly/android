package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestionDto(
    val quizQuestionId: Int,
    val question: String,
    val orderNumber: Int,
    val scorePointsCount: Int,
    val isMultipleAnswer: Boolean,
    val answers: List<QuizAnswerDto>
)