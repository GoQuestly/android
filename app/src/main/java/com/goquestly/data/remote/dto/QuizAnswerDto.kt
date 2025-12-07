package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerDto(
    val quizAnswerId: Int,
    val answer: String
)