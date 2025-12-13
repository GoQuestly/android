package com.goquestly.domain.model

sealed class QuestTask {
    abstract val questTaskId: Int
    abstract val taskType: TaskType
    abstract val description: String
    abstract val maxDurationSeconds: Int
    abstract val isRequiredForNextPoint: Boolean

    data class CodeWord(
        override val questTaskId: Int,
        override val taskType: TaskType,
        override val description: String,
        override val maxDurationSeconds: Int,
        override val isRequiredForNextPoint: Boolean,
        val scorePointsCount: Int
    ) : QuestTask()

    data class Quiz(
        override val questTaskId: Int,
        override val taskType: TaskType,
        override val description: String,
        override val maxDurationSeconds: Int,
        override val isRequiredForNextPoint: Boolean,
        val maxScorePointsCount: Int,
        val successScorePointsPercent: Int,
        val questions: List<QuizQuestion>
    ) : QuestTask()

    data class Photo(
        override val questTaskId: Int,
        override val taskType: TaskType,
        override val description: String,
        override val maxDurationSeconds: Int,
        override val isRequiredForNextPoint: Boolean,
        val scorePointsCount: Int
    ) : QuestTask()
}