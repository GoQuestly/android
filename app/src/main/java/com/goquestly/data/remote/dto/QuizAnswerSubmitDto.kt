package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerSubmitDto(
    val questionId: Int,
    val answerIds: List<Int>
)