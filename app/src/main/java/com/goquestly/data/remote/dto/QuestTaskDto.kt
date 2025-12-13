package com.goquestly.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestTaskDto(
    val questTaskId: Int,
    val taskType: String,
    val description: String,
    val maxDurationSeconds: Int,
    val isRequiredForNextPoint: Boolean,
    val scorePointsCount: Int? = null,
    val maxScorePointsCount: Int? = null,
    val successScorePointsPercent: Int? = null,
    val quizQuestions: List<QuizQuestionDto>? = null
)

