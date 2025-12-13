@file:OptIn(ExperimentalTime::class)

package com.goquestly.domain.mapper

import com.goquestly.data.remote.dto.QuestTaskDto
import com.goquestly.data.remote.dto.QuizAnswerDto
import com.goquestly.data.remote.dto.QuizQuestionDto
import com.goquestly.data.remote.dto.TaskStartResponseDto
import com.goquestly.data.remote.dto.TaskSubmitResponseDto
import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.model.QuizAnswer
import com.goquestly.domain.model.QuizQuestion
import com.goquestly.domain.model.TaskStartData
import com.goquestly.domain.model.TaskSubmitData
import com.goquestly.domain.model.TaskType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun QuestTaskDto.toDomainModel(): QuestTask {
    val type = TaskType.entries.find { it.typeName == taskType }
    checkNotNull(type) { "Unknown task type: $taskType" }

    return when (type) {
        TaskType.CODE_WORD -> QuestTask.CodeWord(
            questTaskId = questTaskId,
            taskType = type,
            description = description,
            maxDurationSeconds = maxDurationSeconds,
            isRequiredForNextPoint = isRequiredForNextPoint,
            scorePointsCount = scorePointsCount ?: 0
        )

        TaskType.QUIZ -> QuestTask.Quiz(
            questTaskId = questTaskId,
            taskType = type,
            description = description,
            maxDurationSeconds = maxDurationSeconds,
            isRequiredForNextPoint = isRequiredForNextPoint,
            maxScorePointsCount = maxScorePointsCount ?: 0,
            successScorePointsPercent = successScorePointsPercent ?: 0,
            questions = quizQuestions?.map { it.toDomainModel() } ?: emptyList()
        )

        TaskType.PHOTO -> QuestTask.Photo(
            questTaskId = questTaskId,
            taskType = type,
            description = description,
            maxDurationSeconds = maxDurationSeconds,
            isRequiredForNextPoint = isRequiredForNextPoint,
            scorePointsCount = scorePointsCount ?: 0
        )
    }
}

fun QuizQuestionDto.toDomainModel() = QuizQuestion(
    quizQuestionId = quizQuestionId,
    question = question,
    orderNumber = orderNumber,
    scorePointsCount = scorePointsCount,
    isMultipleAnswer = isMultipleAnswer,
    answers = answers.map { it.toDomainModel() }
)

fun QuizAnswerDto.toDomainModel() = QuizAnswer(
    quizAnswerId = quizAnswerId,
    answer = answer
)

fun TaskStartResponseDto.toDomainModel() = TaskStartData(
    participantTaskId = participantTaskId,
    questPointId = questPointId,
    startDate = Instant.parse(startDate),
    expiresAt = Instant.parse(expiresAt)
)

fun TaskSubmitResponseDto.toDomainModel() = TaskSubmitData(
    success = success,
    scoreEarned = scoreEarned,
    maxScore = maxScore,
    requiredPercentage = requiredPercentage,
    passed = passed,
    completedAt = Instant.parse(completedAt)
)
