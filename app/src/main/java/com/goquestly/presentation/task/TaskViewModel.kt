package com.goquestly.presentation.task

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goquestly.R
import com.goquestly.data.local.ActiveSessionManager
import com.goquestly.data.local.ServerTimeManager
import com.goquestly.domain.exception.BadRequestException
import com.goquestly.domain.model.QuestTask
import com.goquestly.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@HiltViewModel
class TaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val activeSessionManager: ActiveSessionManager,
    private val serverTimeManager: ServerTimeManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sessionId: Int = checkNotNull(savedStateHandle["sessionId"])
    private val pointId: Int = checkNotNull(savedStateHandle["pointId"])
    private val pointName: String = checkNotNull(savedStateHandle["pointName"])

    private val _state =
        MutableStateFlow(TaskState(sessionId = sessionId, pointId = pointId, pointName = pointName))
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        checkActiveTaskAndLoad()
    }

    private fun checkActiveTaskAndLoad() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val isExpired = activeSessionManager.isTaskExpired()
            if (isExpired) {
                activeSessionManager.clearTaskExpiration()
                activeSessionManager.clearQuizProgress()
                _state.update { it.copy(isTaskExpired = true, isLoading = false) }
                return@launch
            }

            sessionRepository.getActiveTask(sessionId)
                .onSuccess { activeTask ->
                    if (activeTask != null) {
                        restoreActiveTask(activeTask)
                    } else {
                        loadAndStartTask()
                    }
                }
                .onFailure {
                    loadAndStartTask()
                }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun restoreActiveTask(activeTaskData: com.goquestly.domain.model.TaskStartData) {
        sessionRepository.getTask(sessionId, pointId)
            .onSuccess { task ->
                val duration = activeTaskData.expiresAt - serverTimeManager.getCurrentServerTime()

                val savedQuestionIndex = if (task is QuestTask.Quiz) {
                    activeSessionManager.getQuizProgress(activeTaskData.participantTaskId) ?: 0
                } else {
                    0
                }

                activeSessionManager.saveTaskExpiration(activeTaskData.expiresAt.toEpochMilliseconds())

                _state.update {
                    it.copy(
                        task = task,
                        participantTaskId = activeTaskData.participantTaskId,
                        isTaskStarted = true,
                        currentQuestionIndex = savedQuestionIndex,
                        remainingTimeSeconds = duration.inWholeSeconds.coerceAtLeast(0L),
                        isLoading = false
                    )
                }
                startTimer()
            }
            .onFailure { error ->
                if (error.message?.contains("expired", ignoreCase = true) == true) {
                    activeSessionManager.clearTaskExpiration()
                    activeSessionManager.clearQuizProgress()
                    _state.update { it.copy(isTaskExpired = true, isLoading = false) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
            }
    }

    private suspend fun loadAndStartTask() {
        sessionRepository.getTask(sessionId, pointId)
            .onSuccess { task ->
                _state.update {
                    it.copy(
                        task = task,
                        isLoading = false
                    )
                }
                startTask()
            }
            .onFailure { error ->
                if (error is BadRequestException) {
                    activeSessionManager.clearTaskExpiration()
                    _state.update { it.copy(isTaskExpired = true, isLoading = false) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
            }
    }

    @OptIn(ExperimentalTime::class)
    fun startTask() {
        viewModelScope.launch {
            sessionRepository.startTask(sessionId, pointId)
                .onSuccess { response ->
                    activeSessionManager.saveTaskExpiration(response.expiresAt.toEpochMilliseconds())

                    val duration = response.expiresAt - response.startDate
                    _state.update {
                        it.copy(
                            participantTaskId = response.participantTaskId,
                            isTaskStarted = true,
                            remainingTimeSeconds = duration.inWholeSeconds
                        )
                    }
                    startTimer()
                }
                .onFailure { error ->
                    if (error is BadRequestException) {
                        _state.update { it.copy(isTaskExpired = true) }
                    } else {
                        _state.update {
                            it.copy(error = context.getString(R.string.error_something_went_wrong))
                        }
                    }
                }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.remainingTimeSeconds > 0) {
                delay(1.seconds)
                _state.update {
                    it.copy(remainingTimeSeconds = (it.remainingTimeSeconds - 1).coerceAtLeast(0))
                }
            }

            if (_state.value.remainingTimeSeconds == 0L) {
                _state.update {
                    it.copy(isTaskExpired = true)
                }
                activeSessionManager.clearQuizProgress()
            }
        }
    }

    fun updateCodeWord(input: String) {
        _state.update { it.copy(codeWordInput = input) }
    }

    fun submitCodeWord() {
        val codeWord = _state.value.codeWordInput
        if (codeWord.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            sessionRepository.submitCodeWord(sessionId, pointId, codeWord)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitResult = response
                        )
                    }
                    timerJob?.cancel()
                    activeSessionManager.clearQuizProgress()
                    activeSessionManager.clearTaskExpiration()
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun selectQuizAnswer(questionId: Int, answerId: Int, isMultiple: Boolean) {
        _state.update { currentState ->
            val currentAnswers = currentState.quizAnswers[questionId] ?: emptyList()

            val newAnswers = if (isMultiple) {
                if (answerId in currentAnswers) {
                    currentAnswers - answerId
                } else {
                    currentAnswers + answerId
                }
            } else {
                listOf(answerId)
            }

            currentState.copy(
                quizAnswers = currentState.quizAnswers + (questionId to newAnswers)
            )
        }
    }

    fun submitQuizAnswer(questionId: Int) {
        val answerIds = _state.value.quizAnswers[questionId] ?: return
        if (answerIds.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            sessionRepository.submitQuizAnswer(sessionId, pointId, questionId, answerIds)
                .onSuccess { response ->
                    val currentState = _state.value
                    val task = currentState.task as? QuestTask.Quiz
                    val participantTaskId = currentState.participantTaskId

                    if (response != null) {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                submitResult = response
                            )
                        }
                        timerJob?.cancel()
                        activeSessionManager.clearQuizProgress()
                        activeSessionManager.clearTaskExpiration()
                    } else {
                        val nextIndex = currentState.currentQuestionIndex + 1
                        val totalQuestions = task?.questions?.size ?: 0

                        if (nextIndex < totalQuestions) {
                            _state.update {
                                it.copy(
                                    isSubmitting = false,
                                    currentQuestionIndex = nextIndex,
                                    answeredQuestionsCount = currentState.answeredQuestionsCount + 1
                                )
                            }

                            if (participantTaskId != null) {
                                activeSessionManager.saveQuizProgress(participantTaskId, nextIndex)
                            }
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun setPhoto(uri: String, file: File) {
        _state.update {
            it.copy(
                photoUri = uri,
                photoFile = file
            )
        }
    }

    fun submitPhoto() {
        val photoFile = _state.value.photoFile ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }

            sessionRepository.submitPhoto(sessionId, pointId, photoFile)
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            submitResult = response
                        )
                    }
                    timerJob?.cancel()
                    activeSessionManager.clearQuizProgress()
                    activeSessionManager.clearTaskExpiration()
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = context.getString(R.string.error_something_went_wrong)
                        )
                    }
                }
        }
    }

    fun retry() {
        _state.update { it.copy(error = null) }
        checkActiveTaskAndLoad()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
