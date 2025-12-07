package com.goquestly.presentation.task

import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.model.TaskSubmitData

data class TaskState(
    val sessionId: Int = 0,
    val pointId: Int = 0,
    val pointName: String = "",
    val task: QuestTask? = null,
    val participantTaskId: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isTaskStarted: Boolean = false,
    val remainingTimeSeconds: Long = 0,
    val isSubmitting: Boolean = false,
    val submitResult: TaskSubmitData? = null,
    val isTaskExpired: Boolean = false,
    val codeWordInput: String = "",
    val currentQuestionIndex: Int = 0,
    val quizAnswers: Map<Int, List<Int>> = emptyMap(),
    val answeredQuestionsCount: Int = 0,
    val photoUri: String? = null,
    val photoFile: java.io.File? = null
)
