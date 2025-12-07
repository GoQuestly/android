package com.goquestly.domain.model

data class QuizQuestion(
    val quizQuestionId: Int,
    val question: String,
    val orderNumber: Int,
    val scorePointsCount: Int,
    val isMultipleAnswer: Boolean,
    val answers: List<QuizAnswer>
)